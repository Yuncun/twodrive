/*
 * Copyright 2026 Eric Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package codes.fixmy.twodrive.core.network.demo

import JvmUnitTestDemoAssetManager
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DemoGraphNetworkDataSourceTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val subject = DemoGraphNetworkDataSource(
        ioDispatcher = testDispatcher,
        networkJson = Json { ignoreUnknownKeys = true },
        assets = JvmUnitTestDemoAssetManager,
    )

    @Test
    fun deltaDeserializesEveryItemWithFacets() = runTest(testDispatcher) {
        val page = subject.getDelta(deltaLink = null)

        assertEquals(21, page.value.size)
        assertEquals(1, page.value.count { it.isRoot })
        assertEquals(DemoGraphNetworkDataSource.DEMO_DELTA_LINK, page.deltaLink)
        val pdf = page.value.first { it.name == "Apartment lease.pdf" }
        assertEquals("application/pdf", pdf.file?.mimeType)
        assertTrue(page.value.first { it.name == "Documents" }.isFolder)
    }

    @Test
    fun rootChildrenAreItemsWhoseParentIsRoot() = runTest(testDispatcher) {
        val children = subject.getChildren(itemId = null).value

        assertEquals(
            listOf("Documents", "Pictures", "Music", "Personal Vault", "Resume 2026.docx", "Household budget.xlsx"),
            children.map { it.name },
        )
    }

    @Test
    fun createdFolderIsAppendedToItsParentWithAFreeName() = runTest(testDispatcher) {
        val first = subject.createFolder(parentId = "f-documents", name = "Trips")
        val second = subject.createFolder(parentId = "f-documents", name = "Trips")

        assertEquals("Trips", first.name)
        assertEquals("Trips 1", second.name)
        assertTrue(second.isFolder)
        assertEquals(
            listOf(first.id, second.id),
            subject.getChildren("f-documents").value.filter { it.isFolder }.map { it.id }.takeLast(2),
        )
    }

    @Test
    fun createdFolderWithoutAParentLandsInTheRoot() = runTest(testDispatcher) {
        val folder = subject.createFolder(parentId = null, name = "Trips")

        assertEquals(folder, subject.getChildren(itemId = null).value.last())
    }

    @Test
    fun deletedFolderIsForgottenWithEverythingInside() = runTest(testDispatcher) {
        subject.deleteItem("f-documents")

        val ids = subject.getDelta(deltaLink = null).value.map { it.id }
        assertFalse("f-documents" in ids)
        assertFalse("i-notes" in ids)
        assertTrue("i-resume" in ids)
        assertFalse(subject.getChildren(itemId = null).value.any { it.name == "Documents" })
    }

    @Test
    fun deletingAnUnknownItemFails() = runTest(testDispatcher) {
        assertFailsWith<FileNotFoundException> { subject.deleteItem("no-such-item") }
    }

    @Test
    fun secondDeltaIsEmpty() = runTest(testDispatcher) {
        val page = subject.getDelta(deltaLink = DemoGraphNetworkDataSource.DEMO_DELTA_LINK)
        assertTrue(page.value.isEmpty())
        assertNotNull(page.deltaLink)
    }

    @Test
    fun driveAndMeDeserialize() = runTest(testDispatcher) {
        assertEquals("personal", subject.getDrive().driveType)
        assertEquals("demo@example.com", subject.getMe().userPrincipalName)
    }

    @Test
    fun thumbnailsPointAtTheBundledPlaceholderAsset() = runTest(testDispatcher) {
        val set = subject.getThumbnails("i-beach").single()

        assertEquals("file:///android_asset/thumbnails/i-beach.png", set.medium?.url)
        assertEquals(176, set.medium?.width)
        assertEquals(132, set.medium?.height)
    }

    @Test
    fun itemsWithoutABundledThumbnailHaveNone() = runTest(testDispatcher) {
        assertTrue(subject.getThumbnails("i-song").isEmpty())
    }

    @Test
    fun contentServesTheBundledPlaceholder() = runTest(testDispatcher) {
        val content = subject.getContent("i-notes")

        val text = content.use { it.stream.readBytes().decodeToString() }
        assertEquals(text.length.toLong(), content.length)
        assertTrue(text.startsWith("# Meeting notes"))
    }

    @Test
    fun contentOfAFileWithoutAPlaceholderFails() = runTest(testDispatcher) {
        assertFailsWith<FileNotFoundException> { subject.getContent("i-lease") }
    }
}

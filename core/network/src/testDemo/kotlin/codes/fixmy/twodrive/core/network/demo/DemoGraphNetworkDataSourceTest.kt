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
import kotlin.test.assertEquals
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
}

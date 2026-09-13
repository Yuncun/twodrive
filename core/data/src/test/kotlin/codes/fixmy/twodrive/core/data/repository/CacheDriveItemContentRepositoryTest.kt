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

package codes.fixmy.twodrive.core.data.repository

import codes.fixmy.twodrive.core.data.testdoubles.TestGraphNetworkDataSource
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CacheDriveItemContentRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val network = TestGraphNetworkDataSource()

    private val subject by lazy {
        CacheDriveItemContentRepository(network, tmp.root, UnconfinedTestDispatcher())
    }

    private val item = DriveItem(
        id = "i-notes",
        name = "Meeting notes.txt",
        isFolder = false,
        size = 5,
        lastModified = Instant.parse("2026-08-20T09:12:00Z"),
        mimeType = "text/plain",
        parentId = "root",
        webUrl = null,
    )

    @Test
    fun downloadReportsProgressAndWritesTheFileUnderItsName() = runTest {
        network.contents["i-notes"] = "hello".encodeToByteArray()

        val events = subject.download(item).toList()

        assertEquals(ContentDownload.Progress(0, 5), events.first())
        assertEquals(ContentDownload.Progress(5, 5), events[events.size - 2])
        val file = (events.last() as ContentDownload.Complete).file
        assertEquals("Meeting notes.txt", file.name)
        assertEquals("hello", file.readText())
    }

    @Test
    fun theSameVersionIsServedFromTheCache() = runTest {
        network.contents["i-notes"] = "hello".encodeToByteArray()
        subject.download(item).last()

        val events = subject.download(item).toList()

        assertEquals(1, network.contentRequests.size)
        assertEquals(1, events.size)
        assertEquals("hello", (events.single() as ContentDownload.Complete).file.readText())
    }

    @Test
    fun aNewerVersionReplacesTheCachedCopy() = runTest {
        network.contents["i-notes"] = "hello".encodeToByteArray()
        val old = (subject.download(item).last() as ContentDownload.Complete).file
        network.contents["i-notes"] = "hello again".encodeToByteArray()

        val newer = item.copy(lastModified = Instant.parse("2026-08-21T00:00:00Z"))
        val file = (subject.download(newer).last() as ContentDownload.Complete).file

        assertEquals("hello again", file.readText())
        assertTrue(!old.exists())
    }

    @Test
    fun aFailedDownloadLeavesNoFileBehind() = runTest {
        assertFailsWith<IOException> { subject.download(item).toList() }

        assertTrue(tmp.root.walk().none { it.isFile })
    }
}

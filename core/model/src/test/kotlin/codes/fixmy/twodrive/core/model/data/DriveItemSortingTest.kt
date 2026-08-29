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

package codes.fixmy.twodrive.core.model.data

import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals

class DriveItemSortingTest {

    private val folder = item("Photos", isFolder = true, modified = "2026-01-02T00:00:00Z", size = 0)
    private val newBig = item("zeta.mp4", modified = "2026-03-01T00:00:00Z", size = 5_000)
    private val oldSmall = item("Alpha.txt", modified = "2025-01-01T00:00:00Z", size = 10)

    @Test
    fun foldersAlwaysComeFirst() {
        val sorted = listOf(newBig, oldSmall, folder).sortedBy(SortOrder.NAME_ASCENDING)
        assertEquals(folder, sorted.first())
    }

    @Test
    fun nameSortIsCaseInsensitive() {
        val sorted = listOf(newBig, oldSmall).sortedBy(SortOrder.NAME_ASCENDING)
        assertEquals(listOf(oldSmall, newBig), sorted)
    }

    @Test
    fun modifiedNewestFirst() {
        val sorted = listOf(oldSmall, newBig).sortedBy(SortOrder.MODIFIED_NEWEST_FIRST)
        assertEquals(listOf(newBig, oldSmall), sorted)
    }

    @Test
    fun sizeLargestFirst() {
        val sorted = listOf(oldSmall, newBig).sortedBy(SortOrder.SIZE_LARGEST_FIRST)
        assertEquals(listOf(newBig, oldSmall), sorted)
    }

    private fun item(name: String, isFolder: Boolean = false, modified: String, size: Long) = DriveItem(
        id = name,
        name = name,
        isFolder = isFolder,
        size = size,
        lastModified = Instant.parse(modified),
        mimeType = null,
        parentId = "root",
        webUrl = null,
    )
}

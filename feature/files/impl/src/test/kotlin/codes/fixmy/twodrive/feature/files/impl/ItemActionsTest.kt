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

package codes.fixmy.twodrive.feature.files.impl

import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals

class ItemActionsTest {

    private fun item(isFolder: Boolean) = DriveItem(
        id = "id",
        name = "name",
        isFolder = isFolder,
        size = 0,
        lastModified = Instant.fromEpochMilliseconds(0),
        mimeType = null,
        parentId = null,
        webUrl = null,
    )

    @Test
    fun folderInAFolderListOffersEverythingButDownload() {
        assertEquals(
            listOf(ItemAction.SHARE, ItemAction.DELETE, ItemAction.RENAME, ItemAction.MOVE, ItemAction.DETAILS),
            itemActions(item(isFolder = true), ItemSource.FOLDER),
        )
    }

    @Test
    fun fileInAFolderListOffersTheFullSet() {
        assertEquals(ItemAction.entries, itemActions(item(isFolder = false), ItemSource.FOLDER))
    }

    @Test
    fun recentFileDropsDeleteAndMove() {
        assertEquals(
            listOf(ItemAction.SHARE, ItemAction.DOWNLOAD, ItemAction.RENAME, ItemAction.DETAILS),
            itemActions(item(isFolder = false), ItemSource.HOME_RECENT),
        )
    }
}

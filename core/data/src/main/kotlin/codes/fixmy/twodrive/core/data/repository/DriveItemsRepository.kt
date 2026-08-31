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

import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.flow.Flow

/**
 * Data layer for the files and folders in the user's drive.
 */
interface DriveItemsRepository {
    /**
     * The direct children of [folderId], or of the drive root when [folderId] is null. The list is
     * unsorted; callers apply the user's sort preference.
     */
    fun getChildren(folderId: String?): Flow<List<DriveItem>>

    fun getDriveItem(id: String): Flow<DriveItem?>

    /**
     * The newest [limit] files anywhere in the drive, newest first, for Files ▸ Home's
     * "Recent files" section. Folders are never recent files.
     */
    fun getRecentFiles(limit: Int): Flow<List<DriveItem>>

    /**
     * Pulls changes from Graph into the local cache. Returns true when the sync completed and
     * false when it failed (e.g. no network); it never throws.
     */
    suspend fun sync(): Boolean
}

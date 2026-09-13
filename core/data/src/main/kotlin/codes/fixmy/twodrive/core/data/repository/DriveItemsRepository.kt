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
     *
     * Graph's `@odata.nextLink` paging ends at [sync], which walks every page into Room; this
     * read never touches the network. It is a plain list rather than Paging 3 on purpose: the
     * query reads one folder through the `parent_id` index (never the whole drive), a row is a
     * few hundred bytes so even a 10,000-item folder is a few megabytes, the lazy list composes
     * only the visible rows, and the sort preference (folders first, then name, date or size) is
     * applied in memory, which a PagingSource would have to move into SQL.
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

    /**
     * Creates a folder called [name] in [parentId], or in the drive root when [parentId] is null,
     * and stores it locally so folder listings show it at once. It never throws.
     */
    suspend fun createFolder(parentId: String?, name: String): CreateFolderResult

    /**
     * Deletes item [id], and everything inside it when it is a folder. The local copies go first
     * so every listing drops them at once; when Graph refuses, they are put back. It never throws.
     */
    suspend fun deleteItem(id: String): DeleteResult
}

/** The outcome of [DriveItemsRepository.deleteItem]. */
enum class DeleteResult {
    /** Graph moved the item to the recycle bin. */
    DELETED,

    /** The request failed, such as for no connection, and the local copies are back. */
    FAILED,
}

/** The outcome of [DriveItemsRepository.createFolder]. */
sealed interface CreateFolderResult {
    /** Graph created [folder], possibly under a different name when the requested one was taken. */
    data class Created(val folder: DriveItem) : CreateFolderResult

    /** Graph refused with 507 Insufficient Storage: the account is over its quota. */
    data object StorageFull : CreateFolderResult

    /** Any other failure, such as no connection. */
    data object Failed : CreateFolderResult
}

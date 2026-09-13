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

package codes.fixmy.twodrive.core.testing.repository

import codes.fixmy.twodrive.core.data.repository.CreateFolderResult
import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class TestDriveItemsRepository : DriveItemsRepository {

    /**
     * The backing hot flow for the list of drive items for testing.
     */
    private val driveItemsFlow: MutableSharedFlow<List<DriveItem>> =
        MutableSharedFlow(replay = 1)

    var syncCount: Int = 0
        private set

    override fun getChildren(folderId: String?): Flow<List<DriveItem>> = driveItemsFlow.map { items ->
        val parent = folderId ?: items.firstOrNull { it.isRoot }?.id
        items.filter { it.parentId == parent && !it.isRoot }
    }

    override fun getDriveItem(id: String): Flow<DriveItem?> =
        driveItemsFlow.map { items -> items.firstOrNull { it.id == id } }

    override fun getRecentFiles(limit: Int): Flow<List<DriveItem>> = driveItemsFlow.map { items ->
        items.filterNot { it.isFolder }.sortedByDescending { it.lastModified }.take(limit)
    }

    override suspend fun sync(): Boolean {
        syncCount++
        return true
    }

    /** The (parentId, name) of every createFolder call, in order. */
    val createdFolders = mutableListOf<Pair<String?, String>>()

    /** What the next createFolder call returns; null creates a folder and appends it to the items. */
    var createFolderResult: CreateFolderResult? = null

    override suspend fun createFolder(parentId: String?, name: String): CreateFolderResult {
        createdFolders += parentId to name
        createFolderResult?.let { return it }
        val items = driveItemsFlow.replayCache.firstOrNull().orEmpty()
        val folder = DriveItem(
            id = "created-${createdFolders.size}",
            name = name,
            isFolder = true,
            isRoot = false,
            size = 0,
            lastModified = Instant.DISTANT_PAST,
            mimeType = null,
            parentId = parentId ?: items.firstOrNull { it.isRoot }?.id,
            webUrl = null,
            thumbnailUrl = null,
            childCount = 0,
            isShared = false,
        )
        driveItemsFlow.tryEmit(items + folder)
        return CreateFolderResult.Created(folder)
    }

    /**
     * A test-only API to allow controlling the list of drive items from tests.
     */
    fun sendDriveItems(driveItems: List<DriveItem>) {
        driveItemsFlow.tryEmit(driveItems)
    }
}

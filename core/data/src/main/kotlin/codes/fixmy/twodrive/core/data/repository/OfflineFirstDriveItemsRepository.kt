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

import android.util.Log
import androidx.tracing.trace
import codes.fixmy.twodrive.core.data.model.asEntity
import codes.fixmy.twodrive.core.database.dao.DriveItemDao
import codes.fixmy.twodrive.core.database.model.asExternalModel
import codes.fixmy.twodrive.core.datastore.TwoDrivePreferencesDataSource
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.retrofit.InsufficientStorageMonitor.Companion.HTTP_INSUFFICIENT_STORAGE
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.net.HttpURLConnection.HTTP_GONE
import javax.inject.Inject

/**
 * Disk storage backed implementation of [DriveItemsRepository]. Reads are exclusively from local
 * storage; [sync] pulls a Graph delta feed and applies it to the local table.
 */
internal class OfflineFirstDriveItemsRepository @Inject constructor(
    private val driveItemDao: DriveItemDao,
    private val network: GraphNetworkDataSource,
    private val preferences: TwoDrivePreferencesDataSource,
) : DriveItemsRepository {

    override fun getChildren(folderId: String?): Flow<List<DriveItem>> =
        (if (folderId == null) driveItemDao.getRootChildren() else driveItemDao.getChildren(folderId))
            .map { entities -> entities.map { it.asExternalModel() } }

    override fun getDriveItem(id: String): Flow<DriveItem?> =
        driveItemDao.getDriveItem(id).map { it?.asExternalModel() }

    override fun getRecentFiles(limit: Int): Flow<List<DriveItem>> =
        driveItemDao.getRecentFiles(limit).map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun sync(): Boolean = trace("DriveItems.sync") {
        suspendRunCatching {
            val deltaLink = preferences.getDeltaLink()
            try {
                applyDelta(deltaLink)
            } catch (e: HttpException) {
                // Graph replies 410 Gone (resyncRequired) when a delta token has expired: start
                // over with a full enumeration, which sweeps whatever it no longer lists.
                if (e.code() == HTTP_GONE && deltaLink != null) {
                    preferences.setDeltaLink(null)
                    applyDelta(null)
                } else {
                    throw e
                }
            }
        }.isSuccess
    }

    override suspend fun createFolder(parentId: String?, name: String): CreateFolderResult =
        trace("DriveItems.createFolder") {
            try {
                val entity = network.createFolder(parentId, name).asEntity()
                driveItemDao.upsertDriveItems(listOf(entity))
                CreateFolderResult.Created(entity.asExternalModel())
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (e: HttpException) {
                Log.i("DriveItems", "Graph refused to create folder", e)
                if (e.code() == HTTP_INSUFFICIENT_STORAGE) CreateFolderResult.StorageFull else CreateFolderResult.Failed
            } catch (e: Exception) {
                Log.i("DriveItems", "Failed to create folder", e)
                CreateFolderResult.Failed
            }
        }

    /**
     * Applies every page from [startLink] (a full enumeration when null) and only then stores
     * the final `@odata.deltaLink`, so a sync that fails part-way resumes from the last
     * complete position. A full enumeration lists live items only and never reports deletes,
     * so cached rows it did not list are swept once the last page has arrived.
     */
    private suspend fun applyDelta(startLink: String?) {
        val fullEnumeration = startLink == null
        val listedIds = mutableSetOf<String>()
        var page: NetworkDriveItemPage = network.getDelta(startLink)
        while (true) {
            apply(page.value)
            if (fullEnumeration) page.value.filterNot { it.isDeleted }.mapTo(listedIds) { it.id }
            val next = page.nextLink ?: break
            page = network.getPage(next)
        }
        if (fullEnumeration) {
            (driveItemDao.getAllIds() - listedIds)
                .chunked(SQLITE_MAX_BOUND_ARGS)
                .forEach { driveItemDao.deleteDriveItems(it) }
        }
        preferences.setDeltaLink(page.deltaLink)
    }

    private suspend fun apply(items: List<NetworkDriveItem>) {
        val (deleted, live) = items.partition { it.isDeleted }
        if (deleted.isNotEmpty()) driveItemDao.deleteDriveItems(deleted.map { it.id })
        if (live.isNotEmpty()) driveItemDao.upsertDriveItems(live.map { it.asEntity() })
    }
}

/** SQLite before 3.32 (Android 11 and older) binds at most 999 arguments per statement. */
private const val SQLITE_MAX_BOUND_ARGS = 999

/**
 * Like [runCatching], but re-throws cancellation so a cancelled coroutine is not mistaken for
 * a failed sync (the NiA sync pattern).
 */
private suspend fun <T> suspendRunCatching(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellationException: CancellationException) {
    throw cancellationException
} catch (exception: Exception) {
    Log.i(
        "DriveItemsSync",
        "Failed to sync the drive delta feed. Returning a failure result",
        exception,
    )
    Result.failure(exception)
}

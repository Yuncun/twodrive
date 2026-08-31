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
                // Graph replies 410 Gone when a delta token has expired: drop the cache and
                // start over.
                if (e.code() == HTTP_GONE && deltaLink != null) {
                    preferences.setDeltaLink(null)
                    driveItemDao.deleteAll()
                    applyDelta(null)
                } else {
                    throw e
                }
            }
        }.isSuccess
    }

    private suspend fun applyDelta(startLink: String?) {
        var page: NetworkDriveItemPage = network.getDelta(startLink)
        while (true) {
            apply(page.value)
            val next = page.nextLink
            if (next == null) {
                preferences.setDeltaLink(page.deltaLink)
                return
            }
            page = network.getPage(next)
        }
    }

    private suspend fun apply(items: List<NetworkDriveItem>) {
        val (deleted, live) = items.partition { it.isDeleted }
        if (deleted.isNotEmpty()) driveItemDao.deleteDriveItems(deleted.map { it.id })
        if (live.isNotEmpty()) driveItemDao.upsertDriveItems(live.map { it.asEntity() })
    }
}

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

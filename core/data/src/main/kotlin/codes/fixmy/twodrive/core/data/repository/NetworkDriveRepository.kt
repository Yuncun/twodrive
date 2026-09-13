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
import codes.fixmy.twodrive.core.model.data.Drive
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.retrofit.InsufficientStorageMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DriveRepository] that keeps the last `/me/drive` response in memory. The quota is only
 * shown in the account drawer and changes with every upload, so it is not worth a Room table.
 *
 * A 507 Insufficient Storage seen on any Graph call marks the quota full straight away, without
 * waiting for the next [refresh].
 */
@Singleton
internal class NetworkDriveRepository @Inject constructor(
    private val network: GraphNetworkDataSource,
    private val insufficientStorageMonitor: InsufficientStorageMonitor,
) : DriveRepository {

    private val lastDrive = MutableStateFlow<Drive?>(null)

    override val drive: Flow<Drive?> =
        combine(lastDrive, insufficientStorageMonitor.isStorageFull) { drive, storageFull ->
            if (storageFull) drive?.copy(isQuotaFull = true) else drive
        }

    override suspend fun refresh(): Boolean = try {
        val drive = network.getDrive().asExternalModel()
        // A fresh read is the authority: once it shows free space, forget an earlier 507.
        if (!drive.isQuotaFull) insufficientStorageMonitor.clear()
        lastDrive.value = drive
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.i("DriveRepository", "Failed to read the drive quota", e)
        false
    }
}

internal fun NetworkDrive.asExternalModel(): Drive {
    val used = quota?.used ?: 0
    val total = quota?.total ?: 0
    return Drive(
        id = id,
        quotaUsed = used,
        quotaTotal = total,
        isQuotaFull = quota?.state == QUOTA_STATE_EXCEEDED || total in 1..used,
    )
}

/** Graph's `quota.state` when the drive is over its limit; the others are normal, nearing, critical. */
private const val QUOTA_STATE_EXCEEDED = "exceeded"

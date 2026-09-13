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

package codes.fixmy.twodrive.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.fixmy.twodrive.core.data.repository.DriveRepository
import codes.fixmy.twodrive.core.data.util.NetworkMonitor
import codes.fixmy.twodrive.core.data.util.SyncManager
import codes.fixmy.twodrive.core.model.data.Drive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountDrawerViewModel @Inject constructor(
    private val driveRepository: DriveRepository,
    private val syncManager: SyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val uiState: StateFlow<AccountDrawerUiState> = combine(
        driveRepository.drive,
        networkMonitor.isOnline,
        syncManager.lastSyncFailed,
    ) { drive, isOnline, lastSyncFailed ->
        AccountDrawerUiState(
            drive = drive,
            banner = when {
                !isOnline -> AccountDrawerBanner.OFFLINE
                lastSyncFailed -> AccountDrawerBanner.SYNC_FAILED
                else -> null
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountDrawerUiState(),
    )

    init {
        refreshQuota()
    }

    /** Re-reads the quota, e.g. each time the drawer opens, since uploads elsewhere change it. */
    fun refreshQuota() {
        viewModelScope.launch { driveRepository.refresh() }
    }

    /** The sync-error banner's action: retry the sync and the quota read together. */
    fun retrySync() {
        syncManager.requestSync()
        refreshQuota()
    }
}

data class AccountDrawerUiState(
    /** Null until the first quota read succeeds. */
    val drive: Drive? = null,
    val banner: AccountDrawerBanner? = null,
)

/**
 * Notices shown in the drawer's banner slot, between the email row and the link rows. Offline
 * wins over a sync error, since being offline is usually why the sync failed.
 */
enum class AccountDrawerBanner {
    OFFLINE,
    SYNC_FAILED,
}

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

package codes.fixmy.twodrive.core.testing.util

import codes.fixmy.twodrive.core.data.util.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestSyncManager : SyncManager {

    private val syncing = MutableStateFlow(false)
    private val failed = MutableStateFlow(false)

    override val isSyncing: Flow<Boolean> = syncing
    override val lastSyncFailed: Flow<Boolean> = failed

    var requestSyncCount: Int = 0
        private set

    override fun requestSync() {
        requestSyncCount++
    }

    /**
     * A test-only API to set the sync state directly.
     */
    fun setSyncing(isSyncing: Boolean) {
        syncing.value = isSyncing
    }

    fun setLastSyncFailed(lastSyncFailed: Boolean) {
        failed.value = lastSyncFailed
    }
}

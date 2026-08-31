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

package codes.fixmy.twodrive.core.data.util

import codes.fixmy.twodrive.core.common.network.di.ApplicationScope
import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An app-scoped [SyncManager] that runs [DriveItemsRepository.sync] at most once per process:
 * concurrent and repeated requests are no-ops while a sync is running or after one succeeded,
 * and only a failed attempt (e.g. offline at launch) lets a later request retry.
 *
 * M2.2 (delta sync milestone) may replace this with a WorkManager-backed sync module like
 * NiA's `sync:work`, which adds retry with backoff and survives the process.
 */
@Singleton
internal class InProcessSyncManager @Inject constructor(
    private val driveItemsRepository: DriveItemsRepository,
    @ApplicationScope private val scope: CoroutineScope,
) : SyncManager {

    private val running = AtomicBoolean(false)
    private val succeeded = AtomicBoolean(false)

    private val syncing = MutableStateFlow(false)
    private val failed = MutableStateFlow(false)

    override val isSyncing: Flow<Boolean> = syncing.asStateFlow()
    override val lastSyncFailed: Flow<Boolean> = failed.asStateFlow()

    override fun requestSync() {
        if (succeeded.get() || !running.compareAndSet(false, true)) return
        scope.launch {
            syncing.value = true
            try {
                val ok = driveItemsRepository.sync()
                succeeded.set(ok)
                failed.value = !ok
            } finally {
                syncing.value = false
                running.set(false)
            }
        }
    }
}

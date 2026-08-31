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

import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InProcessSyncManagerTest {

    private val repository = RecordingDriveItemsRepository()

    @Test
    fun repeatedRequestsRunASingleSync() = runTest(UnconfinedTestDispatcher()) {
        val manager = InProcessSyncManager(repository, backgroundScope)

        manager.requestSync()
        manager.requestSync()
        manager.requestSync()

        assertEquals(1, repository.syncCount)
        assertFalse(manager.lastSyncFailed.first())
    }

    @Test
    fun requestsWhileASyncIsRunningAreNoOps() = runTest(UnconfinedTestDispatcher()) {
        val manager = InProcessSyncManager(repository, backgroundScope)
        repository.gate = CompletableDeferred()

        manager.requestSync()
        assertTrue(manager.isSyncing.first())
        manager.requestSync()
        repository.gate?.complete(true)

        assertFalse(manager.isSyncing.first())
        assertEquals(1, repository.syncCount)
    }

    @Test
    fun aFailedSyncIsReportedAsStateAndALaterRequestRetries() = runTest(UnconfinedTestDispatcher()) {
        val manager = InProcessSyncManager(repository, backgroundScope)
        repository.syncSucceeds = false

        manager.requestSync()

        assertTrue(manager.lastSyncFailed.first())
        assertFalse(manager.isSyncing.first())

        repository.syncSucceeds = true
        manager.requestSync()

        assertEquals(2, repository.syncCount)
        assertFalse(manager.lastSyncFailed.first())

        // The retry succeeded, so the process is synced and further requests are no-ops.
        manager.requestSync()
        assertEquals(2, repository.syncCount)
    }
}

/** Records [sync] calls; [gate] holds a sync open and [syncSucceeds] sets its result. */
private class RecordingDriveItemsRepository : DriveItemsRepository {
    var syncCount = 0
        private set
    var syncSucceeds = true
    var gate: CompletableDeferred<Boolean>? = null

    override fun getChildren(folderId: String?): Flow<List<DriveItem>> = flowOf(emptyList())

    override fun getDriveItem(id: String): Flow<DriveItem?> = flowOf(null)

    override fun getRecentFiles(limit: Int): Flow<List<DriveItem>> = flowOf(emptyList())

    override suspend fun sync(): Boolean {
        syncCount++
        gate?.await()
        return syncSucceeds
    }
}

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

package codes.fixmy.twodrive

import codes.fixmy.twodrive.core.model.data.Drive
import codes.fixmy.twodrive.core.testing.repository.TestDriveRepository
import codes.fixmy.twodrive.core.testing.util.MainDispatcherRule
import codes.fixmy.twodrive.core.testing.util.TestNetworkMonitor
import codes.fixmy.twodrive.core.testing.util.TestSyncManager
import codes.fixmy.twodrive.ui.AccountDrawerBanner
import codes.fixmy.twodrive.ui.AccountDrawerUiState
import codes.fixmy.twodrive.ui.AccountDrawerViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AccountDrawerViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val driveRepository = TestDriveRepository()
    private val syncManager = TestSyncManager()
    private val networkMonitor = TestNetworkMonitor()

    private lateinit var viewModel: AccountDrawerViewModel

    @Before
    fun setup() {
        viewModel = AccountDrawerViewModel(driveRepository, syncManager, networkMonitor)
    }

    @Test
    fun quotaIsReadWhenTheDrawerIsCreated() = runTest {
        assertEquals(1, driveRepository.refreshCount)
        assertEquals(AccountDrawerUiState(), viewModel.uiState.value)
    }

    @Test
    fun quotaFollowsTheRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        val full = Drive(id = "d", quotaUsed = 107_696_304_947, quotaTotal = 107_374_182_400)

        driveRepository.setDrive(full)

        assertEquals(full, viewModel.uiState.value.drive)
        assertTrue(viewModel.uiState.value.drive!!.isQuotaFull)
        assertNull(viewModel.uiState.value.banner)
    }

    @Test
    fun offlineShowsTheOfflineBannerEvenWhenTheSyncAlsoFailed() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        syncManager.setLastSyncFailed(true)
        assertEquals(AccountDrawerBanner.SYNC_FAILED, viewModel.uiState.value.banner)

        networkMonitor.setConnected(false)
        assertEquals(AccountDrawerBanner.OFFLINE, viewModel.uiState.value.banner)

        networkMonitor.setConnected(true)
        syncManager.setLastSyncFailed(false)
        assertNull(viewModel.uiState.value.banner)
    }

    @Test
    fun retrySyncRequestsASyncAndRereadsTheQuota() = runTest {
        viewModel.retrySync()

        assertEquals(1, syncManager.requestSyncCount)
        assertEquals(2, driveRepository.refreshCount)
    }
}

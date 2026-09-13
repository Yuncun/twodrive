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

package codes.fixmy.twodrive.feature.files.impl

import codes.fixmy.twodrive.core.data.repository.CreateFolderResult
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.UserData
import codes.fixmy.twodrive.core.model.data.ViewMode
import codes.fixmy.twodrive.core.testing.data.driveItemsTestData
import codes.fixmy.twodrive.core.testing.repository.TestDriveItemsRepository
import codes.fixmy.twodrive.core.testing.repository.TestUserDataRepository
import codes.fixmy.twodrive.core.testing.util.MainDispatcherRule
import codes.fixmy.twodrive.core.testing.util.TestNetworkMonitor
import codes.fixmy.twodrive.core.testing.util.TestSyncManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val driveItemsRepository = TestDriveItemsRepository()
    private val userDataRepository = TestUserDataRepository()
    private val syncManager = TestSyncManager()
    private val networkMonitor = TestNetworkMonitor()

    private lateinit var viewModel: FilesViewModel

    @Before
    fun setup() {
        viewModel = FilesViewModel(
            driveItemsRepository = driveItemsRepository,
            userDataRepository = userDataRepository,
            syncManager = syncManager,
            networkMonitor = networkMonitor,
            folderId = null,
        )
    }

    @Test
    fun stateIsInitiallyLoadingAndSyncIsRequested() = runTest {
        assertEquals(FilesUiState.Loading, viewModel.uiState.value)
        assertEquals(1, syncManager.requestSyncCount)
        assertEquals(0, driveItemsRepository.syncCount)
    }

    @Test
    fun myFilesIsTheLandingTabAndSelectionSticks() = runTest {
        assertEquals(FilesTab.MY_FILES, viewModel.selectedTab.value)

        viewModel.selectTab(FilesTab.OFFLINE)

        assertEquals(FilesTab.OFFLINE, viewModel.selectedTab.value)
    }

    @Test
    fun rootChildrenAreShownFoldersFirst() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        val state = assertIs<FilesUiState.Success>(viewModel.uiState.value)
        assertEquals(
            listOf("Documents", "Beach sunset.jpg", "Resume 2026.docx", "Video tour.mp4"),
            state.items.map { it.name },
        )
    }

    @Test
    fun changingSortOrderReordersItems() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        viewModel.setSortOrder(SortOrder.SIZE_LARGEST_FIRST)

        val state = assertIs<FilesUiState.Success>(viewModel.uiState.value)
        assertEquals(SortOrder.SIZE_LARGEST_FIRST, state.sortOrder)
        assertEquals(
            listOf("Documents", "Video tour.mp4", "Beach sunset.jpg", "Resume 2026.docx"),
            state.items.map { it.name },
        )
    }

    @Test
    fun changingViewModeIsReflectedInState() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        viewModel.setViewMode(ViewMode.TILE)

        val state = assertIs<FilesUiState.Success>(viewModel.uiState.value)
        assertEquals(ViewMode.TILE, state.viewMode)
    }

    @Test
    fun homeListsTheNewestFilesNewestFirstAndSkipsFolders() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.homeUiState.collect() }
        assertEquals(HomeUiState.Loading, viewModel.homeUiState.value)

        driveItemsRepository.sendDriveItems(driveItemsTestData)

        val state = assertIs<HomeUiState.Success>(viewModel.homeUiState.value)
        assertEquals(
            driveItemsTestData.filterNot { it.isFolder }
                .sortedByDescending { it.lastModified }
                .take(6)
                .map { it.id },
            state.recentFiles.map { it.id },
        )
    }

    @Test
    fun subfolderShowsOnlyItsChildren() = runTest {
        val subfolderViewModel = FilesViewModel(
            driveItemsRepository = driveItemsRepository,
            userDataRepository = userDataRepository,
            syncManager = syncManager,
            networkMonitor = networkMonitor,
            folderId = "f-documents",
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { subfolderViewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        val state = assertIs<FilesUiState.Success>(subfolderViewModel.uiState.value)
        assertEquals("Documents", state.folder?.name)
        assertEquals(listOf("Apartment lease.pdf", "Meeting notes.txt"), state.items.map { it.name })
    }

    @Test
    fun offlineOnlyWhenTheLastSyncFailedWithoutAConnection() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isOffline.collect() }
        assertFalse(viewModel.isOffline.value)

        networkMonitor.setConnected(false)
        assertFalse(viewModel.isOffline.value)

        syncManager.setLastSyncFailed(true)
        assertTrue(viewModel.isOffline.value)

        networkMonitor.setConnected(true)
        assertFalse(viewModel.isOffline.value)
    }

    @Test
    fun syncFailureWhileOnlineIsNotReportedAsOffline() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isOffline.collect() }

        syncManager.setLastSyncFailed(true)

        assertFalse(viewModel.isOffline.value)
    }

    @Test
    fun createdFolderGoesToThisScreensFolderAndIsListed() = runTest {
        val subfolderViewModel = FilesViewModel(
            driveItemsRepository = driveItemsRepository,
            userDataRepository = userDataRepository,
            syncManager = syncManager,
            networkMonitor = networkMonitor,
            folderId = "f-documents",
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { subfolderViewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        subfolderViewModel.createFolder("  Trips ")

        assertEquals(listOf<Pair<String?, String>>("f-documents" to "Trips"), driveItemsRepository.createdFolders)
        val state = assertIs<FilesUiState.Success>(subfolderViewModel.uiState.value)
        assertEquals("Trips", state.items.first().name)
        assertNull(subfolderViewModel.createFolderError.value)
    }

    @Test
    fun blankFolderNameIsNotSent() = runTest {
        viewModel.createFolder("   ")

        assertTrue(driveItemsRepository.createdFolders.isEmpty())
    }

    @Test
    fun storageFullIsReportedUntilShown() = runTest {
        driveItemsRepository.createFolderResult = CreateFolderResult.StorageFull

        viewModel.createFolder("Trips")

        assertEquals(CreateFolderError.STORAGE_FULL, viewModel.createFolderError.value)
        viewModel.createFolderErrorShown()
        assertNull(viewModel.createFolderError.value)
    }

    @Test
    fun otherCreateFolderFailuresAreReported() = runTest {
        driveItemsRepository.createFolderResult = CreateFolderResult.Failed

        viewModel.createFolder("Trips")

        assertEquals(CreateFolderError.FAILED, viewModel.createFolderError.value)
    }

    @Test
    fun reconnectingRequestsAnotherSync() = runTest {
        assertEquals(1, syncManager.requestSyncCount)

        networkMonitor.setConnected(false)
        assertEquals(1, syncManager.requestSyncCount)

        networkMonitor.setConnected(true)
        assertEquals(2, syncManager.requestSyncCount)
    }
}

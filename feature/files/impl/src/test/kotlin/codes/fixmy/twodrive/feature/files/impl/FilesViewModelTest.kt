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

import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.UserData
import codes.fixmy.twodrive.core.model.data.ViewMode
import codes.fixmy.twodrive.core.testing.data.driveItemsTestData
import codes.fixmy.twodrive.core.testing.repository.TestDriveItemsRepository
import codes.fixmy.twodrive.core.testing.repository.TestUserDataRepository
import codes.fixmy.twodrive.core.testing.util.MainDispatcherRule
import codes.fixmy.twodrive.core.testing.util.TestSyncManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FilesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val driveItemsRepository = TestDriveItemsRepository()
    private val userDataRepository = TestUserDataRepository()
    private val syncManager = TestSyncManager()

    private lateinit var viewModel: FilesViewModel

    @Before
    fun setup() {
        viewModel = FilesViewModel(
            driveItemsRepository = driveItemsRepository,
            userDataRepository = userDataRepository,
            syncManager = syncManager,
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
    fun subfolderShowsOnlyItsChildren() = runTest {
        val subfolderViewModel = FilesViewModel(
            driveItemsRepository = driveItemsRepository,
            userDataRepository = userDataRepository,
            syncManager = syncManager,
            folderId = "f-documents",
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { subfolderViewModel.uiState.collect() }
        driveItemsRepository.sendDriveItems(driveItemsTestData)
        userDataRepository.setUserData(UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST))

        val state = assertIs<FilesUiState.Success>(subfolderViewModel.uiState.value)
        assertEquals("Documents", state.folder?.name)
        assertEquals(listOf("Apartment lease.pdf", "Meeting notes.txt"), state.items.map { it.name })
    }
}

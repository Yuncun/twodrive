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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.fixmy.twodrive.core.common.result.Result
import codes.fixmy.twodrive.core.common.result.asResult
import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import codes.fixmy.twodrive.core.data.repository.UserDataRepository
import codes.fixmy.twodrive.core.data.util.SyncManager
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.ViewMode
import codes.fixmy.twodrive.core.model.data.sortedBy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FilesViewModel.Factory::class)
class FilesViewModel @AssistedInject constructor(
    private val driveItemsRepository: DriveItemsRepository,
    private val userDataRepository: UserDataRepository,
    syncManager: SyncManager,
    @Assisted val folderId: String?,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FilesTab.MY_FILES)

    /** The selected pivot tab. OneDrive lands on My files (docs/ux-reference/spec/my-files-list.md). */
    val selectedTab: StateFlow<FilesTab> = _selectedTab.asStateFlow()

    val uiState: StateFlow<FilesUiState> = combine(
        folderId?.let(driveItemsRepository::getDriveItem) ?: flowOf(null),
        driveItemsRepository.getChildren(folderId),
        userDataRepository.userData,
    ) { folder, items, userData ->
        FilesUiState.Success(
            folder = folder,
            items = items.sortedBy(userData.sortOrder),
            sortOrder = userData.sortOrder,
            viewMode = userData.viewMode,
        )
    }
        .asResult()
        .map { result ->
            when (result) {
                is Result.Success -> result.data
                is Result.Loading -> FilesUiState.Loading
                is Result.Error -> FilesUiState.Error(result.exception.message)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FilesUiState.Loading,
        )

    /**
     * The Home pivot. OneDrive's Recent files section renders six rows
     * (docs/ux-reference/spec/files-home.md).
     */
    val homeUiState: StateFlow<HomeUiState> = driveItemsRepository
        .getRecentFiles(RECENT_FILES_COUNT)
        .map<List<DriveItem>, HomeUiState>(HomeUiState::Success)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    init {
        // At most one sync per process; opening more screens must not re-walk the delta feed.
        syncManager.requestSync()
    }

    fun setSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch { userDataRepository.setSortOrder(sortOrder) }
    }

    fun selectTab(tab: FilesTab) {
        _selectedTab.value = tab
    }

    fun setViewMode(viewMode: ViewMode) {
        viewModelScope.launch { userDataRepository.setViewMode(viewMode) }
    }

    @AssistedFactory
    interface Factory {
        fun create(folderId: String?): FilesViewModel
    }

    private companion object {
        const val RECENT_FILES_COUNT = 6
    }
}

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
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.sortedBy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FilesViewModel.Factory::class)
class FilesViewModel @AssistedInject constructor(
    private val driveItemsRepository: DriveItemsRepository,
    private val userDataRepository: UserDataRepository,
    @Assisted val folderId: String?,
) : ViewModel() {

    val uiState: StateFlow<FilesUiState> = combine(
        folderId?.let(driveItemsRepository::getDriveItem) ?: flowOf(null),
        driveItemsRepository.getChildren(folderId),
        userDataRepository.userData,
    ) { folder, items, userData ->
        FilesUiState.Success(
            folder = folder,
            items = items.sortedBy(userData.sortOrder),
            sortOrder = userData.sortOrder,
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

    init {
        viewModelScope.launch { driveItemsRepository.sync() }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch { userDataRepository.setSortOrder(sortOrder) }
    }

    @AssistedFactory
    interface Factory {
        fun create(folderId: String?): FilesViewModel
    }
}

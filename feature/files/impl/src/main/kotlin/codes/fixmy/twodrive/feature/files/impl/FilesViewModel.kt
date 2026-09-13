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
import codes.fixmy.twodrive.core.data.repository.CreateFolderResult
import codes.fixmy.twodrive.core.data.repository.DeleteResult
import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import codes.fixmy.twodrive.core.data.repository.UserDataRepository
import codes.fixmy.twodrive.core.data.util.NetworkMonitor
import codes.fixmy.twodrive.core.data.util.SyncManager
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.ViewMode
import codes.fixmy.twodrive.core.model.data.sortedBy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FilesViewModel.Factory::class)
class FilesViewModel @AssistedInject constructor(
    private val driveItemsRepository: DriveItemsRepository,
    private val userDataRepository: UserDataRepository,
    private val syncManager: SyncManager,
    networkMonitor: NetworkMonitor,
    @Assisted val folderId: String?,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FilesTab.MY_FILES)

    /** The selected pivot tab. OneDrive lands on My files (docs/ux-reference/spec/my-files-list.md). */
    val selectedTab: StateFlow<FilesTab> = _selectedTab.asStateFlow()

    private val _pendingDelete = MutableStateFlow<DriveItem?>(null)

    /**
     * The item the user just deleted, while its Undo snackbar is up. It is hidden from the lists,
     * but nothing is sent to Graph until [deleteUndoWindowEnded].
     */
    val pendingDelete: StateFlow<DriveItem?> = _pendingDelete.asStateFlow()

    /** Items whose undo window has ended and whose delete request is still running. */
    private val deletingItems = MutableStateFlow(emptySet<DriveItem>())

    /** Everything to keep off screen: the pending delete and the ones in flight. */
    private val hiddenItems = combine(_pendingDelete, deletingItems) { pending, deleting ->
        if (pending == null) deleting else deleting + pending
    }

    val uiState: StateFlow<FilesUiState> = combine(
        folderId?.let(driveItemsRepository::getDriveItem) ?: flowOf(null),
        driveItemsRepository.getChildren(folderId),
        userDataRepository.userData,
        hiddenItems,
    ) { folder, items, userData, hidden ->
        val hiddenIds = hidden.mapTo(mutableSetOf(), DriveItem::id)
        FilesUiState.Success(
            folder = folder,
            items = items.filterNot { it.id in hiddenIds }.sortedBy(userData.sortOrder),
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
    @OptIn(ExperimentalCoroutinesApi::class)
    val homeUiState: StateFlow<HomeUiState> = combine(
        driveItemsRepository.getRecentFiles(RECENT_FILES_COUNT),
        hiddenItems,
    ) { recentFiles, hidden -> recentFiles to hidden.mapTo(mutableSetOf(), DriveItem::id) }
        .mapLatest<Pair<List<DriveItem>, Set<String>>, HomeUiState> { (recentFiles, hiddenIds) ->
            // A deleted folder takes its files out of Recent files too, before Room forgets them.
            HomeUiState.Success(
                if (hiddenIds.isEmpty()) recentFiles else recentFiles.filterNot { isInside(it, hiddenIds) },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    /**
     * True while the last sync failed and the device has no connection, so the screen can say
     * it is showing the files saved on this device. A failure while online is not an offline one.
     */
    val isOffline: StateFlow<Boolean> = combine(
        syncManager.lastSyncFailed,
        networkMonitor.isOnline,
    ) { lastSyncFailed, isOnline -> lastSyncFailed && !isOnline }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        // At most one sync per process; opening more screens must not re-walk the delta feed.
        syncManager.requestSync()
        // Coming back online retries a sync that failed; after a success the request is a no-op.
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .drop(1)
                .filter { it }
                .collect { syncManager.requestSync() }
        }
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

    private val _createFolderError = MutableStateFlow<CreateFolderError?>(null)

    /** Why the last create-folder request failed, until the screen reports it as shown. */
    val createFolderError: StateFlow<CreateFolderError?> = _createFolderError.asStateFlow()

    /**
     * Creates a folder called [name] in this screen's folder. On success Room already lists the
     * new folder, so there is nothing else to show.
     */
    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _createFolderError.value = when (driveItemsRepository.createFolder(folderId, name.trim())) {
                is CreateFolderResult.Created -> null
                CreateFolderResult.StorageFull -> CreateFolderError.STORAGE_FULL
                CreateFolderResult.Failed -> CreateFolderError.FAILED
            }
        }
    }

    fun createFolderErrorShown() {
        _createFolderError.value = null
    }

    private val _deleteError = MutableStateFlow<DriveItem?>(null)

    /** The item whose delete request failed, until the screen reports the error as shown. */
    val deleteError: StateFlow<DriveItem?> = _deleteError.asStateFlow()

    /**
     * Hides [item] and opens its undo window. A delete still waiting in its window is sent first,
     * since its snackbar gives way to this one.
     */
    fun deleteItem(item: DriveItem) {
        deleteUndoWindowEnded()
        _pendingDelete.value = item
    }

    /** Cancels the pending delete; the item shows again in its sorted place. */
    fun undoDelete() {
        _pendingDelete.value = null
    }

    /**
     * Sends the pending delete once its snackbar has gone without Undo. On failure the repository
     * has put the rows back, and [deleteError] names the item.
     */
    fun deleteUndoWindowEnded() {
        val item = _pendingDelete.value ?: return
        _pendingDelete.value = null
        deletingItems.update { it + item }
        viewModelScope.launch {
            val result = driveItemsRepository.deleteItem(item.id)
            deletingItems.update { it - item }
            if (result == DeleteResult.FAILED) _deleteError.value = item
        }
    }

    fun deleteErrorShown() {
        _deleteError.value = null
    }

    /** Whether [item] is one of [ids] or lies somewhere inside one of them. */
    private suspend fun isInside(item: DriveItem, ids: Set<String>): Boolean {
        var current: DriveItem? = item
        while (current != null) {
            if (current.id in ids) return true
            current = current.parentId?.let { driveItemsRepository.getDriveItem(it).first() }
        }
        return false
    }

    @AssistedFactory
    interface Factory {
        fun create(folderId: String?): FilesViewModel
    }

    private companion object {
        const val RECENT_FILES_COUNT = 6
    }
}

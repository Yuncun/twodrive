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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.fixmy.twodrive.core.data.repository.ContentDownload
import codes.fixmy.twodrive.core.data.repository.DriveItemContentRepository
import codes.fixmy.twodrive.core.model.data.DriveItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Opens a tapped file: downloads it to the cache, then hands it to the UI to show in another app.
 */
@HiltViewModel
class OpenFileViewModel @Inject constructor(
    private val contentRepository: DriveItemContentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OpenFileUiState>(OpenFileUiState.Idle)
    val uiState: StateFlow<OpenFileUiState> = _uiState.asStateFlow()

    private var download: Job? = null

    /** Starts downloading [item], replacing any download already running. */
    fun open(item: DriveItem) {
        download?.cancel()
        _uiState.value = OpenFileUiState.Downloading(item, progress = null)
        download = viewModelScope.launch {
            try {
                contentRepository.download(item).conflate().collect { event ->
                    _uiState.value = when (event) {
                        is ContentDownload.Progress -> OpenFileUiState.Downloading(
                            item = item,
                            progress = if (event.totalBytes > 0) event.bytesRead.toFloat() / event.totalBytes else null,
                        )
                        is ContentDownload.Complete -> OpenFileUiState.Ready(item, event.file)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.i("OpenFileViewModel", "Failed to download ${item.id}", e)
                _uiState.value = OpenFileUiState.Failed
            }
        }
    }

    fun cancel() {
        download?.cancel()
        _uiState.value = OpenFileUiState.Idle
    }

    /** The UI has launched the viewer, or told the user why it could not. */
    fun onResultHandled() {
        _uiState.value = OpenFileUiState.Idle
    }
}

sealed interface OpenFileUiState {
    data object Idle : OpenFileUiState

    /** [progress] runs from 0 to 1, or is null while the size is unknown. */
    data class Downloading(val item: DriveItem, val progress: Float?) : OpenFileUiState

    data class Ready(val item: DriveItem, val file: File) : OpenFileUiState

    data object Failed : OpenFileUiState
}

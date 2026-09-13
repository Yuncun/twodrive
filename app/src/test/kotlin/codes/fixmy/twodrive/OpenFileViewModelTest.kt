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

import codes.fixmy.twodrive.core.data.repository.ContentDownload
import codes.fixmy.twodrive.core.testing.data.driveItemsTestData
import codes.fixmy.twodrive.core.testing.repository.TestDriveItemContentRepository
import codes.fixmy.twodrive.core.testing.util.MainDispatcherRule
import codes.fixmy.twodrive.ui.OpenFileUiState
import codes.fixmy.twodrive.ui.OpenFileViewModel
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals

class OpenFileViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = TestDriveItemContentRepository()
    private val viewModel = OpenFileViewModel(repository)
    private val item = driveItemsTestData.first { !it.isFolder }

    @Test
    fun downloadProgressIsShownUntilTheFileIsReady() {
        viewModel.open(item)
        assertEquals(OpenFileUiState.Downloading(item, progress = null), viewModel.uiState.value)

        repository.send(ContentDownload.Progress(bytesRead = 25, totalBytes = 100))
        assertEquals(OpenFileUiState.Downloading(item, progress = 0.25f), viewModel.uiState.value)

        val file = File("Resume 2026.docx")
        repository.send(ContentDownload.Complete(file))
        assertEquals(OpenFileUiState.Ready(item, file), viewModel.uiState.value)

        viewModel.onResultHandled()
        assertEquals(OpenFileUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun anUnknownSizeShowsIndeterminateProgress() {
        viewModel.open(item)
        repository.send(ContentDownload.Progress(bytesRead = 25, totalBytes = -1))

        assertEquals(OpenFileUiState.Downloading(item, progress = null), viewModel.uiState.value)
    }

    @Test
    fun aFailedDownloadIsReported() {
        viewModel.open(item)
        repository.finish(IOException("offline"))

        assertEquals(OpenFileUiState.Failed, viewModel.uiState.value)
    }

    @Test
    fun cancellingStopsTheDownload() {
        viewModel.open(item)
        viewModel.cancel()
        repository.send(ContentDownload.Complete(File("late")))

        assertEquals(OpenFileUiState.Idle, viewModel.uiState.value)
    }
}

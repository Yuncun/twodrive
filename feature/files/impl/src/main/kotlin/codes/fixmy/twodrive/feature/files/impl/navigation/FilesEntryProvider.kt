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

package codes.fixmy.twodrive.feature.files.impl.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.navigation.Navigator
import codes.fixmy.twodrive.feature.files.api.navigation.FilesNavKey
import codes.fixmy.twodrive.feature.files.api.navigation.navigateToFolder
import codes.fixmy.twodrive.feature.files.impl.FilesScreen
import codes.fixmy.twodrive.feature.files.impl.FilesViewModel

fun EntryProviderScope<NavKey>.filesEntry(
    navigator: Navigator,
    onFileClick: (DriveItem) -> Unit,
) {
    entry<FilesNavKey> { key ->
        val folderId = key.folderId
        FilesScreen(
            onFolderClick = navigator::navigateToFolder,
            onFileClick = onFileClick,
            viewModel = hiltViewModel<FilesViewModel, FilesViewModel.Factory>(
                key = folderId ?: "root",
            ) { factory ->
                factory.create(folderId)
            },
        )
    }
}

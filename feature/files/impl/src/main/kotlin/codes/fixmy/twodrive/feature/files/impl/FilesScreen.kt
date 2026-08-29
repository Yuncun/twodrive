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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveLoadingWheel
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.ui.DevicePreviews
import codes.fixmy.twodrive.core.ui.DriveItemPreviewParameterProvider
import codes.fixmy.twodrive.core.ui.formatFileSize
import codes.fixmy.twodrive.core.ui.icon
import codes.fixmy.twodrive.core.ui.iconTint
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun FilesScreen(
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FilesScreen(
        uiState = uiState,
        onFolderClick = onFolderClick,
        onFileClick = onFileClick,
        onSortOrderChange = viewModel::setSortOrder,
        modifier = modifier,
    )
}

@Composable
internal fun FilesScreen(
    uiState: FilesUiState,
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            FilesUiState.Loading -> TwoDriveLoadingWheel(
                modifier = Modifier.align(Alignment.Center),
                contentDesc = stringResource(R.string.feature_files_impl_loading),
            )

            is FilesUiState.Error -> Text(
                text = uiState.message ?: stringResource(R.string.feature_files_impl_error),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            )

            is FilesUiState.Success -> FilesList(
                items = uiState.items,
                sortOrder = uiState.sortOrder,
                onFolderClick = onFolderClick,
                onFileClick = onFileClick,
                onSortOrderChange = onSortOrderChange,
            )
        }
    }
}

@Composable
private fun FilesList(
    items: List<DriveItem>,
    sortOrder: SortOrder,
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("files:list"),
    ) {
        item {
            SortHeader(sortOrder = sortOrder, onSortOrderChange = onSortOrderChange)
        }
        if (items.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.feature_files_impl_empty_folder),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            }
        }
        items(items = items, key = DriveItem::id) { item ->
            DriveItemRow(
                item = item,
                onClick = { if (item.isFolder) onFolderClick(item.id) else onFileClick(item) },
            )
        }
    }
}

@Composable
private fun SortHeader(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(sortOrder.labelRes()),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("files:sort")) {
                Icon(
                    imageVector = TwoDriveIcons.Sort,
                    contentDescription = stringResource(R.string.feature_files_impl_sort),
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SortOrder.entries.forEach { order ->
                    DropdownMenuItem(
                        text = { Text(stringResource(order.labelRes())) },
                        onClick = {
                            expanded = false
                            onSortOrderChange(order)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DriveItemRow(
    item: DriveItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon(),
            contentDescription = null,
            tint = item.iconTint(),
            modifier = Modifier
                .padding(end = 16.dp)
                .size(40.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
            Text(
                text = item.subtitle(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = TwoDriveIcons.MoreVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DriveItem.subtitle(): String {
    val date = lastModified.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateText = "${date.month.name.take(3).lowercase().replaceFirstChar(Char::titlecase)} ${date.dayOfMonth}, ${date.year}"
    return if (isFolder) {
        val count = childCount ?: 0
        "$dateText · ${pluralStringResource(R.plurals.feature_files_impl_item_count, count, count)}"
    } else {
        "$dateText · ${formatFileSize(size)}"
    }
}

private fun SortOrder.labelRes(): Int = when (this) {
    SortOrder.NAME_ASCENDING -> R.string.feature_files_impl_sort_name_ascending
    SortOrder.NAME_DESCENDING -> R.string.feature_files_impl_sort_name_descending
    SortOrder.MODIFIED_NEWEST_FIRST -> R.string.feature_files_impl_sort_modified_newest
    SortOrder.MODIFIED_OLDEST_FIRST -> R.string.feature_files_impl_sort_modified_oldest
    SortOrder.SIZE_LARGEST_FIRST -> R.string.feature_files_impl_sort_size
}

@DevicePreviews
@Composable
fun FilesScreenPreview(
    @PreviewParameter(DriveItemPreviewParameterProvider::class)
    items: List<DriveItem>,
) {
    TwoDriveTheme {
        FilesScreen(
            uiState = FilesUiState.Success(folder = null, items = items, sortOrder = SortOrder.NAME_ASCENDING),
            onFolderClick = {},
            onFileClick = {},
            onSortOrderChange = {},
        )
    }
}

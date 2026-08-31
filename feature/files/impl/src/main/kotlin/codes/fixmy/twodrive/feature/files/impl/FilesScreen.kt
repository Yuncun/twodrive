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

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveLoadingWheel
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveTab
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveTabRow
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.ui.DevicePreviews
import codes.fixmy.twodrive.core.ui.DriveItemPreviewParameterProvider
import codes.fixmy.twodrive.core.ui.formatFileSize
import codes.fixmy.twodrive.core.ui.formatModifiedDate
import codes.fixmy.twodrive.core.ui.icon
import codes.fixmy.twodrive.core.ui.iconTint
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun FilesScreen(
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    FilesScreen(
        uiState = uiState,
        selectedTab = selectedTab,
        // The pivot row belongs to the drive root; pushed folders show only their list.
        showTabRow = viewModel.folderId == null,
        onTabClick = viewModel::selectTab,
        onFolderClick = onFolderClick,
        onFileClick = onFileClick,
        // Will open the item bottom sheet once that screen exists.
        onMoreClick = {},
        onSortOrderChange = viewModel::setSortOrder,
        modifier = modifier,
    )
}

@Composable
internal fun FilesScreen(
    uiState: FilesUiState,
    selectedTab: FilesTab,
    showTabRow: Boolean,
    onTabClick: (FilesTab) -> Unit,
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showTabRow) {
            FilesTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
        }
        when (selectedTab) {
            FilesTab.MY_FILES -> MyFilesTab(
                uiState = uiState,
                today = today,
                onFolderClick = onFolderClick,
                onFileClick = onFileClick,
                onMoreClick = onMoreClick,
                onSortOrderChange = onSortOrderChange,
            )

            FilesTab.HOME -> FilesTabEmptyState(
                icon = TwoDriveIcons.Home,
                titleRes = R.string.feature_files_impl_empty_home_title,
                captionRes = R.string.feature_files_impl_empty_home_caption,
            )

            FilesTab.SHARED -> FilesTabEmptyState(
                icon = TwoDriveIcons.People,
                titleRes = R.string.feature_files_impl_empty_shared_title,
                captionRes = R.string.feature_files_impl_empty_shared_caption,
            )

            FilesTab.VAULT -> FilesTabEmptyState(
                icon = TwoDriveIcons.Lock,
                titleRes = R.string.feature_files_impl_empty_vault_title,
                captionRes = R.string.feature_files_impl_empty_vault_caption,
            )

            FilesTab.OFFLINE -> FilesTabEmptyState(
                icon = TwoDriveIcons.OfflinePin,
                titleRes = R.string.feature_files_impl_empty_offline_title,
                captionRes = R.string.feature_files_impl_empty_offline_caption,
            )
        }
    }
}

@Composable
private fun FilesTabRow(
    selectedTab: FilesTab,
    onTabClick: (FilesTab) -> Unit,
) {
    TwoDriveTabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = Modifier.testTag("files:tabs"),
    ) {
        FilesTab.entries.forEach { tab ->
            TwoDriveTab(
                selected = tab == selectedTab,
                onClick = { onTabClick(tab) },
                icon = {
                    Icon(
                        imageVector = if (tab == selectedTab) tab.selectedIcon() else tab.unselectedIcon(),
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(tab.labelRes())) },
            )
        }
    }
}

@Composable
private fun MyFilesTab(
    uiState: FilesUiState,
    today: LocalDate,
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
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
                today = today,
                onFolderClick = onFolderClick,
                onFileClick = onFileClick,
                onMoreClick = onMoreClick,
                onSortOrderChange = onSortOrderChange,
            )
        }
    }
}

/**
 * What an empty pivot tab shows: a large icon standing in for OneDrive's illustration, a bold
 * title and a grey caption, centred (docs/ux-reference/17-vault.png, 18-offline.png).
 */
@Composable
private fun FilesTabEmptyState(
    icon: ImageVector,
    @StringRes titleRes: Int,
    @StringRes captionRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(captionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun FilesList(
    items: List<DriveItem>,
    sortOrder: SortOrder,
    today: LocalDate,
    onFolderClick: (String) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
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
                today = today,
                onClick = { if (item.isFolder) onFolderClick(item.id) else onFileClick(item) },
                onMoreClick = { onMoreClick(item) },
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
    today: LocalDate,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
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
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.subtitle(today),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = TwoDriveIcons.MoreHoriz,
                contentDescription = stringResource(R.string.feature_files_impl_more_options, item.name),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The secondary line of a row: OneDrive puts the size first and the date second, e.g.
 * "9.2 MB · Jul 12, 2024", and folders show their recursive size just like files
 * (docs/ux-reference/11-myfiles.png).
 */
private fun DriveItem.subtitle(today: LocalDate): String =
    "${formatFileSize(size)} · ${formatModifiedDate(lastModified, today = today)}"

private fun FilesTab.labelRes(): Int = when (this) {
    FilesTab.HOME -> R.string.feature_files_impl_tab_home
    FilesTab.MY_FILES -> R.string.feature_files_impl_tab_my_files
    FilesTab.SHARED -> R.string.feature_files_impl_tab_shared
    FilesTab.VAULT -> R.string.feature_files_impl_tab_vault
    FilesTab.OFFLINE -> R.string.feature_files_impl_tab_offline
}

private fun FilesTab.selectedIcon(): ImageVector = when (this) {
    FilesTab.HOME -> TwoDriveIcons.Home
    FilesTab.MY_FILES -> TwoDriveIcons.Folder
    FilesTab.SHARED -> TwoDriveIcons.People
    FilesTab.VAULT -> TwoDriveIcons.Lock
    FilesTab.OFFLINE -> TwoDriveIcons.OfflinePin
}

private fun FilesTab.unselectedIcon(): ImageVector = when (this) {
    FilesTab.HOME -> TwoDriveIcons.HomeBorder
    FilesTab.MY_FILES -> TwoDriveIcons.FolderBorder
    FilesTab.SHARED -> TwoDriveIcons.PeopleBorder
    FilesTab.VAULT -> TwoDriveIcons.LockBorder
    FilesTab.OFFLINE -> TwoDriveIcons.OfflinePinBorder
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
            selectedTab = FilesTab.MY_FILES,
            showTabRow = true,
            onTabClick = {},
            onFolderClick = {},
            onFileClick = {},
            onMoreClick = {},
            onSortOrderChange = {},
        )
    }
}

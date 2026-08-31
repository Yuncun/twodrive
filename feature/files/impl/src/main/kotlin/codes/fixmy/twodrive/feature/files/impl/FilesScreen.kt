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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import codes.fixmy.twodrive.core.designsystem.component.DynamicAsyncImage
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveLoadingWheel
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveTab
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveTabRow
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.ViewMode
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
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun FilesScreen(
    folderName: String?,
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (viewModel.folderId == null) {
        // The pivot row belongs to the drive root; pushed folders show only their list.
        val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
        val homeUiState by viewModel.homeUiState.collectAsStateWithLifecycle()
        FilesScreen(
            uiState = uiState,
            homeUiState = homeUiState,
            selectedTab = selectedTab,
            onTabClick = viewModel::selectTab,
            onFolderClick = onFolderClick,
            onFileClick = onFileClick,
            // Will open the item bottom sheet once that screen exists.
            onMoreClick = {},
            // Will open the full recents list once that screen exists.
            onSeeAllClick = {},
            onSortOrderChange = viewModel::setSortOrder,
            onViewModeChange = viewModel::setViewMode,
            modifier = modifier,
        )
    } else {
        FolderScreen(
            folderName = folderName.orEmpty(),
            uiState = uiState,
            onBackClick = onBackClick,
            onFolderClick = onFolderClick,
            onFileClick = onFileClick,
            onMoreClick = {},
            onSortOrderChange = viewModel::setSortOrder,
            onViewModeChange = viewModel::setViewMode,
            modifier = modifier,
        )
    }
}

@Composable
internal fun FilesScreen(
    uiState: FilesUiState,
    homeUiState: HomeUiState,
    selectedTab: FilesTab,
    onTabClick: (FilesTab) -> Unit,
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    onSeeAllClick: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FilesTabRow(selectedTab = selectedTab, onTabClick = onTabClick)
            when (selectedTab) {
                FilesTab.MY_FILES -> MyFilesTab(
                    uiState = uiState,
                    today = today,
                    onFolderClick = onFolderClick,
                    onFileClick = onFileClick,
                    onMoreClick = onMoreClick,
                    onSortOrderChange = onSortOrderChange,
                    onViewModeChange = onViewModeChange,
                )

                FilesTab.HOME -> HomeTab(
                    homeUiState = homeUiState,
                    today = today,
                    onFileClick = onFileClick,
                    onMoreClick = onMoreClick,
                    onSeeAllClick = onSeeAllClick,
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
        FilesFloatingLayer(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * A pushed folder: a large title over the same list, per docs/ux-reference/spec/folder.md.
 * OneDrive scrolls the whole header away, back arrow included; M3's large top app bar instead
 * docks into a collapsed bar that keeps the title and the back arrow — a deliberate divergence
 * the spec recommends.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FolderScreen(
    folderName: String,
    uiState: FilesUiState,
    onBackClick: () -> Unit,
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = folderName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = TwoDriveIcons.ArrowBack,
                        contentDescription = stringResource(R.string.feature_files_impl_navigate_up),
                    )
                }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent),
            scrollBehavior = scrollBehavior,
        )
        MyFilesTab(
            uiState = uiState,
            today = today,
            onFolderClick = onFolderClick,
            onFileClick = onFileClick,
            onMoreClick = onMoreClick,
            onSortOrderChange = onSortOrderChange,
            onViewModeChange = onViewModeChange,
        )
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
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
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

            is FilesUiState.Success -> Column {
                SortViewBar(
                    sortOrder = uiState.sortOrder,
                    viewMode = uiState.viewMode,
                    onSortOrderChange = onSortOrderChange,
                    onViewModeChange = onViewModeChange,
                )
                when (uiState.viewMode) {
                    ViewMode.LIST -> FilesList(
                        items = uiState.items,
                        today = today,
                        onFolderClick = onFolderClick,
                        onFileClick = onFileClick,
                        onMoreClick = onMoreClick,
                    )

                    ViewMode.TILE -> FilesTileGrid(
                        items = uiState.items,
                        today = today,
                        onFolderClick = onFolderClick,
                        onFileClick = onFileClick,
                        onMoreClick = onMoreClick,
                    )
                }
            }
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
    today: LocalDate,
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("files:list"),
    ) {
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
                onClick = { if (item.isFolder) onFolderClick(item) else onFileClick(item) },
                onMoreClick = { onMoreClick(item) },
            )
        }
    }
}

/**
 * The Tile layout: a three-column grid of thumbnails with the name, the date alone and a centred
 * "⋯" under each (docs/ux-reference/spec/my-files-tile.md). Folders badge their item count inside
 * the thumbnail — the opposite of list view, where folders show a size and never a count.
 */
@Composable
private fun FilesTileGrid(
    items: List<DriveItem>,
    today: LocalDate,
    onFolderClick: (DriveItem) -> Unit,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("files:grid"),
    ) {
        if (items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.feature_files_impl_empty_folder),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                )
            }
        }
        gridItems(items = items, key = DriveItem::id) { item ->
            DriveItemTile(
                item = item,
                today = today,
                onClick = { if (item.isFolder) onFolderClick(item) else onFileClick(item) },
                onMoreClick = { onMoreClick(item) },
            )
        }
    }
}

@Composable
private fun DriveItemTile(
    item: DriveItem,
    today: LocalDate,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(width = 101.dp, height = 58.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            val thumbnailUrl = item.thumbnailUrl
            if (thumbnailUrl != null) {
                DynamicAsyncImage(
                    imageUrl = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // No thumbnail yet (the demo drive carries none): the type glyph stands in, as
                // OneDrive shows for files it cannot render.
                Icon(
                    imageVector = item.icon(),
                    contentDescription = null,
                    tint = item.iconTint(),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                )
            }
            if (item.isFolder) {
                Text(
                    text = (item.childCount ?: 0).toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = formatModifiedDate(item.lastModified, today = today),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
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
 * The bar pinned between the header and the list: the sort chip at the left, the view-options
 * button at the right, and a hairline divider under both (docs/ux-reference/spec/my-files-list.md).
 */
@Composable
private fun SortViewBar(
    sortOrder: SortOrder,
    viewMode: ViewMode,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortChip(sortOrder = sortOrder, onSortOrderChange = onSortOrderChange)
            Spacer(modifier = Modifier.weight(1f))
            ViewMenuButton(viewMode = viewMode, onViewModeChange = onViewModeChange)
        }
        HorizontalDivider()
    }
}

/**
 * The view-options button and its "View as:" menu: a non-clickable header, then List and Tile,
 * each with a leading checkmark when selected and a trailing glyph tinted blue when selected
 * (docs/ux-reference/spec/view-menu.md, 14-view-options.png).
 */
@Composable
private fun ViewMenuButton(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("files:view")) {
            Icon(
                imageVector = TwoDriveIcons.Tune,
                contentDescription = stringResource(R.string.feature_files_impl_view_options),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(205.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_files_impl_view_as),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            ViewMenuItem(
                labelRes = R.string.feature_files_impl_view_list,
                icon = TwoDriveIcons.ViewList,
                selected = viewMode == ViewMode.LIST,
                onClick = {
                    expanded = false
                    onViewModeChange(ViewMode.LIST)
                },
            )
            ViewMenuItem(
                labelRes = R.string.feature_files_impl_view_tile,
                icon = TwoDriveIcons.GridView,
                selected = viewMode == ViewMode.TILE,
                onClick = {
                    expanded = false
                    onViewModeChange(ViewMode.TILE)
                },
            )
        }
    }
}

@Composable
private fun ViewMenuItem(
    @StringRes labelRes: Int,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(labelRes)) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = TwoDriveIcons.Check,
                    contentDescription = null,
                )
            } else {
                // Reserve the leading slot so labels stay aligned (docs/ux-reference/spec/view-menu.md).
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun SortChip(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val keyLabel = stringResource(sortOrder.key().labelRes())
    val chipDescription = stringResource(R.string.feature_files_impl_sort_by, keyLabel)
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(keyLabel) },
            leadingIcon = {
                Icon(
                    imageVector = if (sortOrder.isFirstDirection()) {
                        TwoDriveIcons.ArrowDownward
                    } else {
                        TwoDriveIcons.ArrowUpward
                    },
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) TwoDriveIcons.KeyboardArrowUp else TwoDriveIcons.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            border = null,
            modifier = Modifier
                .testTag("files:sort")
                .semantics { contentDescription = chipDescription },
        )
        SortMenu(
            expanded = expanded,
            sortOrder = sortOrder,
            onDismissRequest = { expanded = false },
            onSortOrderChange = onSortOrderChange,
        )
    }
}

/**
 * OneDrive's two-group sort menu: a key group and a direction group, one checkmark in each,
 * separated by a hairline (docs/ux-reference/spec/sort-menu.md, 13-sort-menu.png).
 */
@Composable
private fun SortMenu(
    expanded: Boolean,
    sortOrder: SortOrder,
    onDismissRequest: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
) {
    val key = sortOrder.key()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(246.dp),
    ) {
        SortKey.entries.forEach { menuKey ->
            SortMenuItem(
                labelRes = menuKey.labelRes(),
                checked = menuKey == key,
                onClick = {
                    onDismissRequest()
                    onSortOrderChange(menuKey.orderFor(first = sortOrder.isFirstDirection()))
                },
            )
        }
        HorizontalDivider()
        SortMenuItem(
            labelRes = key.firstDirectionLabelRes(),
            checked = sortOrder.isFirstDirection(),
            onClick = {
                onDismissRequest()
                onSortOrderChange(key.orderFor(first = true))
            },
        )
        SortMenuItem(
            labelRes = key.secondDirectionLabelRes(),
            checked = !sortOrder.isFirstDirection(),
            onClick = {
                onDismissRequest()
                onSortOrderChange(key.orderFor(first = false))
            },
        )
    }
}

@Composable
private fun SortMenuItem(
    @StringRes labelRes: Int,
    checked: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(labelRes)) },
        leadingIcon = {
            if (checked) {
                Icon(
                    imageVector = TwoDriveIcons.Check,
                    contentDescription = null,
                )
            } else {
                // Reserve the leading slot so labels stay aligned (docs/ux-reference/spec/sort-menu.md).
                Spacer(modifier = Modifier.size(24.dp))
            }
        },
        onClick = onClick,
    )
}

/**
 * One list row, on the observed OneDrive metrics: a uniform 68dp row (OneDrive splits 64/68 by
 * kind; TwoDrive picks one), 40dp icon at x=16, text at x=68, the ⋯ button 10dp off the right
 * edge, no divider (docs/ux-reference/spec/my-files-list.md).
 */
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
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon(),
            contentDescription = null,
            tint = item.iconTint(),
            modifier = Modifier
                .padding(end = 12.dp)
                .size(40.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isShared) {
                    Icon(
                        imageVector = TwoDriveIcons.People,
                        contentDescription = stringResource(R.string.feature_files_impl_shared),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(16.dp),
                    )
                }
                Text(
                    text = item.subtitle(today),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
 * "9.2 MB · Jul 12, 2024" with a non-breaking space on each side of the dot, and folders show
 * their recursive size just like files
 * (docs/ux-reference/11-myfiles.png).
 */
private fun DriveItem.subtitle(today: LocalDate): String =
    "${formatFileSize(size)}\u00A0·\u00A0${formatModifiedDate(lastModified, today = today)}"

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

/**
 * The sort menu's key group. Each key pairs with two directions; the first direction is the one
 * OneDrive leads with — A to Z, newest first, largest first. Only Name's labels were observed
 * (docs/ux-reference/spec/sort-menu.md); Modified and File size follow the same pattern.
 */
private enum class SortKey {
    NAME,
    MODIFIED,
    SIZE,
}

private fun SortOrder.key(): SortKey = when (this) {
    SortOrder.NAME_ASCENDING, SortOrder.NAME_DESCENDING -> SortKey.NAME
    SortOrder.MODIFIED_NEWEST_FIRST, SortOrder.MODIFIED_OLDEST_FIRST -> SortKey.MODIFIED
    SortOrder.SIZE_LARGEST_FIRST, SortOrder.SIZE_SMALLEST_FIRST -> SortKey.SIZE
}

private fun SortOrder.isFirstDirection(): Boolean = when (this) {
    SortOrder.NAME_ASCENDING,
    SortOrder.MODIFIED_NEWEST_FIRST,
    SortOrder.SIZE_LARGEST_FIRST,
    -> true

    SortOrder.NAME_DESCENDING,
    SortOrder.MODIFIED_OLDEST_FIRST,
    SortOrder.SIZE_SMALLEST_FIRST,
    -> false
}

private fun SortKey.orderFor(first: Boolean): SortOrder = when (this) {
    SortKey.NAME -> if (first) SortOrder.NAME_ASCENDING else SortOrder.NAME_DESCENDING
    SortKey.MODIFIED -> if (first) SortOrder.MODIFIED_NEWEST_FIRST else SortOrder.MODIFIED_OLDEST_FIRST
    SortKey.SIZE -> if (first) SortOrder.SIZE_LARGEST_FIRST else SortOrder.SIZE_SMALLEST_FIRST
}

private fun SortKey.labelRes(): Int = when (this) {
    SortKey.NAME -> R.string.feature_files_impl_sort_key_name
    SortKey.MODIFIED -> R.string.feature_files_impl_sort_key_modified
    SortKey.SIZE -> R.string.feature_files_impl_sort_key_size
}

private fun SortKey.firstDirectionLabelRes(): Int = when (this) {
    SortKey.NAME -> R.string.feature_files_impl_sort_a_to_z
    SortKey.MODIFIED -> R.string.feature_files_impl_sort_newest_first
    SortKey.SIZE -> R.string.feature_files_impl_sort_largest_first
}

private fun SortKey.secondDirectionLabelRes(): Int = when (this) {
    SortKey.NAME -> R.string.feature_files_impl_sort_z_to_a
    SortKey.MODIFIED -> R.string.feature_files_impl_sort_oldest_first
    SortKey.SIZE -> R.string.feature_files_impl_sort_smallest_first
}

/**
 * Files ▸ Home: a "Recent files" section over an "Offline files" section, as a LazyColumn of
 * sections so a third section can be added later without a rewrite
 * (docs/ux-reference/spec/files-home.md).
 */
@Composable
private fun HomeTab(
    homeUiState: HomeUiState,
    today: LocalDate,
    onFileClick: (DriveItem) -> Unit,
    onMoreClick: (DriveItem) -> Unit,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (homeUiState) {
            HomeUiState.Loading -> TwoDriveLoadingWheel(
                modifier = Modifier.align(Alignment.Center),
                contentDesc = stringResource(R.string.feature_files_impl_loading),
            )

            is HomeUiState.Success -> LazyColumn(modifier = Modifier.testTag("files:home")) {
                item {
                    HomeSectionHeader(
                        titleRes = R.string.feature_files_impl_home_recent_files,
                        actionLabelRes = R.string.feature_files_impl_home_see_all,
                        onActionClick = onSeeAllClick,
                    )
                }
                if (homeUiState.recentFiles.isEmpty()) {
                    item {
                        HomeEmptySectionCard(
                            text = stringResource(R.string.feature_files_impl_home_recent_empty),
                        )
                    }
                }
                items(items = homeUiState.recentFiles, key = DriveItem::id) { item ->
                    HomeRecentFileRow(
                        item = item,
                        today = today,
                        onClick = { onFileClick(item) },
                        onMoreClick = { onMoreClick(item) },
                    )
                }
                item { HomeSectionHeader(titleRes = R.string.feature_files_impl_home_offline_files) }
                item {
                    HomeEmptySectionCard(
                        text = stringResource(R.string.feature_files_impl_home_offline_empty),
                    )
                }
            }
        }
    }
}

/**
 * A 56dp Home section header: bold title at the gutter and an optional blue text button at the
 * right ("See all") — docs/ux-reference/spec/files-home.md.
 */
@Composable
private fun HomeSectionHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes actionLabelRes: Int? = null,
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (actionLabelRes != null) {
            TextButton(onClick = onActionClick) {
                Text(text = stringResource(actionLabelRes))
            }
        }
    }
}

/**
 * A Home recent-files row: 68dp tall, taller than the 64dp My-files row, because it carries a
 * 40dp rounded thumbnail (docs/ux-reference/spec/files-home.md). Until M2.3 syncs real
 * thumbnails the slot draws the file-type icon on a tinted square; the video play badge, the
 * document type badge and the leading video duration in the subtitle need the thumbnail and
 * video facets that arrive with the real Graph milestones.
 */
@Composable
private fun HomeRecentFileRow(
    item: DriveItem,
    today: LocalDate,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(item.iconTint().copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon(),
                contentDescription = null,
                tint = item.iconTint(),
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
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
 * The pale-blue filled card an empty Home section shows — deliberately not an M3 Card: no
 * elevation, no border, no icon (docs/ux-reference/spec/files-home.md). OneDrive's #EAF4FC
 * maps to the theme's primaryContainer so the card stays themed in dark mode. Reused for the
 * offline empty state; empty folders can adopt it with M2.5.
 */
@Composable
private fun HomeEmptySectionCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp),
        )
    }
}

/**
 * The search pill and "+" FAB that float over every Files pivot and never scroll away
 * (docs/ux-reference/spec/files-home.md). Both are placeholders: the pill opens search with
 * M4.1 and the FAB opens the add menu with M3.3/M3.6. Unlike the observed OneDrive build,
 * whose pill is mislabelled "Search your photos", the pill's accessible text is exactly its
 * visible label.
 */
@Composable
private fun FilesFloatingLayer(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            // Opens the search screen once M4.1 builds it.
            onClick = {},
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(28.dp),
            // On OneDrive's 411dp reference screen the pill's observed 272dp width is exactly
            // the space left beside the FAB, so it flexes rather than being hardcoded.
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("files:search"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = TwoDriveIcons.Search,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.feature_files_impl_search_your_files),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        FloatingActionButton(
            // Opens the add menu once M3.3 builds it.
            onClick = {},
            modifier = Modifier.testTag("files:add"),
        ) {
            Icon(
                imageVector = TwoDriveIcons.Add,
                contentDescription = stringResource(R.string.feature_files_impl_add),
            )
        }
    }
}

@DevicePreviews
@Composable
fun FilesScreenPreview(
    @PreviewParameter(DriveItemPreviewParameterProvider::class)
    items: List<DriveItem>,
) {
    TwoDriveTheme {
        FilesScreen(
            uiState = FilesUiState.Success(
                folder = null,
                items = items,
                sortOrder = SortOrder.NAME_ASCENDING,
                viewMode = ViewMode.LIST,
            ),
            homeUiState = HomeUiState.Success(recentFiles = items.filterNot { it.isFolder }),
            selectedTab = FilesTab.MY_FILES,
            onTabClick = {},
            onFolderClick = {},
            onFileClick = {},
            onMoreClick = {},
            onSeeAllClick = {},
            onSortOrderChange = {},
            onViewModeChange = {},
        )
    }
}

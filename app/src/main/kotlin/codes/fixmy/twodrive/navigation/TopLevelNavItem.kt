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

package codes.fixmy.twodrive.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.feature.files.api.navigation.FilesNavKey
import codes.fixmy.twodrive.feature.files.api.R as filesR

/**
 * Type for the top level navigation items in the application. Contains metadata about the item
 * that is used in the top app bar and common navigation UI.
 *
 * @param selectedIcon The icon to be displayed in the navigation UI when this item is selected.
 * @param unselectedIcon The icon to be displayed in the navigation UI when this item is not selected.
 * @param iconTextId Text that to be displayed in the navigation UI.
 * @param titleTextId Text that is displayed on the top app bar.
 */
data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
)

val FILES = TopLevelNavItem(
    selectedIcon = TwoDriveIcons.Folder,
    unselectedIcon = TwoDriveIcons.FolderBorder,
    iconTextId = filesR.string.feature_files_api_title,
    titleTextId = filesR.string.feature_files_api_title,
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    FilesNavKey() to FILES,
)

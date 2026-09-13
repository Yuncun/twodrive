/*
 * Copyright 2022 Eric Shen
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

@file:OptIn(ExperimentalMaterial3Api::class)

package codes.fixmy.twodrive.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoDriveTopAppBar(
    @StringRes titleRes: Int,
    navigationIcon: ImageVector,
    navigationIconContentDescription: String,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    TwoDriveTopAppBar(
        titleRes = titleRes,
        navigationIcon = {
            Icon(
                imageVector = navigationIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        navigationIconContentDescription = navigationIconContentDescription,
        modifier = modifier,
        actionIcon = actionIcon,
        actionIconContentDescription = actionIconContentDescription,
        colors = colors,
        onNavigationClick = onNavigationClick,
        onActionClick = onActionClick,
    )
}

/**
 * TwoDrive top app bar whose navigation button shows any content, such as an account avatar.
 * [navigationIconContentDescription] describes the button, so [navigationIcon] should not add
 * its own content description.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoDriveTopAppBar(
    @StringRes titleRes: Int,
    navigationIcon: @Composable () -> Unit,
    navigationIconContentDescription: String,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    actionIconContentDescription: String? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = titleRes)) },
        navigationIcon = {
            IconButton(
                onClick = onNavigationClick,
                modifier = Modifier.semantics {
                    contentDescription = navigationIconContentDescription
                },
            ) {
                navigationIcon()
            }
        },
        actions = {
            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionIconContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        colors = colors,
        modifier = modifier.testTag("niaTopAppBar"),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun TwoDriveTopAppBarPreview() {
    TwoDriveTheme {
        TwoDriveTopAppBar(
            titleRes = android.R.string.untitled,
            navigationIcon = TwoDriveIcons.Search,
            navigationIconContentDescription = "Navigation icon",
            actionIcon = TwoDriveIcons.MoreVert,
            actionIconContentDescription = "Action icon",
        )
    }
}

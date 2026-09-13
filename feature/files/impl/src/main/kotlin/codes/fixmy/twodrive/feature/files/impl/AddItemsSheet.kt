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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons

/**
 * The add-items flow the "+" FAB starts: the add menu while [sheetVisible], and the create-folder
 * dialog its Create folder row opens. The dialog outlives the sheet, so its state lives here.
 */
@Composable
internal fun AddItems(
    sheetVisible: Boolean,
    onSheetDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit,
) {
    var createFolderDialogVisible by rememberSaveable { mutableStateOf(false) }
    if (sheetVisible) {
        AddItemsSheet(
            onDismiss = onSheetDismiss,
            onCreateFolderClick = {
                onSheetDismiss()
                createFolderDialogVisible = true
            },
        )
    }
    if (createFolderDialogVisible) {
        CreateFolderDialog(
            onDismiss = { createFolderDialogVisible = false },
            onCreate = { name ->
                createFolderDialogVisible = false
                onCreateFolder(name)
            },
        )
    }
}

/**
 * The add menu. No OneDrive capture exists (docs/ux-reference/spec/add-menu.md), so it follows
 * the item bottom sheet's row metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemsSheet(
    onDismiss: () -> Unit,
    onCreateFolderClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AddItemsSheetContent(onCreateFolderClick = onCreateFolderClick)
    }
}

@Composable
internal fun AddItemsSheetContent(
    onCreateFolderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = 16.dp)) {
        AddItemsRow(
            icon = TwoDriveIcons.CreateNewFolder,
            label = stringResource(R.string.feature_files_impl_create_folder),
            onClick = onCreateFolderClick,
            modifier = Modifier.testTag("files:add:createFolder"),
        )
        // Upload arrives with M3.6; until then the row is shown but disabled.
        AddItemsRow(
            icon = TwoDriveIcons.Upload,
            label = stringResource(R.string.feature_files_impl_upload),
            onClick = {},
            enabled = false,
            modifier = Modifier.testTag("files:add:upload"),
        )
    }
}

/** A 48dp row: a 24dp icon inset 16dp and its label starting at 56dp. */
@Composable
private fun AddItemsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface.let {
        if (enabled) it else it.copy(alpha = DISABLED_CONTENT_ALPHA)
    }
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Reports a failed create-folder request and, once the snackbar is gone, tells the view model it
 * was shown. Clearing the error any earlier would restart this effect and cancel the snackbar.
 */
@Composable
internal fun CreateFolderErrorSnackbarHost(
    error: CreateFolderError?,
    onShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message = error?.let {
        stringResource(
            when (it) {
                CreateFolderError.STORAGE_FULL -> R.string.feature_files_impl_create_folder_storage_full
                CreateFolderError.FAILED -> R.string.feature_files_impl_create_folder_failed
            },
        )
    }
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(error) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            currentOnShown()
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = modifier)
}

/** Material 3's opacity for disabled content. */
private const val DISABLED_CONTENT_ALPHA = 0.38f

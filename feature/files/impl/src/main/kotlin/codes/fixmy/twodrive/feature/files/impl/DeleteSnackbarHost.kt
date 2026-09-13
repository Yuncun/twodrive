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

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import codes.fixmy.twodrive.core.model.data.DriveItem

/**
 * Offers Undo while [pendingDelete] waits in its undo window, then reports a delete that Graph
 * refused. Undo calls [onUndo]; the snackbar timing out or being swiped away calls
 * [onUndoWindowEnd], which sends the delete. A new pending delete cancels the old snackbar
 * without either callback, because the view model has already sent the old delete. Leaving
 * composition (popping the screen) also calls [onUndoWindowEnd], so the delete is not dropped.
 */
@Composable
internal fun DeleteSnackbarHost(
    pendingDelete: DriveItem?,
    deleteError: DriveItem?,
    onUndo: () -> Unit,
    onUndoWindowEnd: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = pendingDelete?.let { stringResource(R.string.feature_files_impl_deleted, it.name) }
    val undoLabel = stringResource(R.string.feature_files_impl_undo)
    val errorMessage = deleteError?.let { stringResource(R.string.feature_files_impl_delete_failed, it.name) }
    val currentOnUndo by rememberUpdatedState(onUndo)
    val currentOnUndoWindowEnd by rememberUpdatedState(onUndoWindowEnd)
    val currentOnErrorShown by rememberUpdatedState(onErrorShown)
    LaunchedEffect(pendingDelete) {
        if (deletedMessage != null) {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> currentOnUndo()
                SnackbarResult.Dismissed -> currentOnUndoWindowEnd()
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { currentOnUndoWindowEnd() }
    }
    LaunchedEffect(deleteError) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            currentOnErrorShown()
        }
    }
    SnackbarHost(hostState = snackbarHostState, modifier = modifier.testTag("files:delete"))
}

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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import codes.fixmy.twodrive.R
import kotlinx.coroutines.launch
import java.io.File

/**
 * Shows [uiState]: a progress dialog while a file downloads, then the system viewer for it, or a
 * snackbar when the download failed or no installed app can open the file.
 */
@Composable
internal fun OpenFileHost(
    uiState: OpenFileUiState,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit,
    onResultHandled: () -> Unit,
) {
    val context = LocalContext.current
    // Snackbars outlive the state that raised them, so they are not tied to its effect.
    val scope = rememberCoroutineScope()
    val noAppMessage = stringResource(R.string.open_file_no_app)
    val failedMessage = stringResource(R.string.open_file_failed)

    when (uiState) {
        OpenFileUiState.Idle -> Unit
        is OpenFileUiState.Downloading -> Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(dismissOnClickOutside = false),
        ) {
            DownloadProgress(uiState.item.name, uiState.progress, onCancel)
        }
        is OpenFileUiState.Ready -> LaunchedEffect(uiState) {
            val opened = context.viewFile(uiState.file, uiState.item.mimeType)
            onResultHandled()
            if (!opened) scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
        }
        OpenFileUiState.Failed -> LaunchedEffect(uiState) {
            onResultHandled()
            scope.launch { snackbarHostState.showSnackbar(failedMessage) }
        }
    }
}

@Composable
internal fun DownloadProgress(
    fileName: String,
    progress: Float?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.open_file_downloading),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val indicatorModifier = Modifier.fillMaxWidth().testTag("openFile:progress")
            if (progress == null) {
                LinearProgressIndicator(indicatorModifier)
            } else {
                LinearProgressIndicator(progress = { progress }, modifier = indicatorModifier)
            }
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    }
}

/**
 * Hands [file] to whichever app handles [mimeType], granting it read access through the app's
 * FileProvider. Returns false when no installed app can open it.
 */
private fun Context.viewFile(file: File, mimeType: String?): Boolean {
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, mimeType ?: contentResolver.getType(uri))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

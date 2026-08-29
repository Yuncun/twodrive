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

package codes.fixmy.twodrive.core.ui

import androidx.compose.ui.graphics.vector.ImageVector
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.model.data.DriveItem

/**
 * Icon representing a file's kind, derived from its MIME type the way the OneDrive file list does.
 */
fun DriveItem.icon(): ImageVector {
    if (isFolder) return TwoDriveIcons.Folder
    val mime = mimeType ?: return TwoDriveIcons.File
    return when {
        mime.startsWith("image/") -> TwoDriveIcons.Image
        mime.startsWith("video/") -> TwoDriveIcons.Video
        mime.startsWith("audio/") -> TwoDriveIcons.Audio
        mime == "application/pdf" -> TwoDriveIcons.Pdf
        else -> TwoDriveIcons.File
    }
}

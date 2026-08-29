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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.designsystem.theme.FileTypeColors
import codes.fixmy.twodrive.core.model.data.DriveItem

/**
 * The kinds of item the file list draws differently: a yellow folder and one icon and brand color
 * per document type, as in OneDrive's file list.
 */
enum class DriveItemKind(val icon: ImageVector, val tint: Color) {
    FOLDER(TwoDriveIcons.Folder, FileTypeColors.Folder),
    DOCUMENT(TwoDriveIcons.Document, FileTypeColors.Word),
    SPREADSHEET(TwoDriveIcons.Spreadsheet, FileTypeColors.Excel),
    PRESENTATION(TwoDriveIcons.Presentation, FileTypeColors.PowerPoint),
    PDF(TwoDriveIcons.Pdf, FileTypeColors.Pdf),
    IMAGE(TwoDriveIcons.Image, FileTypeColors.Image),
    VIDEO(TwoDriveIcons.Video, FileTypeColors.Video),
    AUDIO(TwoDriveIcons.Audio, FileTypeColors.Audio),
    TEXT(TwoDriveIcons.Text, FileTypeColors.Text),
    OTHER(TwoDriveIcons.File, FileTypeColors.Generic),
}

/**
 * Which [DriveItemKind] this item is, derived from its MIME type.
 */
fun DriveItem.kind(): DriveItemKind {
    if (isFolder) return DriveItemKind.FOLDER
    val mime = mimeType ?: return DriveItemKind.OTHER
    return when {
        mime.startsWith("image/") -> DriveItemKind.IMAGE
        mime.startsWith("video/") -> DriveItemKind.VIDEO
        mime.startsWith("audio/") -> DriveItemKind.AUDIO
        mime == "application/pdf" -> DriveItemKind.PDF
        mime.contains("wordprocessingml") ||
            mime == "application/msword" ||
            mime == "application/vnd.oasis.opendocument.text" -> DriveItemKind.DOCUMENT

        mime.contains("spreadsheetml") ||
            mime == "application/vnd.ms-excel" ||
            mime == "application/vnd.oasis.opendocument.spreadsheet" -> DriveItemKind.SPREADSHEET

        mime.contains("presentationml") ||
            mime == "application/vnd.ms-powerpoint" ||
            mime == "application/vnd.oasis.opendocument.presentation" -> DriveItemKind.PRESENTATION

        mime.startsWith("text/") -> DriveItemKind.TEXT
        else -> DriveItemKind.OTHER
    }
}

/** Icon representing this item's kind in the file list. */
fun DriveItem.icon(): ImageVector = kind().icon

/** Tint for [icon]: yellow for folders, the document type's brand color for files. */
fun DriveItem.iconTint(): Color = kind().tint

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

import codes.fixmy.twodrive.core.designsystem.theme.FileTypeColors
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.datetime.Instant
import org.junit.Test
import kotlin.test.assertEquals

class DriveItemIconTest {

    @Test
    fun foldersAreYellowWhateverTheirMimeType() {
        assertEquals(DriveItemKind.FOLDER, item(isFolder = true).kind())
        assertEquals(FileTypeColors.Folder, item(isFolder = true).iconTint())
    }

    @Test
    fun mediaIsMatchedOnTheMimeTypesFamily() {
        assertEquals(DriveItemKind.IMAGE, item(mimeType = "image/heic").kind())
        assertEquals(DriveItemKind.VIDEO, item(mimeType = "video/mp4").kind())
        assertEquals(DriveItemKind.AUDIO, item(mimeType = "audio/mpeg").kind())
        assertEquals(DriveItemKind.TEXT, item(mimeType = "text/markdown").kind())
    }

    @Test
    fun officeDocumentsGetTheirOwnBrandColor() {
        val word = item(
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        )
        val excel = item(
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        )
        val powerPoint = item(
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        )
        assertEquals(FileTypeColors.Word, word.iconTint())
        assertEquals(FileTypeColors.Excel, excel.iconTint())
        assertEquals(FileTypeColors.PowerPoint, powerPoint.iconTint())
    }

    @Test
    fun legacyOfficeMimeTypesMapToTheSameKinds() {
        assertEquals(DriveItemKind.DOCUMENT, item(mimeType = "application/msword").kind())
        assertEquals(DriveItemKind.SPREADSHEET, item(mimeType = "application/vnd.ms-excel").kind())
        assertEquals(
            DriveItemKind.PRESENTATION,
            item(mimeType = "application/vnd.ms-powerpoint").kind(),
        )
    }

    @Test
    fun pdfsAreRed() {
        assertEquals(DriveItemKind.PDF, item(mimeType = "application/pdf").kind())
        assertEquals(FileTypeColors.Pdf, item(mimeType = "application/pdf").iconTint())
    }

    @Test
    fun unknownAndMissingMimeTypesFallBackToTheGenericFile() {
        assertEquals(DriveItemKind.OTHER, item(mimeType = "application/x-7z-compressed").kind())
        assertEquals(DriveItemKind.OTHER, item(mimeType = null).kind())
    }

    @Test
    fun everyKindHasItsOwnIcon() {
        val icons = DriveItemKind.entries.map { it.icon }
        assertEquals(DriveItemKind.entries.size, icons.distinct().size)
    }

    private fun item(isFolder: Boolean = false, mimeType: String? = null) = DriveItem(
        id = "1",
        name = "item",
        isFolder = isFolder,
        size = 0,
        lastModified = Instant.fromEpochSeconds(0),
        mimeType = mimeType,
        parentId = "root",
        webUrl = null,
    )
}

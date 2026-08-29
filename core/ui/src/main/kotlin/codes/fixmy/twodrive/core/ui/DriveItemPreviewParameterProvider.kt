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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.ui.PreviewParameterData.driveItems
import kotlinx.datetime.Instant

/**
 * This [PreviewParameterProvider](https://developer.android.com/reference/kotlin/androidx/compose/ui/tooling/preview/PreviewParameterProvider)
 * provides list of [DriveItem] for Composable previews.
 */
class DriveItemPreviewParameterProvider : PreviewParameterProvider<List<DriveItem>> {
    override val values: Sequence<List<DriveItem>> = sequenceOf(driveItems)
}

object PreviewParameterData {
    val driveItems = listOf(
        DriveItem(
            id = "f-documents",
            name = "Documents",
            isFolder = true,
            size = 0,
            lastModified = Instant.parse("2026-08-18T14:03:00Z"),
            mimeType = null,
            parentId = "root",
            webUrl = null,
            childCount = 5,
        ),
        DriveItem(
            id = "f-pictures",
            name = "Pictures",
            isFolder = true,
            size = 0,
            lastModified = Instant.parse("2026-08-19T20:41:00Z"),
            mimeType = null,
            parentId = "root",
            webUrl = null,
            childCount = 4,
        ),
        DriveItem(
            id = "i-resume",
            name = "Resume 2026.docx",
            isFolder = false,
            size = 48_213,
            lastModified = Instant.parse("2026-08-20T09:12:00Z"),
            mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            parentId = "root",
            webUrl = null,
        ),
        DriveItem(
            id = "i-beach",
            name = "Beach sunset.jpg",
            isFolder = false,
            size = 3_402_118,
            lastModified = Instant.parse("2026-07-04T19:55:00Z"),
            mimeType = "image/jpeg",
            parentId = "root",
            webUrl = null,
        ),
        DriveItem(
            id = "i-lease",
            name = "Apartment lease.pdf",
            isFolder = false,
            size = 2_301_004,
            lastModified = Instant.parse("2026-03-01T16:20:00Z"),
            mimeType = "application/pdf",
            parentId = "root",
            webUrl = null,
        ),
    )
}

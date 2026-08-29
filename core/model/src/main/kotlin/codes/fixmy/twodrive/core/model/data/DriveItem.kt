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

package codes.fixmy.twodrive.core.model.data

import kotlinx.datetime.Instant

/**
 * A file or folder in the user's OneDrive, as exposed by the Microsoft Graph `driveItem` resource.
 */
data class DriveItem(
    val id: String,
    val name: String,
    val isFolder: Boolean,
    val isRoot: Boolean = false,
    val size: Long,
    val lastModified: Instant,
    val mimeType: String?,
    val parentId: String?,
    val webUrl: String?,
    val thumbnailUrl: String? = null,
    val childCount: Int? = null,
)

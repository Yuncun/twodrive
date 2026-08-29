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

package codes.fixmy.twodrive.core.data.model

import codes.fixmy.twodrive.core.database.model.DriveItemEntity
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import kotlinx.datetime.Instant

fun NetworkDriveItem.asEntity() = DriveItemEntity(
    id = id,
    name = name,
    isFolder = isFolder,
    isRoot = isRoot,
    size = size,
    lastModified = Instant.parse(lastModifiedDateTime),
    mimeType = file?.mimeType,
    parentId = parentReference?.id,
    webUrl = webUrl,
    thumbnailUrl = null,
    childCount = folder?.childCount,
)

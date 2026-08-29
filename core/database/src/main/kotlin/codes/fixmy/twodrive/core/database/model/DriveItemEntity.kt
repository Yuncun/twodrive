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

package codes.fixmy.twodrive.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.datetime.Instant

/**
 * Defines a cached OneDrive file or folder. The local table is the source of truth for the UI and
 * is kept current by delta sync.
 */
@Entity(
    tableName = "drive_items",
    indices = [Index(value = ["parent_id"])],
)
data class DriveItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "is_folder")
    val isFolder: Boolean,
    @ColumnInfo(name = "is_root")
    val isRoot: Boolean,
    val size: Long,
    @ColumnInfo(name = "last_modified")
    val lastModified: Instant,
    @ColumnInfo(name = "mime_type")
    val mimeType: String?,
    @ColumnInfo(name = "parent_id")
    val parentId: String?,
    @ColumnInfo(name = "web_url")
    val webUrl: String?,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    @ColumnInfo(name = "child_count")
    val childCount: Int?,
)

fun DriveItemEntity.asExternalModel() = DriveItem(
    id = id,
    name = name,
    isFolder = isFolder,
    isRoot = isRoot,
    size = size,
    lastModified = lastModified,
    mimeType = mimeType,
    parentId = parentId,
    webUrl = webUrl,
    thumbnailUrl = thumbnailUrl,
    childCount = childCount,
)

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

package codes.fixmy.twodrive.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Network representation of a Microsoft Graph
 * [driveItem](https://learn.microsoft.com/graph/api/resources/driveitem).
 *
 * Facets (`file`, `folder`, `root`, `deleted`) are present only when they apply, which is how
 * Graph tells a file from a folder.
 */
@Serializable
data class NetworkDriveItem(
    val id: String,
    val name: String,
    val size: Long = 0,
    val lastModifiedDateTime: String,
    val webUrl: String? = null,
    val file: NetworkFileFacet? = null,
    val folder: NetworkFolderFacet? = null,
    val root: JsonObject? = null,
    val deleted: NetworkDeletedFacet? = null,
    val parentReference: NetworkParentReference? = null,
) {
    val isFolder: Boolean get() = folder != null
    val isRoot: Boolean get() = root != null
    val isDeleted: Boolean get() = deleted != null
}

@Serializable
data class NetworkFileFacet(
    val mimeType: String? = null,
)

@Serializable
data class NetworkFolderFacet(
    val childCount: Int? = null,
)

@Serializable
data class NetworkDeletedFacet(
    val state: String? = null,
)

@Serializable
data class NetworkParentReference(
    val id: String? = null,
    val driveId: String? = null,
    val path: String? = null,
)

/**
 * One page of a delta or children query.
 */
@Serializable
data class NetworkDriveItemPage(
    val value: List<NetworkDriveItem>,
    @SerialName("@odata.nextLink") val nextLink: String? = null,
    @SerialName("@odata.deltaLink") val deltaLink: String? = null,
)

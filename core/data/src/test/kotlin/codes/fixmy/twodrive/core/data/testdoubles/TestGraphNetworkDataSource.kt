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

package codes.fixmy.twodrive.core.data.testdoubles

import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.model.NetworkContent
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkFolderFacet
import codes.fixmy.twodrive.core.network.model.NetworkParentReference
import codes.fixmy.twodrive.core.network.model.NetworkThumbnailSet
import codes.fixmy.twodrive.core.network.model.NetworkUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Scripted [GraphNetworkDataSource]: pages are keyed by the link they are requested with; the
 * initial delta query uses the `null` key.
 */
class TestGraphNetworkDataSource : GraphNetworkDataSource {
    val pages = mutableMapOf<String?, NetworkDriveItemPage>()
    val requestedLinks = mutableListOf<String?>()
    var failDeltaWith: Int? = null
    var failDeltaWithException: Exception? = null

    override suspend fun getMe(): NetworkUser = NetworkUser(id = "u", userPrincipalName = "test@example.com")

    var drive: NetworkDrive = NetworkDrive(id = "d")
    var failDriveWith: Exception? = null

    override suspend fun getDrive(): NetworkDrive {
        failDriveWith?.let { throw it }
        return drive
    }

    override suspend fun getChildren(itemId: String?): NetworkDriveItemPage = NetworkDriveItemPage(emptyList())

    override suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage {
        requestedLinks += deltaLink
        failDeltaWithException?.let { e ->
            failDeltaWithException = null
            throw e
        }
        failDeltaWith?.let { code ->
            failDeltaWith = null
            throw HttpException(Response.error<Any>(code, "".toResponseBody("text/plain".toMediaType())))
        }
        return pages.getValue(deltaLink)
    }

    override suspend fun getPage(url: String): NetworkDriveItemPage {
        requestedLinks += url
        return pages.getValue(url)
    }

    override suspend fun getThumbnails(itemId: String): List<NetworkThumbnailSet> = emptyList()

    /** The (parentId, name) of every createFolder call, in order. */
    val createdFolders = mutableListOf<Pair<String?, String>>()
    var failCreateFolderWith: Exception? = null

    override suspend fun createFolder(parentId: String?, name: String): NetworkDriveItem {
        failCreateFolderWith?.let { throw it }
        createdFolders += parentId to name
        return NetworkDriveItem(
            id = "created-${createdFolders.size}",
            name = name,
            lastModifiedDateTime = "2026-08-01T00:00:00Z",
            folder = NetworkFolderFacet(childCount = 0),
            parentReference = NetworkParentReference(id = parentId ?: "root"),
        )
    }

    /** The id of every deleteItem call, in order. */
    val deletedItems = mutableListOf<String>()
    var failDeleteItemWith: Exception? = null

    override suspend fun deleteItem(itemId: String) {
        failDeleteItemWith?.let { throw it }
        deletedItems += itemId
    }

    /** File bytes by item id; a missing id fails like a download that cannot complete. */
    val contents = mutableMapOf<String, ByteArray>()
    val contentRequests = mutableListOf<String>()

    override suspend fun getContent(itemId: String): NetworkContent {
        contentRequests += itemId
        val bytes = contents[itemId] ?: throw IOException("No content for $itemId")
        return NetworkContent(length = bytes.size.toLong(), stream = bytes.inputStream())
    }
}

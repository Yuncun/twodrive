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
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkUser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

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

    override suspend fun getDrive(): NetworkDrive = NetworkDrive(id = "d")

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
}

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

package codes.fixmy.twodrive.core.network.retrofit

import androidx.tracing.trace
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.model.NetworkContent
import codes.fixmy.twodrive.core.network.model.NetworkCreateFolderRequest
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkFolderFacet
import codes.fixmy.twodrive.core.network.model.NetworkThumbnailSet
import codes.fixmy.twodrive.core.network.model.NetworkThumbnailSetPage
import codes.fixmy.twodrive.core.network.model.NetworkUser
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Retrofit API declaration for Microsoft Graph.
 */
internal interface RetrofitGraphApi {
    @GET("me")
    suspend fun getMe(): NetworkUser

    @GET("me/drive")
    suspend fun getDrive(): NetworkDrive

    @GET("me/drive/root/children")
    suspend fun getRootChildren(): NetworkDriveItemPage

    @GET("me/drive/items/{itemId}/children")
    suspend fun getChildren(@Path("itemId") itemId: String): NetworkDriveItemPage

    @GET("me/drive/root/delta")
    suspend fun getRootDelta(): NetworkDriveItemPage

    @GET
    suspend fun getPage(@Url url: String): NetworkDriveItemPage

    @GET("me/drive/items/{itemId}/thumbnails")
    suspend fun getThumbnails(@Path("itemId") itemId: String): NetworkThumbnailSetPage

    @POST("me/drive/root/children")
    suspend fun createRootFolder(@Body request: NetworkCreateFolderRequest): NetworkDriveItem

    @POST("me/drive/items/{itemId}/children")
    suspend fun createFolder(
        @Path("itemId") itemId: String,
        @Body request: NetworkCreateFolderRequest,
    ): NetworkDriveItem

    /** Answers 302 with a pre-authenticated download URL, when called without following redirects. */
    @Streaming
    @GET("me/drive/items/{itemId}/content")
    suspend fun getContent(@Path("itemId") itemId: String): Response<ResponseBody>

    @Streaming
    @GET
    suspend fun download(@Url url: String): ResponseBody
}

const val GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0/"

/**
 * Qualifier for the Graph base URL so tests can point the client at a local server.
 */
const val GRAPH_BASE_URL_NAME = "graphBaseUrl"

/**
 * Qualifier for the authenticated Graph client with redirects turned off, so a download's 302
 * is handed back instead of followed with the bearer token attached.
 */
const val GRAPH_NO_REDIRECT_CALL_FACTORY = "graphNoRedirectCallFactory"

/**
 * Qualifier for the client without a bearer token, for pre-authenticated URLs on other hosts.
 */
const val UNAUTHENTICATED_CALL_FACTORY = "unauthenticatedCallFactory"

/**
 * [Retrofit] backed [GraphNetworkDataSource].
 */
@Singleton
class RetrofitGraphNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
    @Named(GRAPH_NO_REDIRECT_CALL_FACTORY) noRedirectCallFactory: dagger.Lazy<Call.Factory>,
    @Named(UNAUTHENTICATED_CALL_FACTORY) unauthenticatedCallFactory: dagger.Lazy<Call.Factory>,
    @Named(GRAPH_BASE_URL_NAME) baseUrl: String,
) : GraphNetworkDataSource {

    private val networkApi = trace("RetrofitGraphNetwork") {
        api(networkJson, okhttpCallFactory, baseUrl)
    }

    private val noRedirectApi = api(networkJson, noRedirectCallFactory, baseUrl)

    private val unauthenticatedApi = api(networkJson, unauthenticatedCallFactory, baseUrl)

    override suspend fun getMe(): NetworkUser = networkApi.getMe()

    override suspend fun getDrive(): NetworkDrive = networkApi.getDrive()

    override suspend fun getChildren(itemId: String?): NetworkDriveItemPage =
        if (itemId == null) networkApi.getRootChildren() else networkApi.getChildren(itemId)

    override suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage =
        if (deltaLink == null) networkApi.getRootDelta() else networkApi.getPage(deltaLink)

    override suspend fun getPage(url: String): NetworkDriveItemPage = networkApi.getPage(url)

    override suspend fun getThumbnails(itemId: String): List<NetworkThumbnailSet> =
        networkApi.getThumbnails(itemId).value

    override suspend fun createFolder(parentId: String?, name: String): NetworkDriveItem {
        val request = NetworkCreateFolderRequest(
            name = name,
            folder = NetworkFolderFacet(),
            conflictBehavior = "rename",
        )
        return if (parentId == null) {
            networkApi.createRootFolder(request)
        } else {
            networkApi.createFolder(parentId, request)
        }
    }

    /**
     * Graph answers `/content` with a 302 to a pre-authenticated URL on another host, which is
     * downloaded without the bearer token. A 200 carries the bytes directly.
     */
    override suspend fun getContent(itemId: String): NetworkContent {
        val response = noRedirectApi.getContent(itemId)
        val location = response.headers()["Location"]
        val body = when {
            response.isSuccessful -> checkNotNull(response.body())
            response.code() in 300..399 && location != null -> {
                response.errorBody()?.close()
                unauthenticatedApi.download(location)
            }
            else -> throw HttpException(response)
        }
        return NetworkContent(length = body.contentLength(), stream = body.byteStream())
    }
}

private fun api(networkJson: Json, callFactory: dagger.Lazy<Call.Factory>, baseUrl: String): RetrofitGraphApi =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        // We use callFactory lambda here with dagger.Lazy<Call.Factory>
        // to prevent initializing OkHttp on the main thread.
        .callFactory { callFactory.get().newCall(it) }
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create(RetrofitGraphApi::class.java)

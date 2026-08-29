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
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkUser
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
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
}

const val GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0/"

/**
 * Qualifier for the Graph base URL so tests can point the client at a local server.
 */
const val GRAPH_BASE_URL_NAME = "graphBaseUrl"

/**
 * [Retrofit] backed [GraphNetworkDataSource].
 */
@Singleton
class RetrofitGraphNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
    @Named(GRAPH_BASE_URL_NAME) baseUrl: String,
) : GraphNetworkDataSource {

    private val networkApi = trace("RetrofitGraphNetwork") {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            // We use callFactory lambda here with dagger.Lazy<Call.Factory>
            // to prevent initializing OkHttp on the main thread.
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RetrofitGraphApi::class.java)
    }

    override suspend fun getMe(): NetworkUser = networkApi.getMe()

    override suspend fun getDrive(): NetworkDrive = networkApi.getDrive()

    override suspend fun getChildren(itemId: String?): NetworkDriveItemPage =
        if (itemId == null) networkApi.getRootChildren() else networkApi.getChildren(itemId)

    override suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage =
        if (deltaLink == null) networkApi.getRootDelta() else networkApi.getPage(deltaLink)

    override suspend fun getPage(url: String): NetworkDriveItemPage = networkApi.getPage(url)
}

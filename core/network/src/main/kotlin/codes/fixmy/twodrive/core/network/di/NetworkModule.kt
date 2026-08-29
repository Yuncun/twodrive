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

package codes.fixmy.twodrive.core.network.di

import android.content.Context
import androidx.tracing.trace
import codes.fixmy.twodrive.core.network.BuildConfig
import codes.fixmy.twodrive.core.network.demo.DemoAssetManager
import codes.fixmy.twodrive.core.network.retrofit.BearerTokenInterceptor
import codes.fixmy.twodrive.core.network.retrofit.GRAPH_BASE_URL
import codes.fixmy.twodrive.core.network.retrofit.GRAPH_BASE_URL_NAME
import coil.ImageLoader
import coil.util.DebugLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesDemoAssetManager(
        @ApplicationContext context: Context,
    ): DemoAssetManager = DemoAssetManager(context.assets::open)

    @Provides
    @Named(GRAPH_BASE_URL_NAME)
    fun providesGraphBaseUrl(): String = GRAPH_BASE_URL

    @Provides
    @Singleton
    fun okHttpCallFactory(
        bearerTokenInterceptor: BearerTokenInterceptor,
    ): Call.Factory = trace("TwoDriveOkHttpClient") {
        OkHttpClient.Builder()
            .addInterceptor(bearerTokenInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            // Headers carry the bearer token; log bodies only.
                            setLevel(HttpLoggingInterceptor.Level.BASIC)
                        }
                    },
            )
            .build()
    }

    /**
     * Coil calls `applicationContext.newImageLoader()` during initialisation to obtain an
     * ImageLoader; thumbnails are fetched through the same authenticated OkHttp client.
     *
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     */
    @Provides
    @Singleton
    fun imageLoader(
        // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger.
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context,
    ): ImageLoader = trace("TwoDriveImageLoader") {
        ImageLoader.Builder(application)
            .callFactory { okHttpCallFactory.get() }
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}

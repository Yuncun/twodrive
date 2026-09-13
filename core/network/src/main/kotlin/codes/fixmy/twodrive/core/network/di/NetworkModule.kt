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
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.demo.DemoAssetManager
import codes.fixmy.twodrive.core.network.retrofit.BearerTokenInterceptor
import codes.fixmy.twodrive.core.network.retrofit.GRAPH_BASE_URL
import codes.fixmy.twodrive.core.network.retrofit.GRAPH_BASE_URL_NAME
import codes.fixmy.twodrive.core.network.retrofit.GRAPH_NO_REDIRECT_CALL_FACTORY
import codes.fixmy.twodrive.core.network.retrofit.InsufficientStorageMonitor
import codes.fixmy.twodrive.core.network.retrofit.RetryAfterInterceptor
import codes.fixmy.twodrive.core.network.retrofit.UNAUTHENTICATED_CALL_FACTORY
import codes.fixmy.twodrive.core.network.thumbnail.DriveItemThumbnailFetcher
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
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
    fun okHttpClient(
        retryAfterInterceptor: RetryAfterInterceptor,
        bearerTokenInterceptor: BearerTokenInterceptor,
        insufficientStorageMonitor: InsufficientStorageMonitor,
    ): OkHttpClient = trace("TwoDriveOkHttpClient") {
        OkHttpClient.Builder()
            // Outermost, so each retry passes through the token interceptor again.
            .addInterceptor(retryAfterInterceptor)
            .addInterceptor(bearerTokenInterceptor)
            .addInterceptor(insufficientStorageMonitor)
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

    @Provides
    fun okHttpCallFactory(client: OkHttpClient): Call.Factory = client

    /** Shares the authenticated client's connection pool and interceptors. */
    @Provides
    @Singleton
    @Named(GRAPH_NO_REDIRECT_CALL_FACTORY)
    fun noRedirectCallFactory(client: OkHttpClient): Call.Factory =
        client.newBuilder().followRedirects(false).build()

    /** Downloads pre-authenticated URLs (thumbnails, file content) that must not see the token. */
    @Provides
    @Singleton
    @Named(UNAUTHENTICATED_CALL_FACTORY)
    fun unauthenticatedCallFactory(): Call.Factory = trace("TwoDriveUnauthenticatedOkHttpClient") {
        OkHttpClient()
    }

    /**
     * Coil calls `applicationContext.newImageLoader()` during initialisation to obtain an
     * ImageLoader. Drive items load their thumbnails through [DriveItemThumbnailFetcher]: the
     * Graph call that returns the thumbnail URL goes through the authenticated Retrofit client,
     * while the download itself uses a client without [BearerTokenInterceptor], because Graph's
     * thumbnail URLs are pre-authenticated links on other hosts that must not see the token.
     *
     * Graph serves thumbnails with headers that forbid caching; the URLs expire anyway and the
     * fetcher keys the disk cache by item and modification time, so cache headers are ignored.
     *
     * @see <a href="https://github.com/coil-kt/coil/blob/main/coil-singleton/src/main/java/coil/Coil.kt">Coil</a>
     */
    @Provides
    @Singleton
    fun imageLoader(
        // We specifically request dagger.Lazy here, so that it's not instantiated from Dagger.
        network: dagger.Lazy<GraphNetworkDataSource>,
        @ApplicationContext application: Context,
        @Named(UNAUTHENTICATED_CALL_FACTORY) unauthenticatedCallFactory: dagger.Lazy<Call.Factory>,
    ): ImageLoader = trace("TwoDriveImageLoader") {
        ImageLoader.Builder(application)
            .callFactory { unauthenticatedCallFactory.get() }
            .memoryCache {
                MemoryCache.Builder(application)
                    .maxSizePercent(IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(application.cacheDir.resolve(IMAGE_DISK_CACHE_DIR))
                    .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                    .build()
            }
            .respectCacheHeaders(false)
            .components {
                add(DriveItemThumbnailFetcher.Factory(network))
                add(DriveItemThumbnailFetcher.Keyer)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}

private const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
private const val IMAGE_DISK_CACHE_DIR = "image_cache"
private const val IMAGE_DISK_CACHE_BYTES = 128L * 1024 * 1024

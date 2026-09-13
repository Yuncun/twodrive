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

package codes.fixmy.twodrive.core.network.thumbnail

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Looper
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.network.retrofit.RetrofitGraphNetwork
import coil.ImageLoader
import coil.decode.DataSource
import coil.disk.DiskCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.SuccessResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Loads thumbnails through a real Coil [ImageLoader] and the real Retrofit client against
 * MockWebServer, which plays both Graph (docs/graph-fixtures/thumbnails.json) and the thumbnail
 * download host.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DriveItemThumbnailFetcherTest {

    private val server = MockWebServer()
    private val context = RuntimeEnvironment.getApplication()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var diskCache: DiskCache

    @Before
    fun setUp() {
        server.start()
        diskCache = DiskCache.Builder().directory(tmpFolder.newFolder("image_cache")).build()
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun downloadsTheRenditionForTheRequestedSizeThenServesItFromDiskWithoutCallingGraph() {
        server.enqueue(MockResponse().setBody(thumbnailsFixture()))
        server.enqueue(MockResponse().setBody(pngBody()))

        val first = load(newImageLoader(), beach, sizePx = 105)

        assertIs<SuccessResult>(first)
        assertEquals(DataSource.NETWORK, first.dataSource)
        assertEquals("/v1.0/me/drive/items/i-beach/thumbnails", server.takeRequest().path)
        // 105px is covered by the 176px medium rendition, not the 96px small one.
        assertEquals("/y4mMediumBeachSunsetRendition/Beach%20sunset.jpg?psid=1&width=176&height=132", server.takeRequest().path)

        // A fresh loader (empty memory cache) with Graph unreachable still has the thumbnail.
        server.shutdown()
        val second = load(newImageLoader(), beach, sizePx = 105)

        assertIs<SuccessResult>(second)
        assertEquals(DataSource.DISK, second.dataSource)
    }

    @Test
    fun anItemGraphCannotRenderFailsSoTheIconStays() {
        server.enqueue(MockResponse().setBody(fixture("thumbnails-none.json")))

        val result = load(newImageLoader(), beach, sizePx = 105)

        assertIs<ErrorResult>(result)
        assertIs<NoThumbnailException>(result.throwable)
    }

    @Test
    fun foldersNeverReachGraph() {
        val result = load(newImageLoader(), beach.copy(isFolder = true, mimeType = null), sizePx = 105)

        assertIs<ErrorResult>(result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun cacheKeysChangeWithTheFileAndTheRenditionButNotTheUrl() {
        val medium = DriveItemThumbnailFetcher.diskCacheKey(beach, ThumbnailSize.MEDIUM)

        assertEquals(medium, DriveItemThumbnailFetcher.diskCacheKey(beach.copy(name = "Renamed.jpg"), ThumbnailSize.MEDIUM))
        assertNotEquals(medium, DriveItemThumbnailFetcher.diskCacheKey(beach, ThumbnailSize.LARGE))
        assertNotEquals(
            medium,
            DriveItemThumbnailFetcher.diskCacheKey(beach.copy(lastModified = Instant.parse("2026-09-01T00:00:00Z")), ThumbnailSize.MEDIUM),
        )
    }

    @Test
    fun anUnreachableGraphWithNothingCachedIsAnError() {
        server.shutdown()

        val result = load(newImageLoader(), beach, sizePx = 105)

        assertIs<ErrorResult>(result)
        assertNull(diskCache.openSnapshot(DriveItemThumbnailFetcher.diskCacheKey(beach, ThumbnailSize.MEDIUM)))
    }

    /**
     * Coil's execute() hops onto the main thread, which under Robolectric is this test thread, so
     * the load runs in the background while the test pumps the main looper until it finishes.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun load(imageLoader: ImageLoader, item: DriveItem, sizePx: Int): ImageResult {
        val request = ImageRequest.Builder(context)
            .data(item)
            .size(sizePx)
            .allowHardware(false)
            .build()
        val result = GlobalScope.async(Dispatchers.IO) { imageLoader.execute(request) }
        while (!result.isCompleted) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(LOOPER_POLL_MILLIS)
        }
        @OptIn(ExperimentalCoroutinesApi::class)
        return result.getCompleted()
    }

    private fun newImageLoader(): ImageLoader {
        val client = OkHttpClient()
        val network = RetrofitGraphNetwork(
            networkJson = Json { ignoreUnknownKeys = true },
            okhttpCallFactory = { client },
            noRedirectCallFactory = { client },
            unauthenticatedCallFactory = { client },
            baseUrl = server.url("/v1.0/").toString(),
        )
        return ImageLoader.Builder(context)
            .callFactory(client)
            .diskCache(diskCache)
            .respectCacheHeaders(false)
            .components {
                add(DriveItemThumbnailFetcher.Factory { network })
                add(DriveItemThumbnailFetcher.Keyer)
            }
            .build()
    }

    private fun thumbnailsFixture(): String =
        fixture("thumbnails.json").replace("https://public.bn.files.1drv.com/", server.url("/").toString())

    private fun fixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "Missing fixture $name" }.readText()

    private fun pngBody(): Buffer {
        val bitmap = Bitmap.createBitmap(176, 132, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        val bytes = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return Buffer().write(bytes.toByteArray())
    }

    private val beach = DriveItem(
        id = "i-beach",
        name = "Beach sunset.jpg",
        isFolder = false,
        size = 2_539_520,
        lastModified = Instant.parse("2026-08-19T20:41:00Z"),
        mimeType = "image/jpeg",
        parentId = "f-pictures",
        webUrl = null,
    )
}

private const val LOOPER_POLL_MILLIS = 5L

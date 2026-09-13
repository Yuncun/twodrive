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

package codes.fixmy.twodrive.core.network.demo

import JvmUnitTestDemoAssetManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import codes.fixmy.twodrive.core.common.network.Dispatcher
import codes.fixmy.twodrive.core.common.network.TwoDriveDispatchers.IO
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkFolderFacet
import codes.fixmy.twodrive.core.network.model.NetworkParentReference
import codes.fixmy.twodrive.core.network.model.NetworkThumbnail
import codes.fixmy.twodrive.core.network.model.NetworkThumbnailSet
import codes.fixmy.twodrive.core.network.model.NetworkUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [GraphNetworkDataSource] implementation that serves a drive from local JSON assets, so the
 * demo flavor and JVM tests run without a Microsoft account or network. Folders created with
 * [createFolder] are appended in memory and last as long as the process; a singleton keeps every
 * caller looking at the same drive.
 */
@Singleton
class DemoGraphNetworkDataSource @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
) : GraphNetworkDataSource {

    private val createdItems = CopyOnWriteArrayList<NetworkDriveItem>()

    override suspend fun getMe(): NetworkUser = getDataFromJsonFile(ME_ASSET)

    override suspend fun getDrive(): NetworkDrive = getDataFromJsonFile(DRIVE_ASSET)

    override suspend fun getChildren(itemId: String?): NetworkDriveItemPage {
        val items = allItems()
        val parentId = itemId ?: items.first { it.isRoot }.id
        return NetworkDriveItemPage(value = items.filter { it.parentReference?.id == parentId })
    }

    override suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage {
        // Only createFolder changes the demo drive, and it hands the new folder straight to the
        // caller, so a delta query after the first one is always empty.
        val items = if (deltaLink == null) allItems() else emptyList()
        return NetworkDriveItemPage(value = items, deltaLink = DEMO_DELTA_LINK)
    }

    override suspend fun getPage(url: String): NetworkDriveItemPage =
        error("The demo drive fits in a single page; no continuation for $url")

    /**
     * Serves the placeholder PNG bundled as `thumbnails/<itemId>.png`, or no thumbnail when the
     * demo drive bundles none for the item. The one rendition stands in for all three sizes.
     */
    override suspend fun getThumbnails(itemId: String): List<NetworkThumbnailSet> =
        withContext(ioDispatcher) {
            val fileName = "$THUMBNAILS_DIR/$itemId.png"
            val (width, height) = try {
                assets.open(fileName).use(::pngDimensions)
            } catch (_: FileNotFoundException) {
                return@withContext emptyList()
            }
            val thumbnail = NetworkThumbnail(url = "$ANDROID_ASSET_URL$fileName", width = width, height = height)
            listOf(NetworkThumbnailSet(small = thumbnail, medium = thumbnail, large = thumbnail))
        }

    /** Appends the folder in memory, renaming it the way Graph's `rename` conflict behavior does. */
    override suspend fun createFolder(parentId: String?, name: String): NetworkDriveItem {
        val items = allItems()
        val parent = parentId ?: items.first { it.isRoot }.id
        val takenNames = items.filter { it.parentReference?.id == parent }.map { it.name }.toSet()
        val freeName = generateSequence(1) { it + 1 }
            .map { "$name $it" }
            .let { sequenceOf(name) + it }
            .first { it !in takenNames }
        val folder = NetworkDriveItem(
            id = "$CREATED_FOLDER_ID_PREFIX${createdItems.size + 1}",
            name = freeName,
            lastModifiedDateTime = Clock.System.now().toString(),
            folder = NetworkFolderFacet(childCount = 0),
            parentReference = NetworkParentReference(id = parent),
        )
        createdItems += folder
        return folder
    }

    /** Reads width and height from a PNG's IHDR chunk, which always follows the 8-byte signature. */
    private fun pngDimensions(input: InputStream): Pair<Int, Int> = DataInputStream(input).run {
        skipBytes(PNG_IHDR_WIDTH_OFFSET)
        readInt() to readInt()
    }

    private suspend fun allItems(): List<NetworkDriveItem> =
        getDataFromJsonFile<NetworkDriveItemPage>(ITEMS_ASSET).value + createdItems

    /**
     * Get data from the given JSON [fileName].
     */
    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified T> getDataFromJsonFile(fileName: String): T =
        withContext(ioDispatcher) {
            assets.open(fileName).use { inputStream ->
                if (SDK_INT <= M) {
                    /**
                     * On API 23 (M) and below we must use a workaround to avoid an exception being
                     * thrown during deserialization. See:
                     * https://github.com/Kotlin/kotlinx.serialization/issues/2457#issuecomment-1786923342
                     */
                    inputStream.bufferedReader().use(BufferedReader::readText)
                        .let(networkJson::decodeFromString)
                } else {
                    networkJson.decodeFromStream(inputStream)
                }
            }
        }

    companion object {
        private const val ME_ASSET = "me.json"
        private const val DRIVE_ASSET = "drive.json"
        private const val ITEMS_ASSET = "items.json"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val ANDROID_ASSET_URL = "file:///android_asset/"
        private const val PNG_IHDR_WIDTH_OFFSET = 16
        private const val CREATED_FOLDER_ID_PREFIX = "demo-created-folder-"
        const val DEMO_DELTA_LINK = "demo://delta/latest"
    }
}

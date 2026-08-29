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
import codes.fixmy.twodrive.core.network.model.NetworkUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.BufferedReader
import javax.inject.Inject

/**
 * [GraphNetworkDataSource] implementation that serves a static drive from local JSON assets, so
 * the demo flavor and JVM tests run without a Microsoft account or network.
 */
class DemoGraphNetworkDataSource @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json,
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager,
) : GraphNetworkDataSource {

    override suspend fun getMe(): NetworkUser = getDataFromJsonFile(ME_ASSET)

    override suspend fun getDrive(): NetworkDrive = getDataFromJsonFile(DRIVE_ASSET)

    override suspend fun getChildren(itemId: String?): NetworkDriveItemPage {
        val items = allItems()
        val parentId = itemId ?: items.first { it.isRoot }.id
        return NetworkDriveItemPage(value = items.filter { it.parentReference?.id == parentId })
    }

    override suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage {
        // The demo drive never changes, so a delta query after the first one is always empty.
        val items = if (deltaLink == null) allItems() else emptyList()
        return NetworkDriveItemPage(value = items, deltaLink = DEMO_DELTA_LINK)
    }

    override suspend fun getPage(url: String): NetworkDriveItemPage =
        error("The demo drive fits in a single page; no continuation for $url")

    private suspend fun allItems(): List<NetworkDriveItem> =
        getDataFromJsonFile<NetworkDriveItemPage>(ITEMS_ASSET).value

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
        const val DEMO_DELTA_LINK = "demo://delta/latest"
    }
}

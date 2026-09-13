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

package codes.fixmy.twodrive.core.data.repository

import codes.fixmy.twodrive.core.common.network.Dispatcher
import codes.fixmy.twodrive.core.common.network.TwoDriveDispatchers.IO
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/** Qualifier for the cache directory downloaded files are written to. */
const val CONTENT_CACHE_DIR = "contentCacheDir"

/**
 * [DriveItemContentRepository] that writes each file to
 * `<cacheDir>/<item id>/<last modified>/<name>`, so a file keeps its name (other apps show it and
 * guess types from it) and a newer version of the file never reuses a stale copy. Bytes go to a
 * temporary file first, so an interrupted download is never mistaken for a complete one.
 */
internal class CacheDriveItemContentRepository @Inject constructor(
    private val network: GraphNetworkDataSource,
    @Named(CONTENT_CACHE_DIR) private val cacheDir: File,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : DriveItemContentRepository {

    override fun download(item: DriveItem): Flow<ContentDownload> = flow {
        val itemDir = cacheDir.resolve(File(item.id).name)
        val target = itemDir.resolve(item.lastModified.toEpochMilliseconds().toString()).resolve(File(item.name).name)
        if (!target.exists()) {
            // Drop copies of older versions before writing the current one.
            itemDir.deleteRecursively()
            target.parentFile!!.mkdirs()
            val partial = File(target.path + PARTIAL_SUFFIX)
            try {
                network.getContent(item.id).use { content ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead = 0L
                        emit(ContentDownload.Progress(bytesRead, content.length))
                        while (true) {
                            val count = content.stream.read(buffer)
                            if (count < 0) break
                            currentCoroutineContext().ensureActive()
                            output.write(buffer, 0, count)
                            bytesRead += count
                            emit(ContentDownload.Progress(bytesRead, content.length))
                        }
                    }
                }
                check(partial.renameTo(target)) { "Could not move the download to $target" }
            } finally {
                partial.delete()
            }
        }
        emit(ContentDownload.Complete(target))
    }.flowOn(ioDispatcher)
}

private const val PARTIAL_SUFFIX = ".partial"

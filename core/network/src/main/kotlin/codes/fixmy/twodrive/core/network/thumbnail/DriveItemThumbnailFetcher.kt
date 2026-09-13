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

import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.network.GraphNetworkDataSource
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import coil.size.pxOrElse

/**
 * Loads a [DriveItem]'s thumbnail, so the UI can hand Coil the item itself
 * (`AsyncImage(model = item)`) instead of a URL.
 *
 * Graph hands out thumbnail URLs through `GET /me/drive/items/{id}/thumbnails`, and they are
 * short-lived, so neither a URL nor the call that produces it can key a cache. The image is
 * cached under [diskCacheKey] instead — the item id, its last modification and the rendition
 * size — which a re-uploaded file changes and nothing else does. A disk hit is served without
 * calling Graph at all, so cached thumbnails keep showing offline; on a miss the fetcher asks
 * Graph for the URL and hands the download to Coil's own fetcher for that URL (HTTP for the real
 * drive, `file:///android_asset` for the demo drive), which writes the disk cache under the same
 * key.
 */
class DriveItemThumbnailFetcher(
    private val item: DriveItem,
    private val options: Options,
    private val imageLoader: ImageLoader,
    private val network: GraphNetworkDataSource,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val size = ThumbnailSize.of(options)
        val cacheKey = diskCacheKey(item, size)
        if (options.diskCachePolicy.readEnabled) {
            imageLoader.diskCache?.let { diskCache ->
                diskCache.openSnapshot(cacheKey)?.let { snapshot ->
                    return SourceResult(
                        source = ImageSource(
                            file = snapshot.data,
                            fileSystem = diskCache.fileSystem,
                            diskCacheKey = cacheKey,
                            closeable = snapshot,
                        ),
                        mimeType = null,
                        dataSource = DataSource.DISK,
                    )
                }
            }
        }
        val url = network.getThumbnails(item.id)
            .firstNotNullOfOrNull { it.bestFor(size.px)?.url }
            ?: throw NoThumbnailException(item.id)
        val delegateOptions = options.copy(diskCacheKey = cacheKey)
        val mapped = imageLoader.components.map(url, delegateOptions)
        val (delegate) = checkNotNull(imageLoader.components.newFetcher(mapped, delegateOptions, imageLoader)) {
            "No Coil fetcher can download $mapped"
        }
        return delegate.fetch()
    }

    class Factory(
        private val network: dagger.Lazy<GraphNetworkDataSource>,
    ) : Fetcher.Factory<DriveItem> {
        override fun create(data: DriveItem, options: Options, imageLoader: ImageLoader): Fetcher? =
            if (data.isFolder) null else DriveItemThumbnailFetcher(data, options, imageLoader, network.get())
    }

    /** Memory-cache key: the same identity as the disk cache, so a changed file misses both. */
    object Keyer : coil.key.Keyer<DriveItem> {
        override fun key(data: DriveItem, options: Options): String =
            diskCacheKey(data, ThumbnailSize.of(options))
    }

    companion object {
        fun diskCacheKey(item: DriveItem, size: ThumbnailSize): String =
            "drive-item-thumbnail:${item.id}:${item.lastModified.toEpochMilliseconds()}:${size.name.lowercase()}"
    }
}

/**
 * Graph's three renditions, by the longest edge in pixels each is rendered to fit. Requests are
 * bucketed into them so a 40dp row and a 48dp row share one cached image.
 */
enum class ThumbnailSize(val px: Int) {
    SMALL(96),
    MEDIUM(176),
    LARGE(800),
    ;

    companion object {
        /** The smallest rendition that covers the size Coil is loading for. */
        fun of(options: Options): ThumbnailSize {
            val requested = maxOf(
                options.size.width.pxOrElse { LARGE.px },
                options.size.height.pxOrElse { LARGE.px },
            )
            return entries.firstOrNull { it.px >= requested } ?: LARGE
        }
    }
}

/** Graph has no rendition of the item (an audio file, or a type it cannot render). */
class NoThumbnailException(itemId: String) : Exception("No thumbnail for drive item $itemId")

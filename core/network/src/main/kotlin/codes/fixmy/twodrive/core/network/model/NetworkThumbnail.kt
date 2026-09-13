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

package codes.fixmy.twodrive.core.network.model

import kotlinx.serialization.Serializable

/**
 * The body of `GET /me/drive/items/{id}/thumbnails`: one
 * [thumbnailSet](https://learn.microsoft.com/graph/api/resources/thumbnailset) per rendition of
 * the item, empty when Graph cannot render the item (an audio file, a plain folder).
 */
@Serializable
data class NetworkThumbnailSetPage(
    val value: List<NetworkThumbnailSet> = emptyList(),
)

/**
 * Graph renders `small` to fit 96px on its longest edge, `medium` 176px and `large` 800px. Any
 * of the three may be missing.
 */
@Serializable
data class NetworkThumbnailSet(
    val id: String = "0",
    val small: NetworkThumbnail? = null,
    val medium: NetworkThumbnail? = null,
    val large: NetworkThumbnail? = null,
) {
    /**
     * The smallest rendition whose longest edge covers [sizePx], or the largest one available
     * when none does. Returns null when the set carries no usable URL.
     */
    fun bestFor(sizePx: Int): NetworkThumbnail? {
        val available = listOfNotNull(small, medium, large)
            .filter { it.url != null }
            .sortedBy { it.longestEdge }
        return available.firstOrNull { it.longestEdge >= sizePx } ?: available.lastOrNull()
    }
}

/**
 * One rendition. The [url] is a short-lived, pre-authenticated download link, so it must not be
 * used as a cache key.
 */
@Serializable
data class NetworkThumbnail(
    val url: String? = null,
    val width: Int = 0,
    val height: Int = 0,
) {
    val longestEdge: Int get() = maxOf(width, height)
}

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

package codes.fixmy.twodrive.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import codes.fixmy.twodrive.core.model.data.DriveItem
import coil.compose.AsyncImage

/**
 * The item's Graph thumbnail, cropped to fill [modifier]'s bounds, over [fallback] (the file-type
 * icon) which shows until the thumbnail has loaded and stays when there is none: a folder, a kind
 * Graph does not render, no network and no cached copy. Coil loads the [DriveItem] itself through
 * the app's image loader, which resolves and caches the thumbnail (core/network's
 * DriveItemThumbnailFetcher).
 */
@Composable
fun DriveItemThumbnail(
    item: DriveItem,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!item.kind().hasThumbnail) {
            fallback()
            return@Box
        }
        var loaded by remember(item) { mutableStateOf(false) }
        if (!loaded) fallback()
        AsyncImage(
            model = item,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onSuccess = { loaded = true },
            onError = { loaded = false },
            modifier = Modifier.matchParentSize(),
        )
    }
}

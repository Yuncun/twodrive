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

import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Downloads files so another app can open them.
 */
interface DriveItemContentRepository {
    /**
     * Downloads [item]'s content into the app's cache, emitting [ContentDownload.Progress] as bytes
     * arrive and [ContentDownload.Complete] last. A copy already downloaded for the item's current
     * version completes at once. Download failures are thrown from the flow.
     */
    fun download(item: DriveItem): Flow<ContentDownload>
}

sealed interface ContentDownload {
    /** [totalBytes] is -1 when the size is unknown. */
    data class Progress(val bytesRead: Long, val totalBytes: Long) : ContentDownload

    data class Complete(val file: File) : ContentDownload
}

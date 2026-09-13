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

package codes.fixmy.twodrive.core.testing.repository

import codes.fixmy.twodrive.core.data.repository.ContentDownload
import codes.fixmy.twodrive.core.data.repository.DriveItemContentRepository
import codes.fixmy.twodrive.core.model.data.DriveItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * [DriveItemContentRepository] whose download events are sent by the test with [send] and ended
 * with [finish].
 */
class TestDriveItemContentRepository : DriveItemContentRepository {

    private var events = Channel<ContentDownload>(Channel.UNLIMITED)

    val requested = mutableListOf<DriveItem>()

    override fun download(item: DriveItem): Flow<ContentDownload> {
        requested += item
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun send(event: ContentDownload) {
        events.trySend(event)
    }

    /** Ends the current download, failing it with [cause] when given. */
    fun finish(cause: Throwable? = null) {
        events.close(cause)
    }
}

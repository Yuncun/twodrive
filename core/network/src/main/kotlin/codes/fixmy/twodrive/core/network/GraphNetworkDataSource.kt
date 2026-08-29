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

package codes.fixmy.twodrive.core.network

import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkUser

/**
 * Interface representing the Microsoft Graph calls the app makes.
 */
interface GraphNetworkDataSource {
    suspend fun getMe(): NetworkUser

    suspend fun getDrive(): NetworkDrive

    /**
     * Children of [itemId], or of the drive root when [itemId] is null. Callers follow
     * [NetworkDriveItemPage.nextLink] with [getPage] until it is null.
     */
    suspend fun getChildren(itemId: String?): NetworkDriveItemPage

    /**
     * Starts a delta query from [deltaLink], or a full enumeration when it is null.
     */
    suspend fun getDelta(deltaLink: String?): NetworkDriveItemPage

    /**
     * Fetches a continuation page given by an `@odata.nextLink`.
     */
    suspend fun getPage(url: String): NetworkDriveItemPage
}

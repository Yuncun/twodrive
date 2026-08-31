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

package codes.fixmy.twodrive.feature.files.impl

import codes.fixmy.twodrive.core.data.model.asEntity
import codes.fixmy.twodrive.core.database.model.asExternalModel
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.sortedBy
import codes.fixmy.twodrive.core.network.demo.DemoGraphNetworkDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.Json

/**
 * The children of [folderId] in the drive the demo flavor ships (core/network demo assets), read
 * through the same data source the running demo app uses so the screenshots show real demo data
 * rather than a second, hand-written copy of it, and sorted by [sortOrder] the same way
 * [FilesViewModel] sorts before rendering.
 */
internal fun demoDriveChildren(
    folderId: String? = null,
    sortOrder: SortOrder = SortOrder.NAME_ASCENDING,
): List<DriveItem> = runBlocking {
    DemoGraphNetworkDataSource(
        ioDispatcher = UnconfinedTestDispatcher(),
        networkJson = Json { ignoreUnknownKeys = true },
    ).getChildren(folderId).value
        .map { it.asEntity().asExternalModel() }
        .sortedBy(sortOrder)
}

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

import codes.fixmy.twodrive.core.data.repository.UserDataRepository
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.UserData
import codes.fixmy.twodrive.core.model.data.ViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull

val emptyUserData = UserData(sortOrder = SortOrder.NAME_ASCENDING, viewMode = ViewMode.LIST)

class TestUserDataRepository : UserDataRepository {
    /**
     * The backing hot flow for the list of followed topic ids for testing.
     */
    private val userDataFlow: MutableSharedFlow<UserData> =
        MutableSharedFlow(replay = 1)

    private val currentUserData
        get() = userDataFlow.replayCache.firstOrNull() ?: emptyUserData

    override val userData: Flow<UserData> = userDataFlow.filterNotNull()

    override suspend fun setSortOrder(sortOrder: SortOrder) {
        userDataFlow.tryEmit(currentUserData.copy(sortOrder = sortOrder))
    }

    override suspend fun setViewMode(viewMode: ViewMode) {
        userDataFlow.tryEmit(currentUserData.copy(viewMode = viewMode))
    }

    /**
     * A test-only API to allow setting the user data directly.
     */
    fun setUserData(userData: UserData) {
        userDataFlow.tryEmit(userData)
    }
}

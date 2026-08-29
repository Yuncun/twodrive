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

package codes.fixmy.twodrive.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Preferences that survive process death: how the user likes folders sorted, and the Graph
 * delta link that lets the next sync fetch only what changed.
 */
class TwoDrivePreferencesDataSource @Inject constructor(
    private val preferences: DataStore<Preferences>,
) {
    val userData: Flow<UserData> = preferences.data.map { prefs ->
        UserData(
            sortOrder = prefs[SORT_ORDER]?.let { stored ->
                SortOrder.entries.firstOrNull { it.name == stored }
            } ?: SortOrder.NAME_ASCENDING,
        )
    }

    suspend fun setSortOrder(sortOrder: SortOrder) {
        preferences.edit { it[SORT_ORDER] = sortOrder.name }
    }

    suspend fun getDeltaLink(): String? = preferences.data.first()[DELTA_LINK]

    suspend fun setDeltaLink(deltaLink: String?) {
        preferences.edit { prefs ->
            if (deltaLink == null) prefs.remove(DELTA_LINK) else prefs[DELTA_LINK] = deltaLink
        }
    }

    private companion object {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val DELTA_LINK = stringPreferencesKey("delta_link")
    }
}

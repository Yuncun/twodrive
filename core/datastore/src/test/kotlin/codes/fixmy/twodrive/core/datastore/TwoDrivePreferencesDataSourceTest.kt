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

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import codes.fixmy.twodrive.core.model.data.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TwoDrivePreferencesDataSourceTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: TwoDrivePreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        subject = TwoDrivePreferencesDataSource(
            PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) {
                tmpFolder.newFile("user_preferences_test.preferences_pb")
            },
        )
    }

    @Test
    fun sortOrderDefaultsToNameAscending() = testScope.runTest {
        assertEquals(SortOrder.NAME_ASCENDING, subject.userData.first().sortOrder)
    }

    @Test
    fun sortOrderIsPersisted() = testScope.runTest {
        subject.setSortOrder(SortOrder.MODIFIED_NEWEST_FIRST)
        assertEquals(SortOrder.MODIFIED_NEWEST_FIRST, subject.userData.first().sortOrder)
    }

    @Test
    fun deltaLinkCanBeSetAndCleared() = testScope.runTest {
        assertNull(subject.getDeltaLink())
        subject.setDeltaLink("https://graph.microsoft.com/v1.0/me/drive/root/delta?token=abc")
        assertEquals("https://graph.microsoft.com/v1.0/me/drive/root/delta?token=abc", subject.getDeltaLink())
        subject.setDeltaLink(null)
        assertNull(subject.getDeltaLink())
    }
}

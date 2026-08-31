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

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import codes.fixmy.twodrive.core.data.testdoubles.TestDriveItemDao
import codes.fixmy.twodrive.core.data.testdoubles.TestGraphNetworkDataSource
import codes.fixmy.twodrive.core.datastore.TwoDrivePreferencesDataSource
import codes.fixmy.twodrive.core.network.model.NetworkDeletedFacet
import codes.fixmy.twodrive.core.network.model.NetworkDriveItem
import codes.fixmy.twodrive.core.network.model.NetworkDriveItemPage
import codes.fixmy.twodrive.core.network.model.NetworkFileFacet
import codes.fixmy.twodrive.core.network.model.NetworkFolderFacet
import codes.fixmy.twodrive.core.network.model.NetworkParentReference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineFirstDriveItemsRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var subject: OfflineFirstDriveItemsRepository
    private lateinit var dao: TestDriveItemDao
    private lateinit var network: TestGraphNetworkDataSource
    private lateinit var preferences: TwoDrivePreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        dao = TestDriveItemDao()
        network = TestGraphNetworkDataSource()
        preferences = TwoDrivePreferencesDataSource(
            PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) {
                tmpFolder.newFile("user_preferences_test.preferences_pb")
            },
        )
        subject = OfflineFirstDriveItemsRepository(
            driveItemDao = dao,
            network = network,
            preferences = preferences,
        )
    }

    @Test
    fun syncFollowsNextLinksAndStoresDeltaLink() = testScope.runTest {
        network.pages[null] = NetworkDriveItemPage(
            value = listOf(root(), folder("docs", "Documents")),
            nextLink = "next-1",
        )
        network.pages["next-1"] = NetworkDriveItemPage(
            value = listOf(file("a", "a.txt", parent = "docs")),
            deltaLink = "delta-1",
        )

        assertTrue(subject.sync())

        assertEquals(listOf(null, "next-1"), network.requestedLinks)
        assertEquals("delta-1", preferences.getDeltaLink())
        assertEquals(listOf("Documents"), subject.getChildren(null).first().map { it.name })
        assertEquals(listOf("a.txt"), subject.getChildren("docs").first().map { it.name })
    }

    @Test
    fun syncResumesFromStoredDeltaLinkAndAppliesDeletes() = testScope.runTest {
        network.pages[null] = NetworkDriveItemPage(
            value = listOf(root(), file("a", "a.txt"), file("b", "b.txt")),
            deltaLink = "delta-1",
        )
        subject.sync()
        network.pages["delta-1"] = NetworkDriveItemPage(
            value = listOf(deleted("a"), file("b", "b renamed.txt")),
            deltaLink = "delta-2",
        )

        subject.sync()

        assertEquals(listOf(null, "delta-1"), network.requestedLinks)
        assertEquals(listOf("b renamed.txt"), subject.getChildren(null).first().map { it.name })
        assertEquals("delta-2", preferences.getDeltaLink())
    }

    @Test
    fun expiredDeltaTokenTriggersFullResync() = testScope.runTest {
        network.pages[null] = NetworkDriveItemPage(value = listOf(root(), file("stale", "stale.txt")), deltaLink = "delta-1")
        subject.sync()
        network.pages[null] = NetworkDriveItemPage(value = listOf(root(), file("fresh", "fresh.txt")), deltaLink = "delta-9")
        network.failDeltaWith = 410

        subject.sync()

        assertEquals(listOf(null, "delta-1", null), network.requestedLinks)
        assertEquals(listOf("fresh.txt"), subject.getChildren(null).first().map { it.name })
        assertEquals("delta-9", preferences.getDeltaLink())
    }

    @Test
    fun deletedFolderInTheDeltaSweepsItsCachedDescendants() = testScope.runTest {
        network.pages[null] = NetworkDriveItemPage(
            value = listOf(root(), folder("docs", "Documents"), file("a", "a.txt", parent = "docs")),
            deltaLink = "delta-1",
        )
        subject.sync()
        // Graph reports the deleted folder alone, without an entry per descendant.
        network.pages["delta-1"] = NetworkDriveItemPage(
            value = listOf(deleted("docs")),
            deltaLink = "delta-2",
        )

        subject.sync()

        assertEquals(emptyList(), subject.getChildren(null).first())
        assertEquals(null, subject.getDriveItem("a").first())
    }

    @Test
    fun syncReturnsFalseInsteadOfThrowingWhenTheNetworkIsDown() = testScope.runTest {
        network.pages[null] = NetworkDriveItemPage(value = listOf(root()), deltaLink = "delta-1")
        network.failDeltaWithException = IOException("no network")

        assertFalse(subject.sync())

        // The failed attempt must not consume the stored position; a retry starts clean.
        assertEquals(null, preferences.getDeltaLink())
        assertTrue(subject.sync())
        assertEquals("delta-1", preferences.getDeltaLink())
    }

    @Test
    fun syncReturnsFalseOnAServerErrorItCannotRecoverFrom() = testScope.runTest {
        network.failDeltaWith = 500

        assertFalse(subject.sync())
    }

    private fun root() = NetworkDriveItem(
        id = "root",
        name = "root",
        lastModifiedDateTime = "2026-08-01T00:00:00Z",
        folder = NetworkFolderFacet(childCount = 0),
        root = JsonObject(emptyMap()),
    )

    private fun folder(id: String, name: String, parent: String = "root") = NetworkDriveItem(
        id = id,
        name = name,
        lastModifiedDateTime = "2026-08-01T00:00:00Z",
        folder = NetworkFolderFacet(childCount = 0),
        parentReference = NetworkParentReference(id = parent),
    )

    private fun file(id: String, name: String, parent: String = "root") = NetworkDriveItem(
        id = id,
        name = name,
        size = 1,
        lastModifiedDateTime = "2026-08-01T00:00:00Z",
        file = NetworkFileFacet(mimeType = "text/plain"),
        parentReference = NetworkParentReference(id = parent),
    )

    private fun deleted(id: String) = NetworkDriveItem(
        id = id,
        name = id,
        lastModifiedDateTime = "2026-08-01T00:00:00Z",
        deleted = NetworkDeletedFacet(state = "deleted"),
        parentReference = NetworkParentReference(id = "root"),
    )
}

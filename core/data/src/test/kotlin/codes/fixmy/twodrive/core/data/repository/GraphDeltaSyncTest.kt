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
import codes.fixmy.twodrive.core.datastore.TwoDrivePreferencesDataSource
import codes.fixmy.twodrive.core.network.retrofit.RetrofitGraphNetwork
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drives [OfflineFirstDriveItemsRepository] end to end through the real Retrofit client against
 * MockWebServer, serving the Graph response bodies in docs/graph-fixtures/.
 */
class GraphDeltaSyncTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val server = MockWebServer()
    private lateinit var dao: TestDriveItemDao
    private lateinit var preferences: TwoDrivePreferencesDataSource
    private lateinit var subject: OfflineFirstDriveItemsRepository

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient()
        dao = TestDriveItemDao()
        preferences = TwoDrivePreferencesDataSource(
            PreferenceDataStoreFactory.create(scope = testScope.backgroundScope) {
                tmpFolder.newFile("delta_sync_test.preferences_pb")
            },
        )
        subject = OfflineFirstDriveItemsRepository(
            driveItemDao = dao,
            network = RetrofitGraphNetwork(
                networkJson = Json { ignoreUnknownKeys = true },
                okhttpCallFactory = { client },
                baseUrl = server.url("/v1.0/").toString(),
            ),
            preferences = preferences,
        )
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun fullEnumerationFollowsNextLinkThenStoresTheDeltaLink() = testScope.runTest {
        enqueueFixture("delta-initial-page-1.json")
        enqueueFixture("delta-initial-page-2.json")

        assertTrue(subject.sync())

        assertEquals("/v1.0/me/drive/root/delta", server.takeRequest().path)
        assertEquals("/v1.0/me/drive/root/delta(token='$PAGE_2_TOKEN')", server.takeRequest().path)
        assertEquals(localUrl("delta(token='$DELTA_A_TOKEN')"), preferences.getDeltaLink())
        assertEquals(
            listOf("Budget.xlsx", "Documents", "Pictures"),
            subject.getChildren(null).first().map { it.name }.sorted(),
        )
        assertEquals(
            listOf("Archive", "Resume.docx"),
            subject.getChildren(id(102)).first().map { it.name }.sorted(),
        )
        assertEquals(listOf("notes.txt"), subject.getChildren(id(106)).first().map { it.name })
        val beach = subject.getDriveItem(id(105)).first()!!
        assertEquals("image/jpeg", beach.mimeType)
        assertEquals(2_539_520, beach.size)
    }

    @Test
    fun incrementalSyncAppliesRenamesMovesAndDeletesFromTheStoredDeltaLink() = testScope.runTest {
        enqueueFixture("delta-initial-page-1.json")
        enqueueFixture("delta-initial-page-2.json")
        subject.sync()
        server.takeRequest()
        server.takeRequest()
        enqueueFixture("delta-incremental.json")

        assertTrue(subject.sync())

        assertEquals("/v1.0/me/drive/root/delta(token='$DELTA_A_TOKEN')", server.takeRequest().path)
        assertEquals(localUrl("delta(token='$DELTA_B_TOKEN')"), preferences.getDeltaLink())
        assertEquals(
            listOf("Documents", "Pictures"),
            subject.getChildren(null).first().map { it.name }.sorted(),
        )
        assertEquals(
            listOf("Resume 2026.docx", "Taxes 2025.pdf", "beach.jpg"),
            subject.getChildren(id(102)).first().map { it.name }.sorted(),
        )
        assertEquals(emptyList(), subject.getChildren(id(103)).first())
        // The deleted Archive folder was reported alone; its cached child goes with it.
        assertNull(subject.getDriveItem(id(106)).first())
        assertNull(subject.getDriveItem(id(107)).first())
        // The deleted file entry carried no name or timestamps and still parsed.
        assertNull(subject.getDriveItem(id(108)).first())
    }

    @Test
    fun expiredDeltaLinkResyncsAndSweepsItemsTheEnumerationNoLongerLists() = testScope.runTest {
        enqueueFixture("delta-initial-page-1.json")
        enqueueFixture("delta-initial-page-2.json")
        subject.sync()
        server.takeRequest()
        server.takeRequest()
        enqueueFixture("error-410-resync-required.json", code = 410)
        // The drive now holds only what page 1 lists.
        enqueueFixture("delta-initial-page-1.json")
        server.enqueue(
            MockResponse().setBody(
                """{"value": [], "@odata.deltaLink": "${localUrl("delta(token='$DELTA_B_TOKEN')")}"}""",
            ),
        )

        assertTrue(subject.sync())

        assertEquals("/v1.0/me/drive/root/delta(token='$DELTA_A_TOKEN')", server.takeRequest().path)
        assertEquals("/v1.0/me/drive/root/delta", server.takeRequest().path)
        assertEquals(
            listOf(id(101), id(102), id(103), id(104)),
            dao.getAllIds().sorted(),
        )
        assertEquals(localUrl("delta(token='$DELTA_B_TOKEN')"), preferences.getDeltaLink())
    }

    @Test
    fun serverErrorReturnsFalseAndKeepsTheCacheAndDeltaLink() = testScope.runTest {
        enqueueFixture("delta-initial-page-1.json")
        enqueueFixture("delta-initial-page-2.json")
        subject.sync()
        val cached = dao.getAllIds().sorted()
        val deltaLink = preferences.getDeltaLink()
        enqueueFixture("error-503-service-unavailable.json", code = 503)

        assertFalse(subject.sync())

        assertEquals(cached, dao.getAllIds().sorted())
        assertEquals(deltaLink, preferences.getDeltaLink())
    }

    @Test
    fun connectionDroppedMidEnumerationStoresNoDeltaLinkAndARetryCompletes() = testScope.runTest {
        enqueueFixture("delta-initial-page-1.json")
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertFalse(subject.sync())
        assertNull(preferences.getDeltaLink())

        enqueueFixture("delta-initial-page-1.json")
        enqueueFixture("delta-initial-page-2.json")
        assertTrue(subject.sync())
        assertEquals(localUrl("delta(token='$DELTA_A_TOKEN')"), preferences.getDeltaLink())
        assertEquals(8, dao.getAllIds().size)
    }

    private fun enqueueFixture(name: String, code: Int = 200) {
        val body = javaClass.classLoader!!.getResource(name)!!.readText()
            .replace(GRAPH_V1, server.url("/v1.0/").toString())
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(body),
        )
    }

    private fun localUrl(deltaPath: String) =
        server.url("/v1.0/").toString() + "me/drive/root/$deltaPath"

    private fun id(n: Int) = "5C6E1F2A3B4D5E6F!$n"

    private companion object {
        const val GRAPH_V1 = "https://graph.microsoft.com/v1.0/"
        const val PAGE_2_TOKEN = "aTE09NjM4OTE0MjU2MDAwMDAwMDAwO1NWMjtwYWdlMg"
        const val DELTA_A_TOKEN = "aTE09NjM4OTE0MjU2MDAwMDAwMDAwO1NWMjtkZWx0YUE"
        const val DELTA_B_TOKEN = "aTE09NjM4OTE0MzQ3MjAwMDAwMDAwO1NWMjtkZWx0YUI"
    }
}

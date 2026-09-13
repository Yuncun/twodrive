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

import codes.fixmy.twodrive.core.data.testdoubles.TestGraphNetworkDataSource
import codes.fixmy.twodrive.core.model.data.Drive
import codes.fixmy.twodrive.core.network.model.NetworkDrive
import codes.fixmy.twodrive.core.network.model.NetworkQuota
import codes.fixmy.twodrive.core.network.retrofit.InsufficientStorageMonitor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetworkDriveRepositoryTest {

    private val network = TestGraphNetworkDataSource()
    private val monitor = InsufficientStorageMonitor()
    private val subject = NetworkDriveRepository(network, monitor)

    private fun quota(used: Long, total: Long, state: String = "normal") =
        NetworkDrive(id = "d", quota = NetworkQuota(used = used, total = total, state = state))

    @Test
    fun driveIsNullBeforeTheFirstRefresh() = runTest {
        assertNull(subject.drive.first())
    }

    @Test
    fun refreshReadsTheQuota() = runTest {
        network.drive = quota(used = 40, total = 100)

        assertTrue(subject.refresh())

        assertEquals(Drive(id = "d", quotaUsed = 40, quotaTotal = 100, isQuotaFull = false), subject.drive.first())
        assertEquals(0.4f, subject.drive.first()!!.quotaFraction)
    }

    @Test
    fun quotaIsFullWhenUsedReachesTotal() = runTest {
        network.drive = quota(used = 107_696_304_947, total = 107_374_182_400)

        subject.refresh()

        val drive = subject.drive.first()!!
        assertTrue(drive.isQuotaFull)
        assertEquals(1f, drive.quotaFraction)
    }

    @Test
    fun quotaIsFullWhenGraphReportsExceeded() = runTest {
        network.drive = quota(used = 10, total = 100, state = "exceeded")

        subject.refresh()

        assertTrue(subject.drive.first()!!.isQuotaFull)
    }

    @Test
    fun failedRefreshKeepsTheLastDrive() = runTest {
        network.drive = quota(used = 40, total = 100)
        subject.refresh()
        network.failDriveWith = IOException("offline")

        assertFalse(subject.refresh())

        assertEquals(40, subject.drive.first()!!.quotaUsed)
    }

    /** A 507 on any Graph call, e.g. an upload, turns the drawer's quota red without a re-read. */
    @Test
    fun insufficientStorageResponseMarksTheQuotaFull() = runTest {
        network.drive = quota(used = 40, total = 100)
        subject.refresh()

        monitor.intercept507()

        assertTrue(subject.drive.first()!!.isQuotaFull)
        assertEquals(40, subject.drive.first()!!.quotaUsed)
    }

    @Test
    fun refreshShowingFreeSpaceClearsAnEarlierInsufficientStorage() = runTest {
        network.drive = quota(used = 40, total = 100)
        subject.refresh()
        monitor.intercept507()

        subject.refresh()

        assertFalse(subject.drive.first()!!.isQuotaFull)
    }

    private fun InsufficientStorageMonitor.intercept507() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(507))
        server.start()
        OkHttpClient.Builder().addInterceptor(this).build()
            .newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        server.shutdown()
        assertTrue(isStorageFull.value)
    }
}

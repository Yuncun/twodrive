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

package codes.fixmy.twodrive.core.network.retrofit

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InsufficientStorageMonitorTest {

    private val server = MockWebServer()
    private val monitor = InsufficientStorageMonitor()
    private val client = OkHttpClient.Builder().addInterceptor(monitor).build()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    private fun call() {
        client.newCall(Request.Builder().url(server.url("/v1.0/me/drive")).build()).execute().close()
    }

    @Test
    fun successfulResponsesLeaveStorageNotFull() {
        server.enqueue(MockResponse().setResponseCode(200))
        server.enqueue(MockResponse().setResponseCode(500))

        call()
        call()

        assertFalse(monitor.isStorageFull.value)
    }

    @Test
    fun insufficientStorageMarksStorageFullUntilCleared() {
        server.enqueue(MockResponse().setResponseCode(507))
        server.enqueue(MockResponse().setResponseCode(200))

        call()
        assertTrue(monitor.isStorageFull.value)

        call()
        assertTrue(monitor.isStorageFull.value)

        monitor.clear()
        assertFalse(monitor.isStorageFull.value)
    }
}

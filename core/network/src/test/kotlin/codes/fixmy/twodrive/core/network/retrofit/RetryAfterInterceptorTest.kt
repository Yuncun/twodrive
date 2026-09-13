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
import kotlin.test.assertEquals

class RetryAfterInterceptorTest {

    private val server = MockWebServer()
    private val sleeps = mutableListOf<Long>()

    /** Fixed "now": Wed, 21 Oct 2026 07:28:00 GMT. */
    private val now = 1_792_567_680_000L

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    private fun client(maxRetries: Int = 3, maxDelayMillis: Long = 60_000) = OkHttpClient.Builder()
        .addInterceptor(
            RetryAfterInterceptor(
                maxRetries = maxRetries,
                defaultDelayMillis = 1_000,
                maxDelayMillis = maxDelayMillis,
                currentTimeMillis = { now },
                sleep = { sleeps += it },
            ),
        )
        .build()

    private fun get(client: OkHttpClient = client()) =
        client.newCall(Request.Builder().url(server.url("/v1.0/me/drive")).build()).execute()

    private fun throttled(retryAfter: String? = null) = MockResponse().setResponseCode(429).apply {
        if (retryAfter != null) setHeader("Retry-After", retryAfter)
    }

    @Test
    fun deltaSecondsIsHonouredThenRequestSucceeds() {
        server.enqueue(throttled("2"))
        server.enqueue(MockResponse().setBody("ok"))

        get().use { response ->
            assertEquals(200, response.code)
            assertEquals("ok", response.body?.string())
        }
        assertEquals(listOf(2_000L), sleeps)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun httpDateIsHonouredRelativeToNow() {
        server.enqueue(throttled("Wed, 21 Oct 2026 07:28:05 GMT"))
        server.enqueue(MockResponse())

        get().use { assertEquals(200, it.code) }

        assertEquals(listOf(5_000L), sleeps)
    }

    @Test
    fun httpDateInThePastRetriesImmediately() {
        server.enqueue(throttled("Wed, 21 Oct 2026 07:27:00 GMT"))
        server.enqueue(MockResponse())

        get().use { assertEquals(200, it.code) }

        assertEquals(listOf(0L), sleeps)
    }

    @Test
    fun missingOrUnparseableHeaderBacksOffExponentially() {
        server.enqueue(throttled())
        server.enqueue(throttled("soon"))
        server.enqueue(throttled())
        server.enqueue(MockResponse())

        get().use { assertEquals(200, it.code) }

        assertEquals(listOf(1_000L, 2_000L, 4_000L), sleeps)
    }

    @Test
    fun retriesAreBoundedAndTheLast429IsReturned() {
        repeat(4) { server.enqueue(throttled("1")) }
        server.enqueue(MockResponse())

        get(client(maxRetries = 3)).use { assertEquals(429, it.code) }

        assertEquals(4, server.requestCount)
        assertEquals(3, sleeps.size)
    }

    @Test
    fun waitLongerThanTheCapIsNotRetried() {
        server.enqueue(throttled("3600"))
        server.enqueue(MockResponse())

        get(client(maxDelayMillis = 60_000)).use { response ->
            assertEquals(429, response.code)
            assertEquals("3600", response.header("Retry-After"))
        }

        assertEquals(1, server.requestCount)
        assertEquals(emptyList(), sleeps)
    }

    @Test
    fun otherErrorsAreNotRetried() {
        server.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", "1"))

        get().use { assertEquals(503, it.code) }

        assertEquals(1, server.requestCount)
    }
}

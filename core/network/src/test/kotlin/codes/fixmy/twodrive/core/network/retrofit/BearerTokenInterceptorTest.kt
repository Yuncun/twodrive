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

import codes.fixmy.twodrive.core.auth.AccessTokenProvider
import codes.fixmy.twodrive.core.auth.AuthError
import codes.fixmy.twodrive.core.auth.AuthException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BearerTokenInterceptorTest {

    private val server = MockWebServer().apply { start() }
    private val tokens = FakeAccessTokenProvider()
    private val client = OkHttpClient.Builder()
        .addInterceptor(BearerTokenInterceptor(tokens))
        .build()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun addsBearerTokenToRequest() {
        server.enqueue(MockResponse().setBody("ok"))

        val response = get()

        assertEquals(200, response.code)
        assertEquals("Bearer token-1", server.takeRequest().getHeader("Authorization"))
        assertEquals(0, tokens.refreshCount)
    }

    @Test
    fun signedOutRequestIsSentWithoutAuthorizationHeader() {
        tokens.current = null
        server.enqueue(MockResponse().setBody("ok"))

        get()

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun unauthorizedRefreshesTokenAndRetriesOnce() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("ok"))

        val response = get()

        assertEquals(200, response.code)
        assertEquals("Bearer token-1", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer token-2", server.takeRequest().getHeader("Authorization"))
        assertEquals(1, tokens.refreshCount)
        assertFalse(tokens.signInRequired)
    }

    @Test
    fun unauthorizedAgainAfterRefreshRequiresSignIn() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get()

        assertEquals(401, response.code)
        assertEquals(2, server.requestCount)
        assertTrue(tokens.signInRequired)
    }

    @Test
    fun refreshNeedingInteractiveSignInReturnsUnauthorizedWithoutToken() {
        tokens.refreshed = null
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(401))

        val response = get()

        assertEquals(401, response.code)
        server.takeRequest()
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun authFailureSurfacesAsIOException() {
        tokens.failure = AuthException(AuthError.NO_NETWORK, "offline")

        val error = assertFailsWith<IOException> { get() }

        assertTrue(error.cause is AuthException)
        assertEquals(0, server.requestCount)
    }

    private fun get() = client.newCall(Request.Builder().url(server.url("/v1.0/me")).build())
        .execute()
        .also { it.close() }

    private class FakeAccessTokenProvider : AccessTokenProvider {
        var current: String? = "token-1"
        var refreshed: String? = "token-2"
        var failure: AuthException? = null
        var refreshCount = 0
        var signInRequired = false

        override suspend fun accessToken(): String? {
            failure?.let { throw it }
            return current
        }

        override suspend fun refreshAccessToken(): String? {
            refreshCount++
            current = refreshed
            return refreshed
        }

        override suspend fun requireSignIn() {
            signInRequired = true
            current = null
        }
    }
}

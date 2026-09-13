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
import codes.fixmy.twodrive.core.auth.AuthException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

/**
 * Adds the signed-in account's bearer token to every request. When Graph answers 401 the token
 * is refreshed and the request retried once; if that also fails the account is signed out so
 * the user signs in again.
 */
class BearerTokenInterceptor @Inject constructor(
    private val accessTokenProvider: AccessTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = auth { accessToken() } ?: return chain.proceed(chain.request())
        val response = chain.proceed(chain.request().withBearer(token))
        if (response.code != HTTP_UNAUTHORIZED) return response

        response.close()
        val refreshed = auth { refreshAccessToken() }
            ?: return chain.proceed(chain.request())
        val retry = chain.proceed(chain.request().withBearer(refreshed))
        if (retry.code == HTTP_UNAUTHORIZED) {
            auth { requireSignIn() }
        }
        return retry
    }

    /**
     * OkHttp only reports [IOException]s to the caller; anything else thrown from an interceptor
     * crashes the dispatcher thread, so auth failures are rethrown as I/O failures.
     */
    private fun <T> auth(block: suspend AccessTokenProvider.() -> T): T = try {
        runBlocking { accessTokenProvider.block() }
    } catch (e: AuthException) {
        throw IOException(e.message, e)
    }

    private fun Request.withBearer(token: String) =
        newBuilder().header("Authorization", "Bearer $token").build()
}

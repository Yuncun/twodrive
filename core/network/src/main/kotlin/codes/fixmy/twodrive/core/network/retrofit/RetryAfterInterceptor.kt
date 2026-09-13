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

import okhttp3.Interceptor
import okhttp3.Response
import java.io.InterruptedIOException
import javax.inject.Inject

/** HTTP 429 Too Many Requests, which Graph sends when it throttles an app. */
internal const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Retries requests that Graph throttles with 429, waiting as long as its `Retry-After` header
 * asks — either delta-seconds ("120") or an HTTP-date ("Wed, 21 Oct 2026 07:28:00 GMT") — per
 * https://learn.microsoft.com/graph/throttling. Without a usable header it backs off
 * exponentially from [defaultDelayMillis].
 *
 * Retries stop after [maxRetries], or as soon as the server asks for a wait longer than
 * [maxDelayMillis]; the last 429 is then returned to the caller unchanged. OkHttp interceptors
 * run on OkHttp's worker threads, so blocking while waiting is acceptable.
 */
class RetryAfterInterceptor internal constructor(
    private val maxRetries: Int,
    private val defaultDelayMillis: Long,
    private val maxDelayMillis: Long,
    private val currentTimeMillis: () -> Long,
    private val sleep: (Long) -> Unit,
) : Interceptor {

    @Inject
    constructor() : this(
        maxRetries = 3,
        defaultDelayMillis = 1_000,
        maxDelayMillis = 60_000,
        currentTimeMillis = System::currentTimeMillis,
        sleep = Thread::sleep,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var retries = 0
        while (response.code == HTTP_TOO_MANY_REQUESTS && retries < maxRetries) {
            // A one-shot body has already been consumed and cannot be sent again.
            if (request.body?.isOneShot() == true) return response
            val delayMillis = retryDelayMillis(response, attempt = retries)
            if (delayMillis > maxDelayMillis) return response
            response.close()
            try {
                sleep(delayMillis)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw InterruptedIOException("Interrupted while waiting to retry").apply { initCause(e) }
            }
            retries++
            response = chain.proceed(request)
        }
        return response
    }

    private fun retryDelayMillis(response: Response, attempt: Int): Long {
        val header = response.header("Retry-After")?.trim()
        header?.toLongOrNull()?.let { seconds -> return (seconds * 1_000).coerceAtLeast(0) }
        response.headers.getDate("Retry-After")?.let { date ->
            return (date.time - currentTimeMillis()).coerceAtLeast(0)
        }
        return defaultDelayMillis shl attempt
    }
}

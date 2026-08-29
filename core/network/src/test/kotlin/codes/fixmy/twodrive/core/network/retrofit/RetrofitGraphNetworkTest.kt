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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RetrofitGraphNetworkTest {

    private val server = MockWebServer()
    private lateinit var subject: RetrofitGraphNetwork

    @Before
    fun setUp() {
        server.start()
        val tokenProvider = AccessTokenProvider { "test-token" }
        val client = OkHttpClient.Builder()
            .addInterceptor(BearerTokenInterceptor(tokenProvider))
            .build()
        subject = RetrofitGraphNetwork(
            networkJson = Json { ignoreUnknownKeys = true },
            okhttpCallFactory = { client },
            baseUrl = server.url("/v1.0/").toString(),
        )
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun rootDeltaSendsBearerTokenAndParsesLinks() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "value": [
                    {"id": "root", "name": "root", "lastModifiedDateTime": "2026-08-01T00:00:00Z",
                     "folder": {"childCount": 1}, "root": {}},
                    {"id": "gone", "name": "old.txt", "lastModifiedDateTime": "2026-08-01T00:00:00Z",
                     "deleted": {"state": "deleted"}, "parentReference": {"id": "root"}}
                  ],
                  "@odata.nextLink": "${server.url("/v1.0/me/drive/root/delta?token=2")}"
                }
                """.trimIndent(),
            ),
        )

        val page = subject.getDelta(deltaLink = null)

        val request = server.takeRequest()
        assertEquals("/v1.0/me/drive/root/delta", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals(2, page.value.size)
        assertEquals(true, page.value[1].isDeleted)
        assertNull(page.deltaLink)
        assertEquals(server.url("/v1.0/me/drive/root/delta?token=2").toString(), page.nextLink)
    }

    @Test
    fun childrenOfItemUsesItemsPath() = runTest {
        server.enqueue(MockResponse().setBody("""{"value": []}"""))

        subject.getChildren("abc")

        assertEquals("/v1.0/me/drive/items/abc/children", server.takeRequest().path)
    }
}

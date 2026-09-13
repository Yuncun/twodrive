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
import retrofit2.HttpException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RetrofitGraphNetworkTest {

    private val server = MockWebServer()
    private lateinit var subject: RetrofitGraphNetwork

    @Before
    fun setUp() {
        server.start()
        val tokenProvider = object : AccessTokenProvider {
            override suspend fun accessToken() = "test-token"
            override suspend fun refreshAccessToken() = "test-token"
            override suspend fun requireSignIn() = Unit
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(BearerTokenInterceptor(tokenProvider))
            .build()
        subject = RetrofitGraphNetwork(
            networkJson = Json { ignoreUnknownKeys = true },
            okhttpCallFactory = { client },
            noRedirectCallFactory = { client.newBuilder().followRedirects(false).build() },
            unauthenticatedCallFactory = { OkHttpClient() },
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

    @Test
    fun thumbnailsUseTheItemsPathAndParseEveryRendition() = runTest {
        server.enqueue(MockResponse().setBody(fixture("thumbnails.json")))

        val sets = subject.getThumbnails("4E1A2B3C5D6F7A8B!105")

        assertEquals("/v1.0/me/drive/items/4E1A2B3C5D6F7A8B!105/thumbnails", server.takeRequest().path)
        val set = sets.single()
        assertEquals(96, set.small?.width)
        assertEquals(132, set.medium?.height)
        assertTrue(set.large!!.url!!.startsWith("https://public.bn.files.1drv.com/"))
    }

    @Test
    fun itemsGraphCannotRenderHaveNoThumbnails() = runTest {
        server.enqueue(MockResponse().setBody(fixture("thumbnails-none.json")))

        assertEquals(emptyList(), subject.getThumbnails("4E1A2B3C5D6F7A8B!110"))
    }

    @Test
    fun createFolderPostsNameFolderFacetAndRenameToTheParentsChildren() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """
                {"id": "new", "name": "Trips 1", "folder": {"childCount": 0},
                 "parentReference": {"id": "abc"}}
                """.trimIndent(),
            ),
        )

        val folder = subject.createFolder(parentId = "abc", name = "Trips")

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1.0/me/drive/items/abc/children", request.path)
        assertEquals(
            """{"name":"Trips","folder":{},"@microsoft.graph.conflictBehavior":"rename"}""",
            request.body.readUtf8(),
        )
        assertEquals("Trips 1", folder.name)
        assertTrue(folder.isFolder)
    }

    @Test
    fun createFolderAtTheRootUsesTheRootPath() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id": "new", "folder": {}}"""))

        subject.createFolder(parentId = null, name = "Trips")

        assertEquals("/v1.0/me/drive/root/children", server.takeRequest().path)
    }

    @Test
    fun contentFollowsTheRedirectWithoutTheBearerToken() = runTest {
        val downloadUrl = server.url("/download/abc?tempauth=xyz")
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", downloadUrl))
        server.enqueue(MockResponse().setBody("hello"))

        val content = subject.getContent("abc")

        val graphRequest = server.takeRequest()
        assertEquals("/v1.0/me/drive/items/abc/content", graphRequest.path)
        assertEquals("Bearer test-token", graphRequest.getHeader("Authorization"))
        val downloadRequest = server.takeRequest()
        assertEquals("/download/abc?tempauth=xyz", downloadRequest.path)
        assertNull(downloadRequest.getHeader("Authorization"))
        assertEquals(5, content.length)
        assertEquals("hello", content.use { it.stream.readBytes().decodeToString() })
    }

    @Test
    fun contentFailureIsAnHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val error = assertFailsWith<HttpException> { subject.getContent("gone") }

        assertEquals(404, error.code())
    }

    private fun fixture(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "Missing fixture $name" }.readText()
}

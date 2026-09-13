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

package codes.fixmy.twodrive.core.auth.msal

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class MsalConfigTest {

    private val template = File("src/prod/res/raw/msal_config.json").readText()

    @Test
    fun debugBuildGetsDebugRedirectUri() {
        val config = MsalConfig.resolve(template, "codes.fixmy.twodrive.debug")

        assertRedirectUri("msauth://codes.fixmy.twodrive.debug/J9AaPozhP6djyGWJSSzVi2DZpoU%3D", config)
    }

    @Test
    fun releaseBuildGetsReleaseRedirectUri() {
        val config = MsalConfig.resolve(template, "codes.fixmy.twodrive")

        assertRedirectUri("msauth://codes.fixmy.twodrive/J9AaPozhP6djyGWJSSzVi2DZpoU%3D", config)
        assertFalse(MsalConfig.APPLICATION_ID_PLACEHOLDER in config)
    }

    @Test
    fun templateWithoutPlaceholderIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            MsalConfig.resolve("""{"redirect_uri": "msauth://other/hash"}""", "codes.fixmy.twodrive")
        }
    }

    private fun assertRedirectUri(expected: String, config: String) {
        val redirectUri = Regex(""""redirect_uri"\s*:\s*"([^"]+)"""").find(config)?.groupValues?.get(1)
        assertEquals(expected, redirectUri)
    }
}

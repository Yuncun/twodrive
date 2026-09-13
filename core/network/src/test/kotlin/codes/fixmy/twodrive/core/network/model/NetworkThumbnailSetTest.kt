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

package codes.fixmy.twodrive.core.network.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NetworkThumbnailSetTest {

    private val set = NetworkThumbnailSet(
        small = NetworkThumbnail(url = "s", width = 96, height = 72),
        medium = NetworkThumbnail(url = "m", width = 176, height = 132),
        large = NetworkThumbnail(url = "l", width = 800, height = 600),
    )

    @Test
    fun picksTheSmallestRenditionThatCoversTheRequestedSize() {
        assertEquals("s", set.bestFor(80)?.url)
        assertEquals("s", set.bestFor(96)?.url)
        assertEquals("m", set.bestFor(105)?.url)
        assertEquals("l", set.bestFor(265)?.url)
    }

    @Test
    fun fallsBackToTheLargestWhenNoneIsBigEnough() {
        assertEquals("l", set.bestFor(2_000)?.url)
        assertEquals("m", set.copy(large = null).bestFor(2_000)?.url)
    }

    @Test
    fun skipsRenditionsWithoutAUrl() {
        assertEquals("l", set.copy(medium = NetworkThumbnail(width = 176, height = 132)).bestFor(105)?.url)
        assertNull(NetworkThumbnailSet().bestFor(96))
    }
}

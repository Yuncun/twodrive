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

package codes.fixmy.twodrive.core.ui

import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class FileSizeTest {
    @Test
    fun bytesBelowOneThousandAreShownAsBytes() {
        assertEquals("999 B", formatFileSize(999, Locale.US))
    }

    @Test
    fun kilobytesKeepOneDecimal() {
        assertEquals("48.2 KB", formatFileSize(48_213, Locale.US))
    }

    @Test
    fun hundredsDropTheDecimal() {
        assertEquals("133 KB", formatFileSize(132_770, Locale.US))
    }

    @Test
    fun megabytesAndGigabytes() {
        assertEquals("3.4 MB", formatFileSize(3_402_118, Locale.US))
        assertEquals("88.1 MB", formatFileSize(88_120_001, Locale.US))
        assertEquals("100 GB", formatFileSize(100_000_000_000, Locale.US))
    }
}

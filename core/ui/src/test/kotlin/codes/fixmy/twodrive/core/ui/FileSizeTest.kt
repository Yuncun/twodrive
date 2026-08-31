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
    fun kilobytesAreTheFloorUnit() {
        assertEquals("0 KB", formatFileSize(0, Locale.US))
        assertEquals("0 KB", formatFileSize(499, Locale.US))
        assertEquals("1 KB", formatFileSize(999, Locale.US))
    }

    @Test
    fun kilobytesNeverCarryDecimals() {
        assertEquals("48 KB", formatFileSize(48_213, Locale.US))
        assertEquals("133 KB", formatFileSize(132_770, Locale.US))
        assertEquals("181 KB", formatFileSize(181_000, Locale.US))
    }

    @Test
    fun megabytesAndUpKeepOneDecimal() {
        assertEquals("1.2 MB", formatFileSize(1_200_000, Locale.US))
        assertEquals("3.4 MB", formatFileSize(3_402_118, Locale.US))
        assertEquals("294.6 MB", formatFileSize(294_600_000, Locale.US))
        assertEquals("1.4 GB", formatFileSize(1_400_000_000, Locale.US))
    }

    @Test
    fun wholeMegabytesAndUpDropTheZeroDecimal() {
        assertEquals("450 MB", formatFileSize(450_000_000, Locale.US))
        assertEquals("100 GB", formatFileSize(100_000_000_000, Locale.US))
    }
}

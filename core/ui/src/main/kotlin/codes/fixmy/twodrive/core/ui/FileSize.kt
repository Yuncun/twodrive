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

import java.util.Locale

/**
 * Formats a byte count the way OneDrive does, using decimal units. KB is the floor unit and never
 * carries decimals ("0 KB", "181 KB"); MB and up keep one decimal unless it is zero ("1.2 MB",
 * "294.6 MB", "450 MB", "1.4 GB") — docs/ux-reference/spec/my-files-list.md.
 */
fun formatFileSize(bytes: Long, locale: Locale = Locale.getDefault()): String {
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes / 1000.0
    var unit = 0
    while (value >= 1000 && unit < units.lastIndex) {
        value /= 1000
        unit++
    }
    val rounded = Math.round(value * 10) / 10.0
    val pattern = if (unit == 0 || rounded == Math.rint(rounded)) "%.0f %s" else "%.1f %s"
    return String.format(locale, pattern, if (unit == 0) value else rounded, units[unit])
}

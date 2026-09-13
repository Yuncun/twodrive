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
import kotlin.math.roundToInt

/**
 * Formats a storage quota amount the way OneDrive's account drawer does. Microsoft sells storage
 * in binary units but labels them GB, so a 107,374,182,400-byte plan reads "100 GB"; amounts keep
 * one decimal unless it is zero ("100.3 GB", "5 GB") — docs/ux-reference/spec/account-drawer.md.
 */
fun formatStorageSize(bytes: Long, locale: Locale = Locale.getDefault()): String {
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes / 1024.0
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    val rounded = Math.round(value * 10) / 10.0
    val pattern = if (rounded == Math.rint(rounded)) "%.0f %s" else "%.1f %s"
    return String.format(locale, pattern, rounded, units[unit])
}

/** The whole-number percentage of [total] that [used] represents; 0 when [total] is unknown. */
fun storagePercent(used: Long, total: Long): Int =
    if (total <= 0) 0 else (used * 100.0 / total).roundToInt()

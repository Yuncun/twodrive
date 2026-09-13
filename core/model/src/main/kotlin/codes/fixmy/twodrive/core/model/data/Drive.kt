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

package codes.fixmy.twodrive.core.model.data

/**
 * The user's default drive and its storage quota, in bytes.
 *
 * [isQuotaFull] is true when Graph reports the quota as exceeded, when [quotaUsed] has reached
 * [quotaTotal], or when a request was refused with 507 Insufficient Storage.
 */
data class Drive(
    val id: String,
    val quotaUsed: Long,
    val quotaTotal: Long,
    val isQuotaFull: Boolean = quotaTotal in 1..quotaUsed,
) {
    /** The share of the quota in use, clamped to 0..1 for a progress bar. */
    val quotaFraction: Float
        get() = if (quotaTotal <= 0) 0f else (quotaUsed.toFloat() / quotaTotal).coerceIn(0f, 1f)
}

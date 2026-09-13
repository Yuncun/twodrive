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

package codes.fixmy.twodrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons

/**
 * The signed-in account's round avatar: the initials of [displayName] in primary on a
 * primaryContainer circle, or the Person glyph when the name has no letters to show.
 * Used by the Files top bar and the account drawer's account tile.
 */
@Composable
fun AccountAvatar(
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val initials = accountInitials(displayName)
        if (initials.isEmpty()) {
            Icon(
                imageVector = TwoDriveIcons.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 2 / 3),
            )
        } else {
            Text(
                text = initials,
                style = if (size < 48.dp) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.titleMedium
                },
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * Up to two initials for [displayName]: the first letters of its first and last words, so
 * "Demo User" gives "DU" and "Ada" gives "A". Returns "" when the name is blank.
 */
internal fun accountInitials(displayName: String): String {
    val words = displayName.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return ""
    val letters = if (words.size == 1) listOf(words.first()) else listOf(words.first(), words.last())
    return letters.joinToString("") { String(Character.toChars(it.codePointAt(0))).uppercase() }
}

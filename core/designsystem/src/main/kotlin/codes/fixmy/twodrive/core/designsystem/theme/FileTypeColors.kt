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

package codes.fixmy.twodrive.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Tints for the file-list icons, as OneDrive draws them: a yellow folder and one brand color per
 * document type. Microsoft uses the same values in light and dark mode, so these do not change
 * with [TwoDriveTheme]'s `darkTheme` flag.
 */
object FileTypeColors {
    val Folder = Color(0xFFFFB900)
    val Word = Color(0xFF185ABD)
    val Excel = Color(0xFF107C41)
    val PowerPoint = Color(0xFFC43E1C)
    val Pdf = Color(0xFFD13438)
    val Image = Color(0xFF038387)
    val Video = Color(0xFF8764B8)
    val Audio = Color(0xFFC239B3)
    val Text = Color(0xFF2B579A)
    val Generic = Color(0xFF797775)
}

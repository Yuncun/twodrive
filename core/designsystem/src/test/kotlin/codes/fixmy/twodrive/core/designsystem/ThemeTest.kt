/*
 * Copyright 2022 Eric Shen
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

package codes.fixmy.twodrive.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.core.designsystem.theme.BackgroundTheme
import codes.fixmy.twodrive.core.designsystem.theme.DarkColorScheme
import codes.fixmy.twodrive.core.designsystem.theme.DarkGradientColors
import codes.fixmy.twodrive.core.designsystem.theme.LightColorScheme
import codes.fixmy.twodrive.core.designsystem.theme.LightGradientColors
import codes.fixmy.twodrive.core.designsystem.theme.LocalBackgroundTheme
import codes.fixmy.twodrive.core.designsystem.theme.LocalGradientColors
import codes.fixmy.twodrive.core.designsystem.theme.LocalTintTheme
import codes.fixmy.twodrive.core.designsystem.theme.TintTheme
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

/**
 * Tests that [TwoDriveTheme] hands the fixed OneDrive palette to [MaterialTheme] and to the
 * composition locals of the design system, in both light and dark mode. There is deliberately no
 * dynamic-color or Android-theme variant to test: the palette never follows the wallpaper.
 */
@RunWith(RobolectricTestRunner::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lightTheme_usesTheLightOneDriveScheme() {
        composeTestRule.setContent {
            TwoDriveTheme(darkTheme = false) {
                assertColorSchemesEqual(LightColorScheme, MaterialTheme.colorScheme)
                assertEquals(LightGradientColors, LocalGradientColors.current)
                assertEquals(
                    BackgroundTheme(color = LightColorScheme.surface, tonalElevation = 0.dp),
                    LocalBackgroundTheme.current,
                )
                assertEquals(TintTheme(), LocalTintTheme.current)
            }
        }
    }

    @Test
    fun darkTheme_usesTheDarkOneDriveScheme() {
        composeTestRule.setContent {
            TwoDriveTheme(darkTheme = true) {
                assertColorSchemesEqual(DarkColorScheme, MaterialTheme.colorScheme)
                assertEquals(DarkGradientColors, LocalGradientColors.current)
                assertEquals(
                    BackgroundTheme(color = DarkColorScheme.surface, tonalElevation = 0.dp),
                    LocalBackgroundTheme.current,
                )
                assertEquals(TintTheme(), LocalTintTheme.current)
            }
        }
    }

    @Test
    fun lightTheme_hasOneDrivesBlueOnWhiteSurfaces() {
        assertEquals(Color(0xFF0F6CBD), LightColorScheme.primary)
        assertEquals(Color.White, LightColorScheme.surface)
        assertEquals(Color.White, LightColorScheme.background)
        // The grey of the "size · date" secondary text.
        assertEquals(Color(0xFF605E5C), LightColorScheme.onSurfaceVariant)
    }

    private fun assertColorSchemesEqual(
        expectedColorScheme: ColorScheme,
        actualColorScheme: ColorScheme,
    ) {
        assertEquals(expectedColorScheme.primary, actualColorScheme.primary)
        assertEquals(expectedColorScheme.onPrimary, actualColorScheme.onPrimary)
        assertEquals(expectedColorScheme.primaryContainer, actualColorScheme.primaryContainer)
        assertEquals(expectedColorScheme.onPrimaryContainer, actualColorScheme.onPrimaryContainer)
        assertEquals(expectedColorScheme.secondary, actualColorScheme.secondary)
        assertEquals(expectedColorScheme.onSecondary, actualColorScheme.onSecondary)
        assertEquals(expectedColorScheme.secondaryContainer, actualColorScheme.secondaryContainer)
        assertEquals(expectedColorScheme.onSecondaryContainer, actualColorScheme.onSecondaryContainer)
        assertEquals(expectedColorScheme.tertiary, actualColorScheme.tertiary)
        assertEquals(expectedColorScheme.onTertiary, actualColorScheme.onTertiary)
        assertEquals(expectedColorScheme.tertiaryContainer, actualColorScheme.tertiaryContainer)
        assertEquals(expectedColorScheme.onTertiaryContainer, actualColorScheme.onTertiaryContainer)
        assertEquals(expectedColorScheme.error, actualColorScheme.error)
        assertEquals(expectedColorScheme.onError, actualColorScheme.onError)
        assertEquals(expectedColorScheme.errorContainer, actualColorScheme.errorContainer)
        assertEquals(expectedColorScheme.onErrorContainer, actualColorScheme.onErrorContainer)
        assertEquals(expectedColorScheme.background, actualColorScheme.background)
        assertEquals(expectedColorScheme.onBackground, actualColorScheme.onBackground)
        assertEquals(expectedColorScheme.surface, actualColorScheme.surface)
        assertEquals(expectedColorScheme.onSurface, actualColorScheme.onSurface)
        assertEquals(expectedColorScheme.surfaceVariant, actualColorScheme.surfaceVariant)
        assertEquals(expectedColorScheme.onSurfaceVariant, actualColorScheme.onSurfaceVariant)
        assertEquals(expectedColorScheme.inverseSurface, actualColorScheme.inverseSurface)
        assertEquals(expectedColorScheme.inverseOnSurface, actualColorScheme.inverseOnSurface)
        assertEquals(expectedColorScheme.outline, actualColorScheme.outline)
        assertEquals(expectedColorScheme.outlineVariant, actualColorScheme.outlineVariant)
    }
}

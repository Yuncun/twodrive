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

package codes.fixmy.twodrive.core.designsystem.theme

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Light color scheme: OneDrive's blue on white surfaces, with grey secondary text.
 */
@VisibleForTesting
val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Neutral40,
    onSecondary = Color.White,
    secondaryContainer = Neutral95,
    onSecondaryContainer = Neutral10,
    tertiary = Teal40,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Teal10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Color.White,
    onBackground = Neutral10,
    surface = Color.White,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral40,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral99,
    outline = Neutral80,
    outlineVariant = Neutral70,
)

/**
 * Dark color scheme: the same hues lightened to stay readable on a near-black surface.
 */
@VisibleForTesting
val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue10,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    secondary = Neutral70,
    onSecondary = Neutral10,
    secondaryContainer = Neutral20,
    onSecondaryContainer = Neutral95,
    tertiary = Teal80,
    onTertiary = Teal10,
    tertiaryContainer = Teal30,
    onTertiaryContainer = Teal90,
    error = Red80,
    onError = Red10,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = NeutralDark,
    onBackground = Neutral95,
    surface = NeutralDark,
    onSurface = Neutral95,
    surfaceVariant = Neutral15,
    onSurfaceVariant = Neutral70,
    inverseSurface = Neutral95,
    inverseOnSurface = Neutral10,
    outline = Neutral30,
    outlineVariant = Neutral20,
)

/**
 * Gradient behind the sign-in illustration, in light and dark mode.
 */
@VisibleForTesting
val LightGradientColors = GradientColors(top = Color.White, bottom = Blue90, container = Color.White)

@VisibleForTesting
val DarkGradientColors = GradientColors(top = NeutralDark, bottom = Blue20, container = NeutralDark)

/**
 * TwoDrive theme.
 *
 * The color scheme is fixed: dynamic (wallpaper) color is deliberately never used, so that the app
 * keeps OneDrive's blue on every device.
 *
 * @param darkTheme Whether the theme should use a dark color scheme (follows system by default).
 */
@Composable
fun TwoDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val gradientColors = if (darkTheme) DarkGradientColors else LightGradientColors
    val backgroundTheme = BackgroundTheme(color = colorScheme.surface, tonalElevation = 0.dp)
    CompositionLocalProvider(
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides TintTheme(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TwoDriveTypography,
            content = content,
        )
    }
}

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

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.R
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveTopAppBar
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.screenshottesting.captureMultiDevice
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import codes.fixmy.twodrive.feature.files.api.R as FilesR

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FilesTopAppBarScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /** The Files top bar with the demo account's initials, as in docs/ux-reference/10-files-root.png. */
    @Test
    fun filesTopAppBar_initials() {
        composeTestRule.captureMultiDevice("FilesTopAppBar") {
            FilesTopAppBar(displayName = profile.displayName)
        }
    }

    @Test
    fun filesTopAppBar_blankNameShowsPersonGlyph() {
        composeTestRule.captureMultiDevice("FilesTopAppBarNoName") {
            FilesTopAppBar(displayName = "")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun FilesTopAppBar(displayName: String) {
        TwoDriveTheme {
            TwoDriveTopAppBar(
                titleRes = FilesR.string.feature_files_api_title,
                navigationIcon = { AccountAvatar(displayName = displayName, size = 40.dp) },
                navigationIconContentDescription = stringResource(R.string.top_app_bar_navigation_icon_description),
            )
        }
    }
}

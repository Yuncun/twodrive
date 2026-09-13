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

package codes.fixmy.twodrive.feature.files.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.screenshottesting.captureMultiDevice
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class DeleteSnackbarScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val budget = demoDriveChildren().first { it.name == "Household budget.xlsx" }

    @Test
    fun deleteSnackbar_undo() {
        composeTestRule.captureMultiDevice("DeleteSnackbarUndo") {
            SnackbarContent(pendingDelete = budget)
        }
    }

    @Test
    fun deleteSnackbar_failed() {
        composeTestRule.captureMultiDevice("DeleteSnackbarFailed") {
            SnackbarContent(deleteError = budget)
        }
    }

    @Composable
    private fun SnackbarContent(pendingDelete: DriveItem? = null, deleteError: DriveItem? = null) {
        TwoDriveTheme {
            Surface {
                Box(modifier = Modifier.fillMaxSize()) {
                    DeleteSnackbarHost(
                        pendingDelete = pendingDelete,
                        deleteError = deleteError,
                        onUndo = {},
                        onUndoWindowEnd = {},
                        onErrorShown = {},
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

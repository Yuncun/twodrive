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
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveBackground
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.screenshottesting.captureMultiDevice
import codes.fixmy.twodrive.core.testing.data.driveItemsTestData
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FilesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun filesScreen_rootFolder() {
        composeTestRule.captureMultiDevice("FilesScreenRoot") {
            FilesScreenContent(
                FilesUiState.Success(
                    folder = null,
                    items = driveItemsTestData.filter { it.parentId == "root" },
                    sortOrder = SortOrder.NAME_ASCENDING,
                ),
            )
        }
    }

    @Test
    fun filesScreen_loading() {
        composeTestRule.captureMultiDevice("FilesScreenLoading") {
            FilesScreenContent(FilesUiState.Loading)
        }
    }

    @Test
    fun filesScreen_emptyFolder() {
        composeTestRule.captureMultiDevice("FilesScreenEmpty") {
            FilesScreenContent(
                FilesUiState.Success(folder = null, items = emptyList(), sortOrder = SortOrder.NAME_ASCENDING),
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun FilesScreenContent(uiState: FilesUiState) {
        TwoDriveTheme {
            TwoDriveBackground {
                FilesScreen(
                    uiState = uiState,
                    onFolderClick = {},
                    onFileClick = {},
                    onSortOrderChange = {},
                )
            }
        }
    }
}

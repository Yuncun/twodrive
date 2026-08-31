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
import codes.fixmy.twodrive.core.model.data.ViewMode
import codes.fixmy.twodrive.core.screenshottesting.captureMultiDevice
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.datetime.LocalDate
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
                    items = demoDriveChildren(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
            )
        }
    }

    @Test
    fun filesScreen_rootFolderTiles() {
        composeTestRule.captureMultiDevice(
            "FilesScreenRootTiles",
            // As for FolderScreen: tiles clipped at the canvas edge lose their visible text and
            // ATF reports them as unlabelled; scroll clipping is not an accessibility defect.
            accessibilitySuppressions = matchesCheck(SpeakableTextPresentCheck::class.java),
        ) {
            FilesScreenContent(
                FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.TILE,
                ),
            )
        }
    }

    @Test
    fun filesScreen_vaultTab() {
        composeTestRule.captureMultiDevice("FilesScreenVaultTab") {
            FilesScreenContent(
                FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
                selectedTab = FilesTab.VAULT,
            )
        }
    }

    @Test
    fun filesScreen_offlineTab() {
        composeTestRule.captureMultiDevice("FilesScreenOfflineTab") {
            FilesScreenContent(
                FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
                selectedTab = FilesTab.OFFLINE,
            )
        }
    }

    @Test
    fun folderScreen() {
        composeTestRule.captureMultiDevice(
            "FolderScreen",
            // The landscape phone canvas clips the last row to a sliver with its text outside the
            // visible bounds, which ATF reports as an unlabelled item; scroll clipping is not an
            // accessibility defect, so that check is suppressed for this capture.
            accessibilitySuppressions = matchesCheck(SpeakableTextPresentCheck::class.java),
        ) {
            TwoDriveTheme {
                TwoDriveBackground {
                    FolderScreen(
                        folderName = "Documents",
                        uiState = FilesUiState.Success(
                            folder = null,
                            items = demoDriveChildren("f-documents"),
                            sortOrder = SortOrder.NAME_ASCENDING,
                            viewMode = ViewMode.LIST,
                        ),
                        onBackClick = {},
                        onFolderClick = {},
                        onFileClick = {},
                        onMoreClick = {},
                        onSortOrderChange = {},
                        onViewModeChange = {},
                        // Fixed so the year-dropping date format keeps the goldens stable over time.
                        today = LocalDate(2026, 8, 30),
                    )
                }
            }
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
                FilesUiState.Success(
                    folder = null,
                    items = emptyList(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun FilesScreenContent(
        uiState: FilesUiState,
        selectedTab: FilesTab = FilesTab.MY_FILES,
    ) {
        TwoDriveTheme {
            TwoDriveBackground {
                FilesScreen(
                    uiState = uiState,
                    selectedTab = selectedTab,
                    onTabClick = {},
                    onFolderClick = {},
                    onFileClick = {},
                    onMoreClick = {},
                    onSortOrderChange = {},
                    onViewModeChange = {},
                    // Fixed so the year-dropping date format keeps the goldens stable over time.
                    today = LocalDate(2026, 8, 30),
                )
            }
        }
    }
}

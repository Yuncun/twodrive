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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.ViewMode
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class DeleteItemFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<String>()

    private val budget = demoDriveChildren().first { it.name == "Household budget.xlsx" }

    @Test
    fun undoOnTheDeletedSnackbarCallsUndoAndNotTheWindowEnd() {
        setContent(initialPendingDelete = budget)

        composeTestRule.onNodeWithText("Deleted Household budget.xlsx").assertExists()
        composeTestRule.onNodeWithText("Undo").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf("undo"), events)
        composeTestRule.onNodeWithText("Deleted Household budget.xlsx").assertDoesNotExist()
    }

    @Test
    fun theSnackbarTimingOutEndsTheUndoWindow() {
        setContent(initialPendingDelete = budget)
        composeTestRule.onNodeWithText("Deleted Household budget.xlsx").assertExists()

        composeTestRule.mainClock.advanceTimeBy(LONG_SNACKBAR_MILLIS)
        composeTestRule.waitForIdle()

        assertEquals(listOf("windowEnd"), events)
    }

    @Test
    fun failedDeleteIsShownAsASnackbarOverTheList() {
        setContent(deleteError = budget)

        composeTestRule.onNodeWithText("Couldn’t delete Household budget.xlsx").assertExists()
        composeTestRule.onNodeWithText("Household budget.xlsx").assertExists()
    }

    private fun setContent(initialPendingDelete: DriveItem? = null, deleteError: DriveItem? = null) {
        composeTestRule.setContent {
            var pendingDelete by remember { mutableStateOf(initialPendingDelete) }
            TwoDriveTheme {
                FilesScreen(
                    uiState = FilesUiState.Success(
                        folder = null,
                        items = demoDriveChildren().filterNot { it.id == pendingDelete?.id },
                        sortOrder = SortOrder.NAME_ASCENDING,
                        viewMode = ViewMode.LIST,
                    ),
                    homeUiState = HomeUiState.Loading,
                    selectedTab = FilesTab.MY_FILES,
                    isOffline = false,
                    onTabClick = {},
                    onFolderClick = {},
                    onFileClick = {},
                    onMoreClick = {},
                    onSeeAllClick = {},
                    onSortOrderChange = {},
                    onViewModeChange = {},
                    pendingDelete = pendingDelete,
                    deleteError = deleteError,
                    onUndoDelete = {
                        events += "undo"
                        pendingDelete = null
                    },
                    onDeleteUndoWindowEnd = {
                        events += "windowEnd"
                        pendingDelete = null
                    },
                    today = LocalDate(2026, 8, 30),
                )
            }
        }
    }

    private companion object {
        /** Material 3's SnackbarDuration.Long plus room for the exit animation. */
        const val LONG_SNACKBAR_MILLIS = 11_000L
    }
}

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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import codes.fixmy.twodrive.core.model.data.SortOrder
import codes.fixmy.twodrive.core.model.data.ViewMode
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.TimeZone
import kotlin.test.assertEquals

/**
 * A pushed folder rendered from the drive the demo flavor ships.
 */
@RunWith(RobolectricTestRunner::class)
class FolderScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var backClicks = 0

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun setContent() {
        composeTestRule.setContent {
            FolderScreen(
                folderName = "Documents",
                uiState = FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren("f-documents"),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
                onBackClick = { backClicks++ },
                onFolderClick = {},
                onFileClick = {},
                onMoreClick = {},
                onSortOrderChange = {},
                onViewModeChange = {},
                // Fixed so the year-dropping date format renders the same on any test day.
                today = LocalDate(2026, 8, 30),
            )
        }
    }

    @Test
    fun titleIsTheFolderName() {
        setContent()

        composeTestRule.onNodeWithText("Documents").assertIsDisplayed()
    }

    @Test
    fun listShowsTheFoldersChildrenFoldersFirst() {
        setContent()

        composeTestRule.onNodeWithText("Taxes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Apartment lease.pdf").assertIsDisplayed()
    }

    @Test
    fun folderScreenHasNoPivotTabs() {
        setContent()

        composeTestRule.onNodeWithTag("files:tabs").assertDoesNotExist()
    }

    @Test
    fun backArrowNavigatesUp() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Navigate up").performClick()

        assertEquals(1, backClicks)
    }
}

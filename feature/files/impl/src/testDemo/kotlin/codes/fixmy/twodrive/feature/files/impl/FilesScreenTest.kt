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
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import codes.fixmy.twodrive.core.model.data.SortOrder
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.TimeZone
import kotlin.test.assertEquals

/**
 * The My files list rendered from the drive the demo flavor ships.
 */
@RunWith(RobolectricTestRunner::class)
class FilesScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val folderClicks = mutableListOf<String>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    private fun setContent() {
        composeTestRule.setContent {
            FilesScreen(
                uiState = FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren(),
                    sortOrder = SortOrder.NAME_ASCENDING,
                ),
                onFolderClick = folderClicks::add,
                onFileClick = {},
                onMoreClick = {},
                onSortOrderChange = {},
                // Fixed so the year-dropping date format renders the same on any test day.
                today = LocalDate(2026, 8, 30),
            )
        }
    }

    @Test
    fun fileRowShowsSizeThenModifiedDate() {
        setContent()

        // Files sort after folders, so the first file row can start below the fold.
        composeTestRule.onNodeWithTag("files:list").performScrollToNode(hasText("Resume 2026.docx"))

        composeTestRule.onNodeWithText("Resume 2026.docx").assertIsDisplayed()
        composeTestRule.onNodeWithText("48.2 KB · Aug 20").assertIsDisplayed()
    }

    @Test
    fun folderRowShowsRecursiveSizeThenModifiedDate() {
        setContent()

        composeTestRule.onNodeWithText("Documents").assertIsDisplayed()
        composeTestRule.onNodeWithText("13.3 MB · Aug 18").assertIsDisplayed()
    }

    @Test
    fun moreOptionsButtonIsLabelledWithTheItemName() {
        setContent()

        composeTestRule.onNodeWithContentDescription("More options for Documents").assertIsDisplayed()
    }

    @Test
    fun tappingAFolderOpensIt() {
        setContent()

        composeTestRule.onNodeWithText("Documents").performClick()

        assertEquals(listOf("f-documents"), folderClicks)
    }
}

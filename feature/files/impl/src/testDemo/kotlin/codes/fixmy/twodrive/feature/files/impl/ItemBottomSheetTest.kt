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

@RunWith(RobolectricTestRunner::class)
class ItemBottomSheetTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val lease = demoDriveChildren("f-documents").first { it.id == "i-lease" }

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun tappingAnActionHandsItToTheCallback() {
        val tapped = mutableListOf<ItemAction>()
        composeTestRule.setContent {
            ItemBottomSheetContent(
                item = lease,
                actions = itemActions(lease, ItemSource.FOLDER),
                today = LocalDate(2026, 8, 30),
                onAction = { tapped += it },
            )
        }

        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Details").performClick()

        assertEquals(listOf(ItemAction.DELETE, ItemAction.DETAILS), tapped)
    }

    @Test
    fun moreButtonOpensTheSheetAndAnActionDismissesIt() {
        composeTestRule.setContent {
            var open by remember { mutableStateOf(false) }
            FolderScreen(
                folderName = "Documents",
                uiState = FilesUiState.Success(
                    folder = null,
                    items = demoDriveChildren("f-documents"),
                    sortOrder = SortOrder.NAME_ASCENDING,
                    viewMode = ViewMode.LIST,
                ),
                isOffline = false,
                onBackClick = {},
                onFolderClick = {},
                onFileClick = {},
                onMoreClick = { open = true },
                onSortOrderChange = {},
                onViewModeChange = {},
                today = LocalDate(2026, 8, 30),
            )
            if (open) {
                ItemBottomSheet(
                    item = lease,
                    source = ItemSource.FOLDER,
                    today = LocalDate(2026, 8, 30),
                    onDismissRequest = { open = false },
                    onAction = { _, _ -> },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("More options for Apartment lease.pdf").performClick()
        composeTestRule.onNodeWithTag("files:itemSheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rename").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("files:itemSheet").assertDoesNotExist()
    }
}

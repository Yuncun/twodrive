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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
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
class CreateFolderFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val createdNames = mutableListOf<String>()

    @Test
    fun fabOpensTheAddMenuAndCreateFolderSendsTheTypedName() {
        setContent()

        composeTestRule.onNodeWithTag("files:add").performClick()
        composeTestRule.onNodeWithTag("files:add:upload").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("files:add:createFolder").performClick()

        composeTestRule.onNodeWithTag("files:createFolder:create").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("files:createFolder:name").performTextInput("Trips")
        composeTestRule.onNodeWithTag("files:createFolder:create").assertIsEnabled().performClick()

        assertEquals(listOf("Trips"), createdNames)
        composeTestRule.onNodeWithTag("files:createFolder:name").assertDoesNotExist()
    }

    @Test
    fun storageFullIsShownAsASnackbarOverTheList() {
        setContent(createFolderError = CreateFolderError.STORAGE_FULL)

        composeTestRule.onNodeWithText("Your OneDrive is full, so the folder wasn’t created.").assertExists()
        composeTestRule.onNodeWithTag("files:add").assertExists()
    }

    private fun setContent(createFolderError: CreateFolderError? = null) {
        composeTestRule.setContent {
            TwoDriveTheme {
                FilesScreen(
                    uiState = FilesUiState.Success(
                        folder = null,
                        items = demoDriveChildren(),
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
                    createFolderError = createFolderError,
                    onCreateFolder = { createdNames += it },
                    today = LocalDate(2026, 8, 30),
                )
            }
        }
    }
}

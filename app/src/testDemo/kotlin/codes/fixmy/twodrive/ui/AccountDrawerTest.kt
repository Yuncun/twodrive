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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import codes.fixmy.twodrive.R
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AccountDrawerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int) = composeTestRule.activity.getString(id)

    @Test
    fun showsEmailQuotaAndSignOut() {
        var signOuts = 0
        composeTestRule.setContent {
            AccountDrawerSheet(
                profile = profile,
                uiState = AccountDrawerUiState(drive = fullDrive),
                onSignOut = { signOuts++ },
                onViewPlan = {},
                onRetrySync = {},
            )
        }

        composeTestRule.onNodeWithText("demo@example.com").assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Microsoft storage, 100.3 GB used of 100 GB (100%)")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.account_drawer_view_plan)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("accountDrawer:banner").assertDoesNotExist()

        composeTestRule.onNodeWithText(string(R.string.account_drawer_sign_out)).performClick()
        assertEquals(1, signOuts)
    }

    @Test
    fun syncFailedBannerRetries() {
        var retries = 0
        composeTestRule.setContent {
            AccountDrawerSheet(
                profile = profile,
                uiState = AccountDrawerUiState(drive = normalDrive, banner = AccountDrawerBanner.SYNC_FAILED),
                onSignOut = {},
                onViewPlan = {},
                onRetrySync = { retries++ },
            )
        }

        composeTestRule.onNodeWithText(string(R.string.account_drawer_banner_sync_failed_title)).assertExists()
        composeTestRule.onNodeWithText(string(R.string.account_drawer_banner_sync_failed_action))
            .performScrollTo()
            .performClick()
        assertEquals(1, retries)
    }
}

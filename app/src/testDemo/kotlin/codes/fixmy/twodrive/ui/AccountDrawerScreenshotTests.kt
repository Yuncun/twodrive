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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.Drive
import codes.fixmy.twodrive.core.model.data.UserProfile
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
class AccountDrawerScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun accountDrawer_quotaNormal() {
        composeTestRule.captureMultiDevice("AccountDrawer") {
            DrawerContent(AccountDrawerUiState(drive = normalDrive))
        }
    }

    /** The demo drive's state: over quota, as in docs/ux-reference/spec/account-drawer.md. */
    @Test
    fun accountDrawer_quotaFull() {
        composeTestRule.captureMultiDevice("AccountDrawerQuotaFull") {
            DrawerContent(AccountDrawerUiState(drive = fullDrive))
        }
    }

    @Test
    fun accountDrawer_syncFailedBanner() {
        composeTestRule.captureMultiDevice("AccountDrawerSyncFailed") {
            DrawerContent(AccountDrawerUiState(drive = normalDrive, banner = AccountDrawerBanner.SYNC_FAILED))
        }
    }

    @Test
    fun accountDrawer_offlineBeforeQuotaLoaded() {
        composeTestRule.captureMultiDevice("AccountDrawerOffline") {
            DrawerContent(AccountDrawerUiState(drive = null, banner = AccountDrawerBanner.OFFLINE))
        }
    }

    @Composable
    private fun DrawerContent(uiState: AccountDrawerUiState) {
        TwoDriveTheme {
            Box(Modifier.fillMaxSize()) {
                AccountDrawerSheet(
                    profile = profile,
                    uiState = uiState,
                    onSignOut = {},
                    onViewPlan = {},
                    onRetrySync = {},
                )
            }
        }
    }
}

internal val profile = UserProfile(displayName = "Demo User", email = "demo@example.com")
internal val normalDrive = Drive(id = "d", quotaUsed = 3_758_096_384, quotaTotal = 5_368_709_120)
internal val fullDrive = Drive(id = "d", quotaUsed = 107_696_304_947, quotaTotal = 107_374_182_400)

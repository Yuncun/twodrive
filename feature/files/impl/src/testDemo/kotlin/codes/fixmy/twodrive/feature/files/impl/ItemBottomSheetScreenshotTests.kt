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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.screenshottesting.captureMultiDevice
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
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

/**
 * The item sheet's body, captured without the modal container: a ModalBottomSheet draws in its
 * own window, which the capture of the activity's content does not include.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ItemBottomSheetScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Test
    fun itemBottomSheet_folder() {
        composeTestRule.captureMultiDevice(
            "ItemBottomSheetFolder",
            // The short landscape phone canvas clips the last action row to a sliver that ATF
            // measures as an undersized touch target; clipping is not an accessibility defect.
            accessibilitySuppressions = matchesCheck(TouchTargetSizeCheck::class.java),
        ) {
            SheetContent(demoDriveChildren().first { it.id == "f-documents" }, ItemSource.FOLDER)
        }
    }

    @Test
    fun itemBottomSheet_file() {
        composeTestRule.captureMultiDevice(
            "ItemBottomSheetFile",
            // The short landscape phone canvas clips the last action row to a sliver that ATF
            // measures as an undersized touch target; clipping is not an accessibility defect.
            accessibilitySuppressions = matchesCheck(TouchTargetSizeCheck::class.java),
        ) {
            SheetContent(demoDriveChildren("f-documents").first { it.id == "i-lease" }, ItemSource.FOLDER)
        }
    }

    @Test
    fun itemBottomSheet_recentFile() {
        composeTestRule.captureMultiDevice("ItemBottomSheetRecentFile") {
            SheetContent(demoDriveChildren("f-documents").first { it.id == "i-lease" }, ItemSource.HOME_RECENT)
        }
    }

    @Composable
    private fun SheetContent(item: DriveItem, source: ItemSource) {
        TwoDriveTheme {
            Surface {
                ItemBottomSheetContent(
                    item = item,
                    actions = itemActions(item, source),
                    // Fixed so the year-dropping date format keeps the goldens stable over time.
                    today = LocalDate(2026, 8, 30),
                    onAction = {},
                )
            }
        }
    }
}

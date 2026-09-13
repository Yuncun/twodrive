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
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class DeleteSnackbarHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val items = demoDriveChildren()

    @Test
    fun leavingComposition_endsUndoWindow() {
        var shown by mutableStateOf(true)
        var windowEnds = 0
        var undos = 0
        // A running clock would time the snackbar out and end the window on its own.
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            if (shown) {
                DeleteSnackbarHost(
                    pendingDelete = items[0],
                    deleteError = null,
                    onUndo = { undos++ },
                    onUndoWindowEnd = { windowEnds++ },
                    onErrorShown = {},
                )
            }
        }
        composeTestRule.mainClock.advanceTimeByFrame()
        assertEquals(0, windowEnds)

        shown = false
        composeTestRule.mainClock.advanceTimeByFrame()

        assertEquals(1, windowEnds)
        assertEquals(0, undos)
    }

    @Test
    fun newPendingDelete_doesNotEndUndoWindow() {
        var pendingDelete by mutableStateOf(items[0])
        var windowEnds = 0
        // A running clock would time the snackbar out and end the window on its own.
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            DeleteSnackbarHost(
                pendingDelete = pendingDelete,
                deleteError = null,
                onUndo = {},
                onUndoWindowEnd = { windowEnds++ },
                onErrorShown = {},
            )
        }
        composeTestRule.mainClock.advanceTimeByFrame()

        pendingDelete = items[1]
        composeTestRule.mainClock.advanceTimeByFrame()

        assertEquals(0, windowEnds)
    }
}

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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import codes.fixmy.twodrive.R
import codes.fixmy.twodrive.core.auth.AuthError
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class WelcomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val headline get() = composeTestRule.activity.getString(R.string.welcome_headline)
    private val signInLabel get() = composeTestRule.activity.getString(R.string.welcome_sign_in_button)

    @Test
    fun signInButtonReportsClicks() {
        var clicks = 0
        composeTestRule.setContent {
            WelcomeScreen(error = null, onSignInClick = { clicks++ })
        }

        composeTestRule.onNodeWithText(headline).assertIsDisplayed()
        composeTestRule.onNodeWithText(signInLabel).assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome:signIn").performClick()

        assertEquals(1, clicks)
    }

    /**
     * docs/ux-reference/02-welcome.png carries the headline alone: no subheading or tagline sits
     * between it and the bottom-anchored button stack.
     */
    @Test
    fun headlineIsTheOnlyTextBesidesTheButton() {
        composeTestRule.setContent {
            WelcomeScreen(error = null, onSignInClick = {})
        }

        val texts = composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .flatMap { node ->
                node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            }

        assertEquals(listOf(headline, signInLabel), texts)
    }

    @Test
    fun signInErrorIsShownAsItsStringResource() {
        composeTestRule.setContent {
            WelcomeScreen(error = AuthError.CANCELLED, onSignInClick = {})
        }

        val cancelled =
            composeTestRule.activity.getString(R.string.welcome_sign_in_error_cancelled)
        composeTestRule.onNodeWithText(cancelled).assertIsDisplayed()
    }
}

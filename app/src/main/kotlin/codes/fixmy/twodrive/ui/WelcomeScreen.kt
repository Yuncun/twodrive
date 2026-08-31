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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.R
import codes.fixmy.twodrive.core.auth.AuthError
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveBackground
import codes.fixmy.twodrive.core.designsystem.component.TwoDriveButton
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme

/**
 * First screen of a signed-out app, following docs/ux-reference/02-welcome.png: an illustration
 * and headline in the middle of the screen, and the sign-in button pinned to the bottom.
 *
 * In the demo flavor the fake [codes.fixmy.twodrive.core.auth.AuthRepository] signs in
 * immediately, so tapping the button goes straight to the Files tab with no account.
 */
@Composable
fun WelcomeScreen(
    error: AuthError?,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TwoDriveBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.illustration_welcome),
                contentDescription = null,
                modifier = Modifier.width(WelcomeIllustrationWidth),
            )
            Spacer(Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.welcome_headline),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = WelcomeHeadlineMaxWidth),
            )
            Spacer(Modifier.weight(1f))
            if (error != null) {
                Text(
                    text = stringResource(error.messageRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
            TwoDriveButton(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SignInButtonHeight)
                    .testTag("welcome:signIn"),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.welcome_sign_in_button),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

/**
 * The user-facing message for a failed sign-in. Provider error text never reaches the
 * screen; only these localised strings do.
 */
private fun AuthError.messageRes() = when (this) {
    AuthError.CANCELLED -> R.string.welcome_sign_in_error_cancelled
    AuthError.NO_NETWORK -> R.string.welcome_sign_in_error_no_network
    AuthError.SERVICE -> R.string.welcome_sign_in_error_service
    AuthError.UNKNOWN -> R.string.welcome_sign_in_error_unknown
}

private val WelcomeIllustrationWidth = 240.dp
private val WelcomeHeadlineMaxWidth = 312.dp
private val SignInButtonHeight = 56.dp

@Preview
@Composable
private fun WelcomeScreenPreview() {
    TwoDriveTheme {
        WelcomeScreen(error = null, onSignInClick = {})
    }
}

@Preview
@Composable
private fun WelcomeScreenErrorPreview() {
    TwoDriveTheme {
        WelcomeScreen(error = AuthError.CANCELLED, onSignInClick = {})
    }
}

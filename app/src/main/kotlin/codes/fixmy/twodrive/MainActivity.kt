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

package codes.fixmy.twodrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import codes.fixmy.twodrive.core.data.util.NetworkMonitor
import codes.fixmy.twodrive.core.designsystem.theme.TwoDriveTheme
import codes.fixmy.twodrive.ui.TwoDriveApp
import codes.fixmy.twodrive.ui.WelcomeScreen
import codes.fixmy.twodrive.ui.rememberTwoDriveAppState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the splash screen on-screen until the sign-in state is known. This condition is
        // evaluated each time the app needs to be redrawn so it should be fast to avoid blocking
        // the UI.
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TwoDriveTheme {
                when (val state = uiState) {
                    MainActivityUiState.Loading -> Unit
                    is MainActivityUiState.SignedOut -> WelcomeScreen(
                        error = state.error,
                        onSignInClick = { viewModel.signIn(this) },
                    )

                    is MainActivityUiState.SignedIn -> {
                        val appState = rememberTwoDriveAppState(networkMonitor = networkMonitor)
                        TwoDriveApp(
                            appState = appState,
                            profile = state.profile,
                            onSignOut = viewModel::signOut,
                        )
                    }
                }
            }
        }
    }
}

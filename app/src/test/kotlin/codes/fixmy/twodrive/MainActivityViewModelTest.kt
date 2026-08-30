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

import android.app.Activity
import codes.fixmy.twodrive.core.auth.AuthException
import codes.fixmy.twodrive.core.auth.AuthState
import codes.fixmy.twodrive.core.testing.repository.TestAuthRepository
import codes.fixmy.twodrive.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MainActivityViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val authRepository = TestAuthRepository()
    private lateinit var viewModel: MainActivityViewModel

    @Before
    fun setup() {
        viewModel = MainActivityViewModel(authRepository)
    }

    @Test
    fun loadingKeepsSplashScreen() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        assertEquals(MainActivityUiState.Loading, viewModel.uiState.value)
        assertEquals(true, viewModel.uiState.value.shouldKeepSplashScreen())
    }

    @Test
    fun signedOutThenSignedInFollowsRepository() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        authRepository.setAuthState(AuthState.SignedOut)
        assertEquals(MainActivityUiState.SignedOut(error = null), viewModel.uiState.value)

        authRepository.setAuthState(AuthState.SignedIn(TestAuthRepository.testProfile))
        val state = assertIs<MainActivityUiState.SignedIn>(viewModel.uiState.value)
        assertEquals("test@example.com", state.profile.email)
        assertEquals(false, state.shouldKeepSplashScreen())
    }

    @Test
    fun signInFromWelcomeScreenSignsIn() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        authRepository.setAuthState(AuthState.SignedOut)

        viewModel.signIn(Activity())

        assertIs<MainActivityUiState.SignedIn>(viewModel.uiState.value)
    }

    @Test
    fun failedSignInIsReportedOnTheWelcomeScreen() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
        authRepository.setAuthState(AuthState.SignedOut)
        authRepository.signInException = AuthException("Sign-in was cancelled.")

        viewModel.signIn(Activity())

        assertEquals(
            MainActivityUiState.SignedOut(error = "Sign-in was cancelled."),
            viewModel.uiState.value,
        )
    }
}

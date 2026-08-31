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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.fixmy.twodrive.core.auth.AuthError
import codes.fixmy.twodrive.core.auth.AuthException
import codes.fixmy.twodrive.core.auth.AuthRepository
import codes.fixmy.twodrive.core.auth.AuthState
import codes.fixmy.twodrive.core.model.data.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val signInError = MutableStateFlow<AuthError?>(null)

    val uiState: StateFlow<MainActivityUiState> = combine(
        authRepository.authState,
        signInError,
    ) { authState, error ->
        when (authState) {
            AuthState.Loading -> MainActivityUiState.Loading
            AuthState.SignedOut -> MainActivityUiState.SignedOut(error)
            is AuthState.SignedIn -> MainActivityUiState.SignedIn(authState.profile)
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            signInError.value = null
            try {
                authRepository.signIn(activity)
            } catch (e: AuthException) {
                signInError.value = e.error
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepository.signOut() }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class SignedOut(val error: AuthError?) : MainActivityUiState

    data class SignedIn(val profile: UserProfile) : MainActivityUiState

    /**
     * Returns `true` while the sign-in state is unknown, so the splash screen stays up.
     */
    fun shouldKeepSplashScreen() = this is Loading
}

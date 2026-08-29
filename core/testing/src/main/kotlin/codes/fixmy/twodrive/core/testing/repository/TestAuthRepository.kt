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

package codes.fixmy.twodrive.core.testing.repository

import android.app.Activity
import codes.fixmy.twodrive.core.auth.AuthRepository
import codes.fixmy.twodrive.core.auth.AuthState
import codes.fixmy.twodrive.core.model.data.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestAuthRepository : AuthRepository {

    private val state = MutableStateFlow<AuthState>(AuthState.Loading)

    override val authState: Flow<AuthState> = state

    override suspend fun signIn(activity: Activity) {
        state.value = AuthState.SignedIn(testProfile)
    }

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    /**
     * A test-only API to set the auth state directly.
     */
    fun setAuthState(authState: AuthState) {
        state.value = authState
    }

    companion object {
        val testProfile = UserProfile(displayName = "Test User", email = "test@example.com")
    }
}

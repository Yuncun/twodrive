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

package codes.fixmy.twodrive.core.auth.demo

import android.app.Activity
import codes.fixmy.twodrive.core.auth.AccessTokenProvider
import codes.fixmy.twodrive.core.auth.AuthRepository
import codes.fixmy.twodrive.core.auth.AuthState
import codes.fixmy.twodrive.core.model.data.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo flavor: a fixed demo account that starts signed in, so the app can run with no network
 * and no Microsoft account.
 */
@Singleton
class DemoAuthRepository @Inject constructor() : AuthRepository, AccessTokenProvider {

    private val state = MutableStateFlow<AuthState>(AuthState.SignedIn(DEMO_PROFILE))

    override val authState: Flow<AuthState> = state

    override suspend fun signIn(activity: Activity) {
        state.value = AuthState.SignedIn(DEMO_PROFILE)
    }

    override suspend fun signOut() {
        state.value = AuthState.SignedOut
    }

    override suspend fun accessToken(): String? =
        if (state.value is AuthState.SignedIn) DEMO_TOKEN else null

    companion object {
        val DEMO_PROFILE = UserProfile(displayName = "Demo User", email = "demo@example.com")
        const val DEMO_TOKEN = "demo-access-token"
    }
}

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
import app.cash.turbine.test
import codes.fixmy.twodrive.core.auth.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class DemoAuthRepositoryTest {

    private val repository = DemoAuthRepository()

    @Test
    fun startsSignedInWithDemoProfile() = runTest {
        repository.authState.test {
            val state = assertIs<AuthState.SignedIn>(awaitItem())
            assertEquals(DemoAuthRepository.DEMO_PROFILE, state.profile)
        }
        assertEquals(DemoAuthRepository.DEMO_TOKEN, repository.accessToken())
    }

    @Test
    fun signOutClearsTokenAndSignInRestoresIt() = runTest {
        repository.signOut()
        assertIs<AuthState.SignedOut>(repository.authState.first())
        assertNull(repository.accessToken())

        repository.signIn(Activity())
        assertIs<AuthState.SignedIn>(repository.authState.first())
    }
}

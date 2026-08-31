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

package codes.fixmy.twodrive.core.auth

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Sign-in with a personal Microsoft account.
 */
interface AuthRepository {
    val authState: Flow<AuthState>

    /**
     * Starts the interactive sign-in flow. The result is reflected in [authState].
     *
     * @throws AuthException if the sign-in fails or is cancelled by the user.
     */
    suspend fun signIn(activity: Activity)

    suspend fun signOut()
}

/**
 * A failed auth operation. [error] is what the UI may show, mapped to a localised message
 * there; [message] and [cause] carry the provider detail for logs only and must never
 * reach the screen.
 */
class AuthException(
    val error: AuthError,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * The kinds of auth failure the UI distinguishes.
 */
enum class AuthError {
    CANCELLED,
    NO_NETWORK,
    SERVICE,
    UNKNOWN,
}

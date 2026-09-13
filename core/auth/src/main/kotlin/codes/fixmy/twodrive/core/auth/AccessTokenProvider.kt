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

interface AccessTokenProvider {
    /**
     * Returns a valid access token, refreshing silently when needed, or `null` if nobody is
     * signed in or the account needs to sign in again.
     *
     * @throws AuthException if a token could not be obtained for another reason, e.g. no network.
     */
    suspend fun accessToken(): String?

    /**
     * Returns a new access token, bypassing any cached one, after the server rejected the
     * current token. Returns `null` when the account can no longer refresh silently; the
     * provider is then signed out so the UI prompts for an interactive sign-in.
     *
     * @throws AuthException if a token could not be obtained for another reason, e.g. no network.
     */
    suspend fun refreshAccessToken(): String?

    /**
     * Signs out because the server keeps rejecting freshly issued tokens, so the UI prompts for
     * an interactive sign-in.
     */
    suspend fun requireSignIn()
}

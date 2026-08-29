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

package codes.fixmy.twodrive.core.auth.msal

import android.app.Activity
import android.content.Context
import codes.fixmy.twodrive.core.auth.AccessTokenProvider
import codes.fixmy.twodrive.core.auth.AuthException
import codes.fixmy.twodrive.core.auth.AuthRepository
import codes.fixmy.twodrive.core.auth.AuthState
import codes.fixmy.twodrive.core.auth.GraphScopes
import codes.fixmy.twodrive.core.auth.R
import codes.fixmy.twodrive.core.common.network.Dispatcher
import codes.fixmy.twodrive.core.common.network.TwoDriveDispatchers.IO
import codes.fixmy.twodrive.core.common.network.di.ApplicationScope
import codes.fixmy.twodrive.core.model.data.UserProfile
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Production flavor: signs in a personal Microsoft account through MSAL for Android in
 * single-account mode.
 */
@Singleton
class MsalAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope scope: CoroutineScope,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository,
    AccessTokenProvider {

    private val state = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: Flow<AuthState> = state.asStateFlow()

    /**
     * MSAL reads its configuration and token cache from disk, so it is created lazily off the
     * main thread and shared by every call.
     */
    private val client: Deferred<ISingleAccountPublicClientApplication> = scope.async(ioDispatcher) {
        val app = createClient()
        state.value = app.currentAccount()?.toSignedIn() ?: AuthState.SignedOut
        app
    }

    override suspend fun signIn(activity: Activity) {
        val app = client.await()
        val result = suspendCancellableCoroutine { continuation ->
            val parameters = SignInParameters.builder()
                .withActivity(activity)
                .withScopes(GraphScopes.ALL)
                .withCallback(
                    object : AuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) =
                            continuation.resume(authenticationResult)

                        override fun onError(exception: MsalException) =
                            continuation.resumeWithException(
                                AuthException("Sign-in failed: ${exception.errorCode}", exception),
                            )

                        override fun onCancel() =
                            continuation.resumeWithException(AuthException("Sign-in cancelled"))
                    },
                )
                .build()
            app.signIn(parameters)
        }
        state.value = result.account.toSignedIn()
    }

    override suspend fun signOut() {
        val app = client.await()
        withContext(ioDispatcher) { app.signOut() }
        state.value = AuthState.SignedOut
    }

    override suspend fun accessToken(): String? {
        val app = client.await()
        val account = withContext(ioDispatcher) { app.currentAccount() } ?: return null
        return suspendCancellableCoroutine { continuation ->
            val parameters = AcquireTokenSilentParameters.Builder()
                .withScopes(GraphScopes.ALL)
                .forAccount(account)
                .fromAuthority(account.authority)
                .withCallback(
                    object : SilentAuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) =
                            continuation.resume(authenticationResult.accessToken)

                        override fun onError(exception: MsalException) =
                            continuation.resumeWithException(
                                AuthException("Token refresh failed: ${exception.errorCode}", exception),
                            )
                    },
                )
                .build()
            app.acquireTokenSilentAsync(parameters)
        }
    }

    private suspend fun createClient(): ISingleAccountPublicClientApplication =
        suspendCancellableCoroutine { continuation ->
            PublicClientApplication.createSingleAccountPublicClientApplication(
                context,
                R.raw.msal_config,
                object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                    override fun onCreated(application: ISingleAccountPublicClientApplication) =
                        continuation.resume(application)

                    override fun onError(exception: MsalException) =
                        continuation.resumeWithException(
                            AuthException("MSAL initialisation failed", exception),
                        )
                },
            )
        }

    private fun ISingleAccountPublicClientApplication.currentAccount(): IAccount? =
        currentAccount?.currentAccount

    private fun IAccount.toSignedIn() = AuthState.SignedIn(
        UserProfile(
            displayName = claims?.get("name")?.toString() ?: username,
            email = username,
        ),
    )
}

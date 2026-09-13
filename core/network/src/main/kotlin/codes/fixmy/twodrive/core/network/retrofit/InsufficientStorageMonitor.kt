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

package codes.fixmy.twodrive.core.network.retrofit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches every Graph response for 507 Insufficient Storage, which Graph returns when the
 * account's quota is full, so the account drawer can show the full state whichever call hit it.
 */
@Singleton
class InsufficientStorageMonitor @Inject constructor() : Interceptor {

    private val storageFull = MutableStateFlow(false)

    /** True once any request was refused with 507, until [clear] is called. */
    val isStorageFull: StateFlow<Boolean> = storageFull.asStateFlow()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == HTTP_INSUFFICIENT_STORAGE) storageFull.value = true
        return response
    }

    /** Forgets an earlier 507, e.g. after a fresh quota read shows free space. */
    fun clear() {
        storageFull.value = false
    }

    companion object {
        const val HTTP_INSUFFICIENT_STORAGE = 507
    }
}

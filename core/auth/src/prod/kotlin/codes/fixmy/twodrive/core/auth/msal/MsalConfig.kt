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

/**
 * `res/raw/msal_config.json` spells its redirect URI host as `${applicationId}` because
 * each build type installs under its own package name (`codes.fixmy.twodrive.debug`,
 * `codes.fixmy.twodrive`), and MSAL rejects a redirect URI whose host is not the running package.
 */
internal object MsalConfig {
    const val APPLICATION_ID_PLACEHOLDER = "${'$'}{applicationId}"

    fun resolve(template: String, applicationId: String): String {
        require(APPLICATION_ID_PLACEHOLDER in template) {
            "msal_config.json has no $APPLICATION_ID_PLACEHOLDER redirect URI host"
        }
        return template.replace(APPLICATION_ID_PLACEHOLDER, applicationId)
    }
}

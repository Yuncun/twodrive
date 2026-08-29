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

plugins {
    alias(libs.plugins.twodrive.android.library)
    alias(libs.plugins.twodrive.hilt)
}

android {
    namespace = "codes.fixmy.twodrive.core.auth"
}

dependencies {
    api(projects.core.common)
    api(projects.core.model)

    implementation(libs.kotlinx.coroutines.android)

    // MSAL is only linked into the prod flavor; the demo flavor is always signed in with a fake.
    prodImplementation(libs.msal)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

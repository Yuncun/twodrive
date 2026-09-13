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

package codes.fixmy.twodrive.feature.files.impl

/** Why a folder could not be created, shown as a snackbar over the list. */
enum class CreateFolderError {
    /**
     * Graph refused with 507 because the account is over quota. OneDrive answers this with a
     * full-screen frozen-account page; TwoDrive keeps the user on the list
     * (docs/ux-reference/spec/add-menu.md).
     */
    STORAGE_FULL,

    /** Any other failure, such as no connection. */
    FAILED,
}

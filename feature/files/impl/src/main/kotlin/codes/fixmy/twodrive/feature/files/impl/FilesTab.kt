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

/**
 * The pivot tabs of the Files screen, in display order (docs/ux-reference/11-myfiles.png).
 * Only [MY_FILES] lists the drive; the other tabs show empty states for now.
 */
enum class FilesTab {
    HOME,
    MY_FILES,
    SHARED,
    VAULT,
    OFFLINE,
}

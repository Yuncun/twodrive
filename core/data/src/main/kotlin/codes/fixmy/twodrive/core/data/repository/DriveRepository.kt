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

package codes.fixmy.twodrive.core.data.repository

import codes.fixmy.twodrive.core.model.data.Drive
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's drive and its storage quota, from Graph `/me/drive`.
 */
interface DriveRepository {
    /** The last drive read from Graph, or null before the first successful [refresh]. */
    val drive: Flow<Drive?>

    /** Re-reads `/me/drive`. Returns false if the read failed; [drive] then keeps its last value. */
    suspend fun refresh(): Boolean
}

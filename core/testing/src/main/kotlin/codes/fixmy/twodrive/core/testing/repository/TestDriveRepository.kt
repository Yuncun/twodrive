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

package codes.fixmy.twodrive.core.testing.repository

import codes.fixmy.twodrive.core.data.repository.DriveRepository
import codes.fixmy.twodrive.core.model.data.Drive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestDriveRepository : DriveRepository {

    private val driveFlow = MutableStateFlow<Drive?>(null)

    override val drive: Flow<Drive?> = driveFlow

    var refreshCount: Int = 0
        private set

    override suspend fun refresh(): Boolean {
        refreshCount++
        return true
    }

    /**
     * A test-only API to set the drive directly.
     */
    fun setDrive(drive: Drive?) {
        driveFlow.value = drive
    }
}

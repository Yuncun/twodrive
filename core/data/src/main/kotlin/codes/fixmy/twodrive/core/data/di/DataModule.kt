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

package codes.fixmy.twodrive.core.data.di

import codes.fixmy.twodrive.core.data.repository.DriveItemsRepository
import codes.fixmy.twodrive.core.data.repository.OfflineFirstDriveItemsRepository
import codes.fixmy.twodrive.core.data.repository.OfflineFirstUserDataRepository
import codes.fixmy.twodrive.core.data.repository.UserDataRepository
import codes.fixmy.twodrive.core.data.util.ConnectivityManagerNetworkMonitor
import codes.fixmy.twodrive.core.data.util.InProcessSyncManager
import codes.fixmy.twodrive.core.data.util.NetworkMonitor
import codes.fixmy.twodrive.core.data.util.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsDriveItemsRepository(
        driveItemsRepository: OfflineFirstDriveItemsRepository,
    ): DriveItemsRepository

    @Binds
    internal abstract fun bindsUserDataRepository(
        userDataRepository: OfflineFirstUserDataRepository,
    ): UserDataRepository

    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor

    @Binds
    internal abstract fun bindsSyncManager(
        syncManager: InProcessSyncManager,
    ): SyncManager
}

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

package codes.fixmy.twodrive.core.data.testdoubles

import codes.fixmy.twodrive.core.database.dao.DriveItemDao
import codes.fixmy.twodrive.core.database.model.DriveItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [DriveItemDao] mirroring the SQL in the real one.
 */
class TestDriveItemDao : DriveItemDao {
    private val entities = MutableStateFlow(emptyMap<String, DriveItemEntity>())

    override fun getDriveItem(id: String): Flow<DriveItemEntity?> = entities.map { it[id] }

    override fun getRoot(): Flow<DriveItemEntity?> = entities.map { all -> all.values.firstOrNull { it.isRoot } }

    override fun getChildren(parentId: String): Flow<List<DriveItemEntity>> =
        entities.map { all -> all.values.filter { it.parentId == parentId } }

    override fun getRootChildren(): Flow<List<DriveItemEntity>> = entities.map { all ->
        val rootId = all.values.firstOrNull { it.isRoot }?.id
        all.values.filter { it.parentId == rootId && rootId != null }
    }

    override suspend fun upsertDriveItems(entities: List<DriveItemEntity>) =
        this.entities.update { it + entities.associateBy(DriveItemEntity::id) }

    /** Mirrors the real DAO's recursive delete: descendants of a deleted id go too. */
    override suspend fun deleteDriveItems(ids: List<String>) =
        entities.update { all ->
            val doomed = ids.toMutableSet()
            var grew = true
            while (grew) {
                val children = all.values.filter { it.parentId in doomed && it.id !in doomed }
                grew = children.isNotEmpty()
                doomed += children.map { it.id }
            }
            all - doomed
        }

    override suspend fun deleteAll() = entities.update { emptyMap() }
}

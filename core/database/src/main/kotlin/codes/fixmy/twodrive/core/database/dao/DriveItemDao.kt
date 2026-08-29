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

package codes.fixmy.twodrive.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import codes.fixmy.twodrive.core.database.model.DriveItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [DriveItemEntity] access.
 */
@Dao
interface DriveItemDao {
    @Query("SELECT * FROM drive_items WHERE id = :id")
    fun getDriveItem(id: String): Flow<DriveItemEntity?>

    @Query("SELECT * FROM drive_items WHERE is_root = 1 LIMIT 1")
    fun getRoot(): Flow<DriveItemEntity?>

    @Query("SELECT * FROM drive_items WHERE parent_id = :parentId")
    fun getChildren(parentId: String): Flow<List<DriveItemEntity>>

    @Query(
        """
        SELECT * FROM drive_items
        WHERE parent_id = (SELECT id FROM drive_items WHERE is_root = 1 LIMIT 1)
        """,
    )
    fun getRootChildren(): Flow<List<DriveItemEntity>>

    @Upsert
    suspend fun upsertDriveItems(entities: List<DriveItemEntity>)

    @Query("DELETE FROM drive_items WHERE id IN (:ids)")
    suspend fun deleteDriveItems(ids: List<String>)

    @Query("DELETE FROM drive_items")
    suspend fun deleteAll()
}

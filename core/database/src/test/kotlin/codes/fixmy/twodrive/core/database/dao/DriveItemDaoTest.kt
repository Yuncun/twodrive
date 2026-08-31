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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import codes.fixmy.twodrive.core.database.TwoDriveDatabase
import codes.fixmy.twodrive.core.database.model.DriveItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class DriveItemDaoTest {

    private lateinit var db: TwoDriveDatabase
    private lateinit var dao: DriveItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TwoDriveDatabase::class.java).build()
        dao = db.driveItemDao()
    }

    @After
    fun teardown() = db.close()

    @Test
    fun getDriveItemEmitsTheRowOrNull() = runTest {
        dao.upsertDriveItems(testDriveTree)

        assertEquals("Documents", dao.getDriveItem("folder-documents").first()?.name)
        assertNull(dao.getDriveItem("no-such-id").first())
    }

    @Test
    fun getRootFindsTheSingleRootRow() = runTest {
        dao.upsertDriveItems(testDriveTree)

        assertEquals("root", dao.getRoot().first()?.id)
    }

    @Test
    fun getChildrenReturnsOnlyDirectChildren() = runTest {
        dao.upsertDriveItems(testDriveTree)

        val children = dao.getChildren("folder-documents").first()

        assertEquals(listOf("file-resume"), children.map { it.id })
    }

    @Test
    fun getRootChildrenResolvesTheRootThroughTheSubquery() = runTest {
        dao.upsertDriveItems(testDriveTree)

        val children = dao.getRootChildren().first()

        assertEquals(
            setOf("folder-documents", "file-budget"),
            children.map { it.id }.toSet(),
        )
    }

    @Test
    fun upsertReplacesAnExistingRowById() = runTest {
        dao.upsertDriveItems(testDriveTree)

        dao.upsertDriveItems(
            listOf(
                testDriveItem(id = "file-budget", parentId = "root", name = "Budget 2027.xlsx"),
            ),
        )

        assertEquals("Budget 2027.xlsx", dao.getDriveItem("file-budget").first()?.name)
        assertEquals(
            setOf("folder-documents", "file-budget"),
            dao.getRootChildren().first().map { it.id }.toSet(),
        )
    }

    @Test
    fun deleteDriveItemsRemovesOnlyTheNamedIds() = runTest {
        dao.upsertDriveItems(testDriveTree)

        dao.deleteDriveItems(listOf("file-budget", "no-such-id"))

        assertNull(dao.getDriveItem("file-budget").first())
        assertEquals(listOf("folder-documents"), dao.getRootChildren().first().map { it.id })
    }

    @Test
    fun deleteDriveItemsSweepsDescendantsOfADeletedFolder() = runTest {
        dao.upsertDriveItems(testDriveTree)
        // Nest one level deeper: Documents > Archive > old-notes.txt.
        dao.upsertDriveItems(
            listOf(
                testDriveItem(id = "folder-archive", parentId = "folder-documents", isFolder = true),
                testDriveItem(id = "file-old-notes", parentId = "folder-archive"),
            ),
        )

        dao.deleteDriveItems(listOf("folder-documents"))

        assertNull(dao.getDriveItem("folder-documents").first())
        assertNull(dao.getDriveItem("file-resume").first())
        assertNull(dao.getDriveItem("folder-archive").first())
        assertNull(dao.getDriveItem("file-old-notes").first())
        assertEquals(listOf("file-budget"), dao.getRootChildren().first().map { it.id })
    }

    @Test
    fun deleteAllEmptiesTheTable() = runTest {
        dao.upsertDriveItems(testDriveTree)

        dao.deleteAll()

        assertNull(dao.getRoot().first())
        assertEquals(emptyList(), dao.getRootChildren().first())
    }
}

/** A root with one folder ("Documents" holding one file) and one loose file. */
private val testDriveTree = listOf(
    testDriveItem(id = "root", parentId = null, isRoot = true, isFolder = true, name = "root"),
    testDriveItem(id = "folder-documents", parentId = "root", isFolder = true, name = "Documents"),
    testDriveItem(id = "file-budget", parentId = "root", name = "Household budget.xlsx"),
    testDriveItem(id = "file-resume", parentId = "folder-documents", name = "Resume 2026.docx"),
)

private fun testDriveItem(
    id: String,
    parentId: String?,
    name: String = id,
    isRoot: Boolean = false,
    isFolder: Boolean = false,
) = DriveItemEntity(
    id = id,
    name = name,
    isFolder = isFolder,
    isRoot = isRoot,
    size = 1_024,
    lastModified = Instant.parse("2026-02-04T12:00:00Z"),
    mimeType = if (isFolder) null else "application/octet-stream",
    parentId = parentId,
    webUrl = null,
    thumbnailUrl = null,
    childCount = if (isFolder) 1 else null,
)

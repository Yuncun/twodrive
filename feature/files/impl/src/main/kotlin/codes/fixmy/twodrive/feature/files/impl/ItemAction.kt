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

import codes.fixmy.twodrive.core.model.data.DriveItem

/**
 * An entry in the item bottom sheet. Tiles sit in the grey row under the header; the rest are
 * list rows (docs/ux-reference/spec/item-bottom-sheet.md).
 */
enum class ItemAction(val isTile: Boolean) {
    SHARE(isTile = true),
    DELETE(isTile = true),
    DOWNLOAD(isTile = true),
    RENAME(isTile = false),
    MOVE(isTile = false),
    DETAILS(isTile = false),
}

/** The list a sheet was opened from, which decides the actions it offers alongside the item. */
enum class ItemSource {
    /** Home ▸ Recent files: the item's real folder is not on screen. */
    HOME_RECENT,

    /** My files or a pushed folder. */
    FOLDER,
}

/**
 * The actions the sheet offers for [item] opened from [source], in display order. Folders never
 * offer Download, and Home's recents drop the structural and destructive actions (Delete, Move)
 * that OneDrive only offers where the item's folder is on screen.
 */
fun itemActions(item: DriveItem, source: ItemSource): List<ItemAction> = ItemAction.entries.filter { action ->
    when (action) {
        ItemAction.DOWNLOAD -> !item.isFolder
        ItemAction.DELETE, ItemAction.MOVE -> source == ItemSource.FOLDER
        ItemAction.SHARE, ItemAction.RENAME, ItemAction.DETAILS -> true
    }
}

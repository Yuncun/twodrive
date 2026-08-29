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

package codes.fixmy.twodrive.core.model.data

/**
 * Sorts a folder listing the way OneDrive does: folders first, then files, each group ordered by
 * [sortOrder].
 */
fun List<DriveItem>.sortedBy(sortOrder: SortOrder): List<DriveItem> {
    val comparator: Comparator<DriveItem> = when (sortOrder) {
        SortOrder.NAME_ASCENDING -> compareBy(String.CASE_INSENSITIVE_ORDER, DriveItem::name)
        SortOrder.NAME_DESCENDING -> compareByDescending(String.CASE_INSENSITIVE_ORDER, DriveItem::name)
        SortOrder.MODIFIED_NEWEST_FIRST -> compareByDescending(DriveItem::lastModified)
        SortOrder.MODIFIED_OLDEST_FIRST -> compareBy(DriveItem::lastModified)
        SortOrder.SIZE_LARGEST_FIRST -> compareByDescending(DriveItem::size)
    }
    return sortedWith(compareByDescending(DriveItem::isFolder).then(comparator))
}

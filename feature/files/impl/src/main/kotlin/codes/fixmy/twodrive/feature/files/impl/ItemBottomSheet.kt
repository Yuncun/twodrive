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

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.model.data.DriveItem
import codes.fixmy.twodrive.core.ui.DriveItemThumbnail
import codes.fixmy.twodrive.core.ui.icon
import codes.fixmy.twodrive.core.ui.iconTint
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * The item "more options" sheet opened from a row's or tile's "⋯": a content-height modal sheet
 * with the item's thumbnail, name and "size · date", a row of grey action tiles, then action rows
 * (docs/ux-reference/spec/item-bottom-sheet.md, 12-item-more-options.png). Tapping an action hides
 * the sheet, calls [onDismissRequest] and hands the action to [onAction].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ItemBottomSheet(
    item: DriveItem,
    source: ItemSource,
    today: LocalDate,
    onDismissRequest: () -> Unit,
    onAction: (ItemAction, DriveItem) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    ) {
        ItemBottomSheetContent(
            item = item,
            actions = remember(item, source) { itemActions(item, source) },
            today = today,
            onAction = { action ->
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismissRequest() }
                onAction(action, item)
            },
        )
    }
}

/** The sheet's body without the modal container, so it can be rendered and tested in place. */
@Composable
internal fun ItemBottomSheetContent(
    item: DriveItem,
    actions: List<ItemAction>,
    today: LocalDate,
    onAction: (ItemAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .testTag("files:itemSheet"),
    ) {
        ItemSheetHeader(item = item, today = today)
        val (tiles, rows) = actions.partition(ItemAction::isTile)
        if (tiles.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tiles.forEach { action ->
                    ItemActionTile(
                        action = action,
                        onClick = { onAction(action) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        rows.forEach { action ->
            ItemActionRow(action = action, onClick = { onAction(action) })
        }
    }
}

@Composable
private fun ItemSheetHeader(item: DriveItem, today: LocalDate) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 96.dp, height = 72.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            DriveItemThumbnail(item = item, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = item.icon(),
                    contentDescription = null,
                    tint = item.iconTint(),
                    modifier = Modifier.size(72.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = item.subtitle(today),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * A grey 80dp tile. No M3 component fits, and Delete carries no destructive styling, matching
 * OneDrive.
 */
@Composable
private fun ItemActionTile(
    action: ItemAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier.height(80.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(imageVector = action.icon(), contentDescription = null)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(action.labelRes()), style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** A 48dp row: 24dp icon at x=16, label at x=56, no divider. */
@Composable
private fun ItemActionRow(action: ItemAction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = action.icon(), contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = stringResource(action.labelRes()), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun ItemAction.icon(): ImageVector = when (this) {
    ItemAction.SHARE -> TwoDriveIcons.Share
    ItemAction.DELETE -> TwoDriveIcons.Delete
    ItemAction.DOWNLOAD -> TwoDriveIcons.Download
    ItemAction.RENAME -> TwoDriveIcons.Rename
    ItemAction.MOVE -> TwoDriveIcons.Move
    ItemAction.DETAILS -> TwoDriveIcons.Details
}

@StringRes
private fun ItemAction.labelRes(): Int = when (this) {
    ItemAction.SHARE -> R.string.feature_files_impl_item_action_share
    ItemAction.DELETE -> R.string.feature_files_impl_item_action_delete
    ItemAction.DOWNLOAD -> R.string.feature_files_impl_item_action_download
    ItemAction.RENAME -> R.string.feature_files_impl_item_action_rename
    ItemAction.MOVE -> R.string.feature_files_impl_item_action_move
    ItemAction.DETAILS -> R.string.feature_files_impl_item_action_details
}

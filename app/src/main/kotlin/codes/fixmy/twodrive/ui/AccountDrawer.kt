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

package codes.fixmy.twodrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codes.fixmy.twodrive.R
import codes.fixmy.twodrive.core.designsystem.icon.TwoDriveIcons
import codes.fixmy.twodrive.core.model.data.Drive
import codes.fixmy.twodrive.core.model.data.UserProfile
import codes.fixmy.twodrive.core.ui.formatStorageSize
import codes.fixmy.twodrive.core.ui.storagePercent

/**
 * The account drawer's sheet, following docs/ux-reference/spec/account-drawer.md: a fixed header
 * (account tile and email), a scrolling middle (the banner slot and the link rows), and a fixed
 * quota footer that never scrolls away.
 *
 * A full quota is shown only here, by turning the footer's icon, used amount and bar red-orange;
 * the app raises no dialog or snackbar for it.
 */
@Composable
fun AccountDrawerSheet(
    profile: UserProfile,
    uiState: AccountDrawerUiState,
    onSignOut: () -> Unit,
    onViewPlan: () -> Unit,
    onRetrySync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(DrawerWidth)
            .testTag("accountDrawer"),
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.fillMaxHeight()) {
            AccountSwitcher(profile)
            HorizontalDivider()
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp)
                    .padding(horizontal = DrawerGutter, vertical = 10.dp),
            )
            HorizontalDivider()
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                uiState.banner?.let { banner ->
                    DrawerBanner(banner = banner, onRetrySync = onRetrySync)
                    HorizontalDivider()
                }
                DrawerLinkRow(
                    icon = {
                        Icon(
                            TwoDriveIcons.SignOut,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    label = stringResource(R.string.account_drawer_sign_out),
                    onClick = onSignOut,
                    modifier = Modifier.testTag("accountDrawer:signOut"),
                )
            }
            StorageQuotaFooter(drive = uiState.drive, onViewPlan = onViewPlan)
        }
    }
}

@Composable
private fun AccountSwitcher(profile: UserProfile) {
    val tileDescription = stringResource(R.string.account_drawer_tile_description, profile.email)
    Column(
        Modifier
            .padding(top = 6.dp)
            .width(AccountTileWidth)
            .clearAndSetSemantics { contentDescription = tileDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AccountAvatar(
            displayName = profile.displayName,
            size = 48.dp,
            modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp),
        )
        Text(
            text = stringResource(R.string.account_drawer_personal),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp),
        )
        // The tab-style indicator under the current account's tile.
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.primary),
        )
    }
}

@Composable
private fun DrawerBanner(banner: AccountDrawerBanner, onRetrySync: () -> Unit) {
    val (icon, title, body) = when (banner) {
        AccountDrawerBanner.OFFLINE -> Triple(
            TwoDriveIcons.Offline,
            R.string.account_drawer_banner_offline_title,
            R.string.account_drawer_banner_offline_body,
        )
        AccountDrawerBanner.SYNC_FAILED -> Triple(
            TwoDriveIcons.SyncProblem,
            R.string.account_drawer_banner_sync_failed_title,
            R.string.account_drawer_banner_sync_failed_body,
        )
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = DrawerGutter, end = DrawerGutter, top = 16.dp, bottom = 16.dp)
            .testTag("accountDrawer:banner"),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(DrawerGutter))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (banner == AccountDrawerBanner.SYNC_FAILED) {
                OutlinedButton(
                    onClick = onRetrySync,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.account_drawer_banner_sync_failed_action))
                }
            }
        }
    }
}

@Composable
private fun DrawerLinkRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LinkRowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = DrawerGutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(DrawerGutter))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StorageQuotaFooter(drive: Drive?, onViewPlan: () -> Unit) {
    val isFull = drive?.isQuotaFull == true
    val accent = if (isFull) QuotaFullColor else MaterialTheme.colorScheme.primary
    val title = stringResource(R.string.account_drawer_storage_title)
    val description = drive?.let {
        stringResource(
            R.string.account_drawer_storage_used,
            formatStorageSize(it.quotaUsed),
            formatStorageSize(it.quotaTotal),
            storagePercent(it.quotaUsed, it.quotaTotal),
        )
    } ?: stringResource(R.string.account_drawer_storage_loading)

    Column(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = DrawerGutter, end = DrawerGutter, top = 12.dp, bottom = 16.dp)
                .clearAndSetSemantics { contentDescription = "$title, $description" }
                .testTag("accountDrawer:quota"),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp)) {
                    Icon(
                        imageVector = TwoDriveIcons.Cloud,
                        contentDescription = null,
                        tint = if (isFull) QuotaFullColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isFull) {
                        Icon(
                            imageVector = TwoDriveIcons.Cancel,
                            contentDescription = null,
                            tint = QuotaFullColor,
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.width(DrawerGutter))
                Text(title, style = MaterialTheme.typography.bodyLarge)
            }
            Column(Modifier.padding(start = 24.dp + DrawerGutter, top = 12.dp)) {
                Text(
                    text = if (drive == null) {
                        buildAnnotatedString { append(description) }
                    } else {
                        val used = formatStorageSize(drive.quotaUsed)
                        buildAnnotatedString {
                            // Over quota, the used amount is bold red-orange; the rest stays grey.
                            val index = description.indexOf(used)
                            append(description)
                            if (index >= 0 && isFull) {
                                addStyle(
                                    SpanStyle(color = QuotaFullColor, fontWeight = FontWeight.Bold),
                                    index,
                                    index + used.length,
                                )
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (drive != null) {
                    LinearProgressIndicator(
                        progress = { drive.quotaFraction },
                        color = accent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {},
                        gapSize = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .height(4.dp),
                    )
                }
            }
        }
        HorizontalDivider()
        Button(
            onClick = onViewPlan,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(DrawerGutter)
                .height(52.dp),
        ) {
            Text(stringResource(R.string.account_drawer_view_plan), fontWeight = FontWeight.Bold)
        }
    }
}

/** OneDrive's red-orange for a full quota; the only signal the app gives that storage is full. */
internal val QuotaFullColor = Color(0xFFD83B01)

/** Microsoft's storage management page, opened by "View Plan". */
internal const val VIEW_PLAN_URL = "https://onedrive.live.com/?v=managestorage"

private val DrawerWidth = 305.dp
private val DrawerGutter = 16.dp
private val AccountTileWidth = 96.dp
private val LinkRowHeight = 48.4.dp

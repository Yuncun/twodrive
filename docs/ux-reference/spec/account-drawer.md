# Screen: account-drawer (navigation drawer)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → any Files tab.
2. Tap the round avatar `ImageButton` at the top-left of the app bar, `[11,142][158,289]` px
   (content description **"Open account drawer"**), i.e. a 56 dp target at x 4.2 dp, y 54.1 dp.

Screenshot: **not committed** — it shows the owner's email and avatar.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/account-drawer.png`
(+ `account-drawer.xml`). `docs/ux-reference/19-account-drawer.png` is cited by the backlog but has
never been committed; it should stay uncommitted for the same privacy reason.

## Structure
A **modal navigation drawer** sliding in from the left: `drawer_constraint_layout` `[0,0][801,2424]` px
= **305 dp wide**, full height, white, with rounded right corners (~16 dp). The rest of the screen
keeps the Files list visible under a grey scrim; the search pill and FAB stay visible but dimmed.
Content description while open: `"Navigation drawer opened"`.

Three bands: a header, a scrolling middle (`scroll_view` [0,549][801,1884]), and a **fixed footer**
(`fixed_footer` [0,1884][801,2361]) pinned to the bottom regardless of scroll.

### Header — account switcher (`od3_account_switcher`, [0,142][801,433] px → y 54.1..165 dp)
A horizontally scrolling row of account tiles, each 252 px = **96 dp** wide.

| Tile | Avatar bounds (px) | dp | Label |
|---|---|---|---|
| Personal (current) | `[52,158][199,305]` | **56 dp circle** at x 19.8, y 60.2 | **"Personal"**, blue, `[63,323][189,407]` px → y 123..155 dp |
| Add account | `[304,158][451,305]` | 56 dp circle at x 115.8 | **"Add account"**, grey |

- The current account's avatar carries a **2 px blue ring** around the circle; "Add account" is a flat
  grey circle with a grey "+".
- A **3 px (≈1 dp) blue selection indicator** runs under the selected tile only, x 0..252 px (96 dp wide),
  at y 421..427 px — a tab-style indicator, not a pill.
- Whole-tile content description: `"Personal account with email <email>"`.

Then `top_divider` `[0,433][801,436]` — a 3 px (≈1 dp) full-width hairline.

### Email row (`account_item_title`, [0,436][801,546] px → y 166..208 dp, **41.9 dp tall**)
The signed-in address as plain text at the 16 dp gutter, ≈16 sp, dark grey. **Not clickable.**
Followed by another 3 px hairline at y 546..549 px.

### Camera-backup banner (`banner_layout`, [0,549][801,928] px → y 209.1..353.5 dp)
| Part | px | dp |
|---|---|---|
| `banner_icon` (blue crossed-out camera) | `[42,591][105,654]` | **24 dp** at x 16, y 225.1 |
| `banner_primary_text` **"Camera backup is off"** | `[147,591][759,640]` | x **56**, ≈16 sp dark |
| `banner_secondary_text` "Turn on camera backup to automatically back up your photos and videos." | `[147,640][759,777]` | x 56, ≈14 sp grey, 3 lines |
| `banner_button` **"Turn on"** | `[147,809][378,886]` | x 56, y 308.2..337.5 — **88 × 29.3 dp** outlined button, thin grey border, dark label |

Out of scope for TwoDrive (camera backup is excluded), but it fixes the **banner slot**: an icon +
two-line text + a small outlined action, sitting between the email row and the link list, closed by a
hairline `divider` at [0,928][801,931].

### Link list — five rows, pitch 127 px = **48.4 dp**
| Row | `resource-id` | Bounds (px) | Icon (outline, grey, 24 dp at x 16 dp) |
|---|---|---|---|
| **Recycle bin** | `recycle_bin_link` | `[0,931][801,1058]` | wastebasket with recycle arrows |
| **Settings** | `settings_link` | `[0,1058][801,1185]` | gear |
| **Debug Options** | `debug_options_link` | `[0,1185][801,1312]` | bug (debug build only) |
| **Help and feedback** | `help_link` | `[0,1312][801,1439]` | circled "?" |
| **Sign out** | `sign_out_link` | `[0,1439][801,1566]` | door with an out arrow |

Labels start at x 147 px = **56 dp**, ≈16 sp dark grey. **No dividers between rows**, no trailing
chevrons, no section headers, and **Sign out is styled exactly like the others** — no red, no separator.

### Fixed footer — storage quota (`od3_account_quota`, [0,1884][801,2157] px → y 717.7..821.7 dp)
This account is **over quota**, so the footer is showing the error state:

| Part | px | dp | Colour |
|---|---|---|---|
| `settings_quota_icon` (cloud with a filled "✗" badge) | `[42,1916][105,1979]` | 24 dp at x 16, y 729.9 | **red-orange ≈#D83B01** |
| `settings_quota_title` **"Microsoft storage"** | `[147,1916][759,1973]` | x 56 | dark grey, ≈16 sp |
| `settings_quota_description` **"100.3 GB used of 100 GB (100%)"** | `[147,2011][759,2062]` | x 56, y 766..785.5 | the leading amount **"100.3 GB" is bold red-orange**; the rest is grey |
| `settings_quota_progress_bar` | `[147,2062][759,2115]` | x 56..289.1, y 785.5..805.7 | a determinate bar filled 100 % in the same red-orange; ≈4 dp track inside a 20 dp view |
| hairline | `[0,2157][801,2160]` | | |
| `see_plan_button` **"View Plan"** | `[42,2192][759,2329]` | x 16, y 835..887.2 — **273 × 52.2 dp** | filled OneDrive blue, white bold label, radius ≈4 dp |

The header row (`quota_header`, icon + title) is itself clickable; the whole quota block's content
description is `"Microsoft storage, 100.3 GB used of 100 GB (100%)"`.

## Behaviours
- Opens by tapping the avatar **or** by an edge swipe from the left; closes on BACK, on a tap in the
  scrim, or on a right-to-left swipe.
- Tapping the current account tile does nothing visible; "Add account" starts a second sign-in.
- The middle band scrolls independently; the quota footer and **View Plan** never scroll away.
- **Not exercised** (read-only rule): Recycle bin, Settings, Sign out, Turn on, View Plan.
- Quota is the only place in the Files experience that shows storage. When full it does **not** raise a
  dialog or a snackbar — the drawer footer turning red is the entire signal, which is why an upload
  failure elsewhere would be unexplained without opening the drawer.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Container | `ModalNavigationDrawer` + `ModalDrawerSheet(drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp), modifier = Modifier.width(305.dp))` |
| Account switcher | a `Row` of 96 dp tiles; the current one is an `Image`/`Icon` in a 56 dp `Box` with `Modifier.border(2.dp, primary, CircleShape)` and a 1 dp `Box` indicator underneath — hand-built, since no M3 component matches |
| Email row | `Text(style = titleMedium, modifier = Modifier.padding(16.dp))`, non-interactive |
| Banner | reuse for TwoDrive's own notices (offline, sync error): `Row` = 24 dp `Icon` + `Column(titleMedium, bodyMedium)` + `OutlinedButton` |
| Link rows | `NavigationDrawerItem(icon, label, selected = false, onClick, modifier = Modifier.height(48.dp))` — but with `NavigationDrawerItemDefaults.colors()` overridden so there is **no pill/indicator**, and no `Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)` inset: labels must land at 56 dp, flush to the sheet |
| Dividers | `HorizontalDivider()` after the switcher, after the email row, after the banner, and above the footer button — **not** between the link rows |
| Footer | a `Column` outside the scrolling one; `LinearProgressIndicator(progress = { used / total })` with `color = error` when `used >= total`, else `primary` |
| Footer button | `Button(shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().height(52.dp))` |

For TwoDrive's scope the rows collapse to **Settings** (or nothing) and **Sign out**; the drawer must
still carry the avatar, the email and the quota block, because M2.4 depends on all three.

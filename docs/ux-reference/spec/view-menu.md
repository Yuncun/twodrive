# Screen: view-menu ("View as:" dropdown)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → Files ▸ My files.
2. Tap the view-options (sliders) `ImageButton` at the right of the sort bar (px 1001,510).

Screenshot: **not committed** — real file names remain visible beside the menu.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/view-menu.png`
(crop: `view-menu-crop.png`). The repo already carries `docs/ux-reference/14-view-options.png`.

## Structure
A white rounded card **anchored below the view-options button** and right-aligned to it.
No scrim; the list stays visible behind it.

| Property | px | dp |
|---|---|---|
| Card left / right | 507 / 1045 | 193.1 / 398.1 (**205 dp wide**) |
| Card top / bottom | 605 / 970 | 230.5 / 369.5 (**139 dp tall**) |
| Header "View as:" `title` | [505,625][1043,718] | x 192.4, y 238.1..273.5 — **a non-clickable header row**, label at x ≈ 57 dp from the card's left |
| Row pitch | 127 | **48.4** |
| Checkmark x | 578 | 220.2 (**24 dp**), i.e. ~27 dp from the card's left |
| Label x | 631 | **240.4**, i.e. ~47 dp from the card's left |
| Trailing icon x | ~968 | ~368.8 (**24 dp**), right-aligned inside the card |

### Rows
| Label | Bounds (px) | Leading | Trailing icon | State |
|---|---|---|---|---|
| **List** | [631,750][917,807] | ✓ | three stacked horizontal lines, **blue** | selected |
| **Tile** | [631,877][917,934] | — | 2 × 2 rounded squares, **grey** | — |

The trailing icon is tinted with the primary blue on the selected row and grey on the unselected one —
the check and the tint both carry the selection.

## Behaviours
- Tapping **List** / **Tile** switches the content area immediately and closes the menu; see
  `my-files-list.md` and `my-files-tile.md`.
- The choice persists across tab switches and app restarts (the browse container's description flips
  between "List view, two finger swipe to refresh" and "Grid view, two finger swipe to refresh").
- Tapping outside, or BACK, closes with no change.
- Only two options exist — there is no "Details" or "Compact" mode on Android.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Container | `DropdownMenu`, anchored top-end to the `IconButton`, `Modifier.width(205.dp)` |
| Header | a non-interactive row: `Text("View as:", style = titleSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))` — **not** a `DropdownMenuItem` |
| Row | `DropdownMenuItem(leadingIcon = check-or-spacer, text = label, trailingIcon = Icon(tint = if (selected) primary else onSurfaceVariant))` |
| Icons | List → `Icons.AutoMirrored.Filled.ViewList`; Tile → `Icons.Filled.GridView` |
| State | one `ViewMode` enum (`LIST`, `TILE`) in DataStore, shared with the sort preference |

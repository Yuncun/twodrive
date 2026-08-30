# Screen: item-bottom-sheet ("more options" sheet)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → Files ▸ **My files** (or ▸ Home).
2. Tap the trailing **"⋯"** `action_button` on any row (px x ≈ 990, row centre y).

Screenshot: **committed for the folder variant** — `docs/ux-reference/12-item-more-options.png`
(folder name only, no file contents). The file variants are **not committed** (real file names,
photo thumbnails); raw captures at `/Users/ericshen/Claude/twodrive-ref/observed/`:
`item-bottom-sheet-folder.png`, `item-bottom-sheet-file.png`, `item-bottom-sheet-myfile.png`
(+ matching `.xml`).

## Structure
A **modal bottom sheet** (`design_bottom_sheet` → `operations_bottom_sheet_layout`) sized to its
content — it opens fully expanded, never at a half-height peek, and its top edge therefore moves with
the number of rows (y 1111 px for the 4-row folder sheet, y 603 px for the 8-row file sheet).
The content behind is dimmed by a scrim; the sheet is white with rounded **top** corners (≈12 dp).

Bands, top→bottom:

| Band | px (8-row file variant) | dp | Notes |
|---|---|---|---|
| Drag handle `pill_image` | strip `[0,617][1080,723]`, pill ≈96 × 10 px | 40.4 dp strip; pill **36.6 × 3.8 dp**, grey, fully rounded, centred ≈19 dp below the sheet top | clickable, description "Bottom sheet drag handle" |
| `thumbnail_container` | `[0,723][1080,912]` | **72 dp tall**, thumbnail centred | folder → 84 × 72 dp yellow folder art (`folder_thumbnail_background` + `_foreground`); photo/video → 96 × 72 dp frame with a 16 dp `media_icon_overlay` and a 16 dp `item_type_overlay` at bottom-left; plain doc → 56 × 72 dp page render |
| `file_name` | `[84,933][996,984]` | x 32, **centred**, ≈16 sp near-black, single line | |
| `file_details` | `[84,984][996,1093]` | x 32, **centred**, ≈14 sp grey | same `size · date` string as the list row |
| `custom_toolbar` (action tiles) | `[0,1093][1080,1303]` | **80 dp tall** | see below |
| `bottom_operations_list` | `[0,1303][1080,2361]` | 16 dp top padding, then rows | see below |
| bottom inset | 2361..2424 | 24 dp | |

### Action tiles (`custom_toolbar`)
Large square-ish grey tiles filling the width, 16 dp gutters, ≈8 dp corner radius, light-grey fill
(≈#F5F5F5), a 24 dp outline glyph over a ≈13 sp label.

| Variant | Tile bounds (px) | Tile size |
|---|---|---|
| 2 tiles | `[32,1601][540,1811]`, `[540,1601][1048,1811]` | **193.5 × 80 dp** each |
| 3 tiles | `[32,1093][371,1303]`, `[371,…][710,…]`, `[710,…][1048,…]` | **129.1 × 80 dp** each |

### Operation rows (`operation_item_container`)
Pitch 127 px = **48.4 dp**; 24 dp icon at x **16 dp**; label at x **56 dp**, ≈16 sp; no dividers, no
trailing chevrons. Only "Make available offline" has a trailing control: a `Switch` at
`[858,1623][1038,1702]` px → x 326.9..395.4 dp, **68.6 × 30.1 dp**, off by default.
Row content descriptions are `"<Label> button"`.

### The three observed variants
| Where | Tiles | Rows (in order) |
|---|---|---|
| **Home ▸ Recent files**, file | Share · Download | Make available offline (switch) · Rename · Comment · Details |
| **My files**, folder | Share · **Delete** | Make available offline (switch) · Rename · **Move** · Details |
| **My files**, file | Share · **Delete** · Download | **Add to album** · **Add to Favorites** · Make available offline (switch) · Rename · **Copy** · **Move** · Comment · Details |

Reading the differences:
- **Delete lives in the tile row, not the list** — and it is a plain grey tile like Share, with no red
  tint and no destructive styling anywhere in the sheet.
- A **folder** loses Download, Copy, Comment, Add to album and Add to Favorites; it keeps Move.
- **Home's recent-file sheet is deliberately reduced**: no Delete, no Move, no Copy — the structural
  and destructive actions are only offered where the item's real folder is on screen.
- Nothing in the sheet is scrollable in practice: even the 8-row variant fits, ending exactly at the
  24 dp bottom inset.

## Behaviours
- BACK, a scrim tap, or a downward drag dismisses with no action.
- **Not exercised** (read-only rule): every tile and every row, and the offline switch.
- The sheet header repeats the row's own name and `size · date`; it does not add a path or an owner.
- No "Open" / "Open with" entry — tapping the row itself is the only way to open a file.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Container | `ModalBottomSheet(sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))` |
| Handle | `BottomSheetDefaults.DragHandle()` (36 × 4 dp) — the M3 default already matches |
| Header | `Column(horizontalAlignment = CenterHorizontally)`: 72 dp thumbnail `Box` + `Text(titleMedium)` + `Text(bodyMedium, onSurfaceVariant)` |
| Tiles | a `Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = spacedBy(0.dp))` of `Surface(shape = RoundedCornerShape(8.dp), color = surfaceContainerHighest, modifier = Modifier.weight(1f).height(80.dp))`, each a `Column` of `Icon` + `Text(labelMedium)` — no M3 component fits; do **not** substitute `FilledTonalButton` |
| Rows | `ListItem(headlineContent, leadingContent = Icon, trailingContent = Switch?, modifier = Modifier.height(48.dp).clickable { })` with `ListItemDefaults.colors(containerColor = Color.Transparent)` |
| Row set | drive it from a `List<ItemAction>` computed from the item (`isFolder`) **and the source list**, so Home's recents can hand back the reduced set — matching OneDrive rather than hard-coding one menu |

For TwoDrive's M3.2 scope the sheet is: tiles **Share · Delete** (+ Download for files), rows
**Rename · Move · Details**. "Make available offline", "Comment", "Add to album" and "Add to Favorites"
are out of the frozen scope and should be omitted rather than shown disabled.

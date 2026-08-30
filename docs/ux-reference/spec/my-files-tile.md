# Screen: my-files-tile (Files ▸ My files, tile view)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → Files ▸ My files.
2. Tap the view-options button at the right of the sort bar (px 1001,510).
3. Tap **Tile** in the "View as:" menu.
The choice is remembered — the list container's description changes to "Grid view, two finger swipe to refresh".

Screenshot: **not committed** — folder names and folder-peek thumbnails are real user content.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/my-files-tile.png`.

## Structure
Everything above the content area is **identical to `my-files-list.md`** (avatar, Photos/Files pill,
5 secondary tabs, sort chip, view-options button, floating search pill, FAB). Only the content area changes.

### Grid
| Property | px | dp |
|---|---|---|
| Columns | 3 | — |
| Content left inset | 47 | 17.9 |
| Content right edge | 1032 (48 px margin) | 18.3 |
| Column pitch (tile 1 x → tile 2 x) | 360 | **137.1** |
| Tile width | 265 | **101.0** |
| Horizontal gutter | 95 | 36.2 |
| Row pitch (thumbnail top → next thumbnail top: 685 → 1197) | 512 | **195.0** |
| First thumbnail top | 685 | 261.0 |

### Tile anatomy (from `accountpicker`, bounds `[47,685][312,837]`)
| Part | Bounds (px) | dp |
|---|---|---|
| Thumbnail `skydrive_item_thumbnail` | [47,685][312,837] | **101 × 57.9**, aligned to the tile's left edge |
| Item-count badge `skydrive_item_size`, desc "2 items" | [60,794][81,845] | inside the thumbnail, bottom-left, ~8 dp in |
| Shared badge (only if shared) | — | inside the thumbnail, **bottom-right**, ~16 dp |
| Name `onedrive_item_name` | [48,885][312,942] | y 337.1..358.9, **centered**, single line |
| Date `onedrive_item_description` | [77,947][271,993] | y 360.8..378.3, **centered**, grey |
| Overflow `action_button` "⋯" | [148,993][211,1056] | **24 × 24** glyph, **centered** under the date, y 378.3..402.3 |

### Differences from list view that matter
1. **Folders draw as a folder shape, not a square thumbnail.** The first child's image peeks out of the top of the amber folder body; a plain amber folder is drawn when there is no image child.
2. **The badge is an item count (`2`, `5`, `10`, `14`), not a size** — the opposite of list view, where folders show a size and never a count.
3. **The subtitle is the date only** — no size, no `·` separator. `Jan 18, 2024`, `Apr 13, 2020`.
4. **Long names are middle-ellipsized**, not tail-ellipsized: `animati…hillhop`, `Email at…ments`.
5. Name, date and "⋯" are all **center-aligned**; in list view they are left-aligned and the "⋯" is trailing.
6. The tile itself has no card, border, or elevation — just the thumbnail and text on the white surface.

## Behaviours
- Tap a tile → open folder / file, same as the list row.
- Tap the "⋯" under a tile → the same item bottom sheet.
- Long-press a tile → multi-select.
- Sort chip and sort order carry over unchanged from list view.
- The view choice persists across tab switches and app restarts.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Grid | `LazyVerticalGrid(GridCells.Fixed(3))`, `contentPadding = 18.dp`, `horizontalArrangement`/`verticalArrangement` `spacedBy` to hit the 137 × 195 dp pitch |
| Tile | plain `Column(horizontalAlignment = CenterHorizontally)` — **no `Card`** |
| Thumbnail | `AsyncImage` in a 101 × 58 dp `Box`, `ContentScale.Crop`, `RoundedCornerShape(4.dp)`; folders get a folder-shaped `Icon` with the child image clipped behind the flap (v1: acceptable to draw a plain folder icon and skip the peek) |
| Count badge | `Text(labelSmall)` in the `Box`, `Alignment.BottomStart`, 8 dp padding |
| Name | `Text(bodyMedium, maxLines = 1, overflow = TextOverflow.MiddleEllipsis, textAlign = Center)` |
| Date | `Text(bodySmall, color = onSurfaceVariant, textAlign = Center)` |
| Overflow | `IconButton(Icons.Filled.MoreHoriz)` sized 24 dp, centered |

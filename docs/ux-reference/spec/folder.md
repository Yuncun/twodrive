# Screen: folder (a folder's contents, pushed from My files)

Observed 2026-08-29 on **Eric's Pixel 8a** (`44271JEKB17967`), 1080×2400 px @ 420 dpi
(density 2.625 → dp = px ÷ 2.625). Screen is 411 × 914.3 dp.

> Earlier specs in this folder were captured on `emulator-5554` (1080×2424). That emulator no longer
> exists, so this and later specs are measured on the phone. The only difference is 24 px of height;
> all x values and all component sizes match.

## How to reach it
1. `adb shell am start -n com.microsoft.skydrive/.MainActivity`
2. Tap **Files** in the Photos/Files pill → the **My files** pivot is already selected.
3. Tap any folder row.

Screenshot: **not committed** — folder contents are real user files.
Raw captures at `/Users/ericshen/Claude/twodrive-ref/observed/folder.png`, `folder-scrolled.png`
(+ matching `.xml`). The repo already carries `docs/ux-reference/15-folder-contents.png`.

## Structure, top → bottom (expanded, list scrolled to the top)

| # | Component | Bounds (px) | Position & size (dp) |
|---|---|---|---|
| 1 | Status bar | [0,0][1080,121] | 46.1 dp tall |
| 2 | `toolbar` | [0,121][1080,268] | y 46.1, **56 dp tall** |
| 2a | · back `ImageButton`, desc **"Navigate Up"**, glyph ← | [0,121][147,268] | x **0**, **56 × 56** touch target, 24 dp glyph centred |
| 2b | · *(no title, no trailing action in the toolbar)* | — | — |
| 3 | `default_content` — the large-title band | [0,268][1080,504] | y 102.1..192, **89.9 dp tall** |
| 3a | · `header_title` = the folder name | [421,310][659,395] | **horizontally centred**, y 118.1..150.5, glyph box **32.4 dp** ⇒ ≈ 24–26 sp **bold** `#242424` |
| 4 | `view_switcher_header` (sort + view bar) | [0,507][1080,633] | y 193.1, **48 dp tall** |
| 4a | · `sort_spinner` chip "↓ Name ⌄" | [0,507][217,633] | label `item_label` at x **16**, chevron 16 dp at x 62.9 |
| 4b | · `view_switcher_button` (sliders glyph) | [938,507][1064,633] | x 357.3, **48 × 48** |
| 5 | List `skydrive_browse_swipelayout`, desc "List view, two finger swipe to refresh" | [0,633][1080,2400] | y **241.1** → bottom |
| 6 | Floating search pill `search_transition_container` | [88,2064][803,2211] | x 33.5, y 786.3, **272.4 × 56**, radius 28 |
| 7 | FAB `create_fab_button`, desc "Add items" | [845,2064][992,2211] | x 321.9, **56 × 56** |

The folder screen has **no** account avatar, **no** Photos/Files pill and **no** pivot tab row — the
whole `application_header` of My files is replaced by the back arrow plus the centred large title.
There is **no breadcrumb**: nesting is shown only by the title and the back arrow.

### Row heights differ by item kind
Measured over full (unclipped) rows:

| Row kind | Pitch (px) | dp | Padding above/below the 40 dp icon |
|---|---|---|---|
| Folder row | 169 | **64.4** | 32 px = 12.2 dp |
| File row | 179 | **68.2** | 37 px = 14.1 dp |

Everything else is identical to `my-files-list.md`: 40 dp leading icon at x 16.4, name and subtitle
both at x 68.6, trailing 48 dp "⋯" at x 353.1, no dividers. A file row adds a 16 dp
`skydrive_item_type_overlay` badge at the **bottom-left of its thumbnail** (x 18.3, i.e. overlapping
the icon), and image files show a real thumbnail with ≈4 dp rounded corners instead of a glyph.

## Behaviours
- **Scrolling the list collapses the whole header away.** After one swipe the `collapsible_header`
  (toolbar **and** the big title) is gone and the sort/view bar is pinned directly under the status
  bar at [0,121][1080,247] — 48 dp tall, the list starting at y 247 px = 94.1 dp. The **back arrow
  disappears with it**; the only way back while scrolled is the system back gesture.
- Scrolling back to the top re-expands the header; there is no intermediate "small title in the
  toolbar" state — the title never docks into the app bar, it simply scrolls off.
- Tap a subfolder → pushes another folder screen with the same layout.
- Tap a file row → opens the preview (see `file-preview-image.md` / `file-preview-doc.md`).
- Tap "⋯" → item bottom sheet (`item-bottom-sheet.md`).
- Sort chip and view button behave exactly as on My files (`sort-menu.md`, `view-menu.md`); the sort
  choice is global, not per folder — a folder opens with the same key already applied.
- BACK (system or the arrow) pops one level.
- An empty folder was not encountered on this account; `files-home.md` describes the pale-blue empty
  card that OneDrive reuses for empty sections.

## Material 3 mapping for TwoDrive
| OneDrive part | M3 |
|---|---|
| Toolbar + large centred title + scroll-away | `LargeTopAppBar` with `title = { Text(folderName) }`, `navigationIcon = { IconButton(Icons.AutoMirrored.Filled.ArrowBack) }` and `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()` — but note M3's large app bar **keeps** a collapsed 64 dp bar with the title and the back arrow, which is better than OneDrive's behaviour of losing the back affordance entirely. Prefer the M3 default; centre the expanded title with `CenterAlignedTopAppBar` semantics only if we want to copy OneDrive exactly. |
| Sort/view bar | the same pinned `Row` used on My files, placed **below** the app bar and outside the `LazyColumn` |
| List | the same `ListItem` row composable as `my-files-list.md`; use **one** 68 dp row height for both kinds rather than copying OneDrive's 64/68 split |
| Thumbnail | `AsyncImage` (Coil) with `RoundedCornerShape(4.dp)` falling back to the type glyph |
| Navigation | type-safe nav route `Folder(itemId, name)`; the title comes from the route argument so the screen renders before the children load |

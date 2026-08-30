# Screen: search-results

Observed 2026-08-29 on **Eric's Pixel 8a** (`44271JEKB17967`), 1080×2400 px @ 420 dpi (density 2.625).

## How to reach it
1. Files ▸ My files → tap the search pill → type a query (observed: `hello`, then `zzzqqxy`).
2. Press the keyboard's magnifier / ENTER.

Screenshot: **the results list is not committed** — it shows real file names. The **no-results**
state is committed as `docs/ux-reference/24-search-no-results.png` (nothing but the query I typed).
Raw captures at `/Users/ericshen/Claude/twodrive-ref/observed/search-results.png`,
`search-empty.png`, `search-filter.png` (+ matching `.xml`).

## Structure, top → bottom

| # | Component | Bounds (px) | Position & size (dp) |
|---|---|---|---|
| 1 | Status bar | [0,0][1080,121] | 46.1 dp |
| 2 | `collapsing_toolbar_search` | [0,121][1080,352] | y 46.1..134.1, **88 dp tall** |
| 2a | · `search_bar_background` — **now an elevated white pill** | [63,163][1017,310] | x **24**, y 62.1, **363.4 × 56**, radius **28**, soft drop shadow on a white page |
| 2b | · `action_back_button` ← | [63,174][189,300] | x 24, **48 × 48** |
| 2c | · `search_view_edit_text` — the submitted query | [189,163][875,310] | text at x **72**, ≈ 18 sp `#242424` |
| 2d | · `action_button_search` ✕ desc "Clear Text" | [875,174][1001,300] | x **333.3**, **48 × 48** |
| 3 | `header_view` | [0,352][1080,478] | y 134.1, **48 dp tall** |
| 3a | · `all_file_search_text` **"Results from all files"** | [42,390][377,441] | x **16**, ≈ 14 sp grey `#242424`, vertically centred |
| 3b | · `filter_button`, desc "View options" (sliders glyph) | [912,352][1038,478] | x **347.4**, **48 × 48** |
| 4 | `skydrive_browse_gridview` — the results | [0,478][1080,2400] | y **182.1** → bottom |

**The search bar changes shape when results appear**: before submitting it is flat and full-width
(x 8 dp, 395.4 dp wide, no shadow); after submitting it insets to x 24 dp / 363.4 dp and becomes a
rounded, shadowed pill. Nothing else about it moves.

### Result rows
Identical anatomy to `my-files-list.md` — 40 dp leading icon at x 16.4, name at x 68.6, "size · date"
subtitle under it, 48 dp trailing "⋯" at x 353.1, no dividers — with the same kind-dependent pitch as
`folder.md`: **folder rows 169 px (64.4 dp), file rows 179 px (68.2 dp)**.

Observed in the result set:
- Folders and files are **interleaved in one flat list**, not grouped and not sectioned by type,
  location or date. There is no "in folder X" breadcrumb on a row, so two files with the same name
  are indistinguishable.
- A folder result showed `0 KB · Dec 3, 2018` where the same folder shows `1.4 GB · Dec 3, 2018` in
  My files — **the search API returns no size for folders**; the row falls back to `0 KB` rather than
  hiding the size.
- Shared items keep the inline 16 dp two-person badge before the subtitle.
- The floating search pill and the "+" FAB are **absent** on this screen.

### The "View options" button
Opens the same anchored dropdown as the list screens, but with only the view group — **no sort chip
and no sort menu exist in search**.

| Property | px | dp |
|---|---|---|
| Card | [479,509][996,877] | x **182.5**, y 193.9, **197 × 140.2** |
| Header `title` "View as:" (non-clickable) | [479,530][996,623] | y 201.9..237.3 |
| Row pitch | 127 | **48.4** |
| Rows | `view_switcher_list` **"List"** (selected), `view_switcher_grid` **"Grid"** | label x 605 → **230.5** | leading icon 24 dp at x 198.5, trailing widget frame 24 dp at x 339.4 |

**Copy inconsistency worth not copying:** the same two modes are labelled **"List / Tile"** in the My
files view menu (`view-menu.md`) and **"List / Grid"** here.

## Empty state — "Couldn't find anything"
Query `zzzqqxy` (committed screenshot `24-search-no-results.png`):

| # | Component | Text | Bounds (px) | dp |
|---|---|---|---|---|
| 1 | `empty_state_view` | — | [0,1000][1080,1753] | y 381, 286.9 dp tall, **not vertically centred** — it sits ≈ 40 % down the page |
| 2 | `empty_state_image` | blue/violet magnifier over a grey card with three coloured dots | [288,1000][792,1504] | x 109.7, **192 × 192** |
| 3 | `title` | **"Couldn't find anything"** | [282,1567][798,1638] | y 596.9..624, ≈ 22 sp bold `#2B2B2B`, centred |
| 4 | `subtitle` | "It can take a few minutes for new or edited items to appear in search results." | [63,1659][1017,1753] | x 24, ≈ 16 sp grey, centred, 2 lines |

The header row ("Results from all files" + view options) is **hidden** in the empty state; only the
search pill and the illustration block remain.

## Behaviours
- Results appear ≈ 1–2 s after submit; **no loading spinner or skeleton was observed** in between —
  the body simply stays blank white and then fills.
- Editing the query and re-submitting replaces the results in place; the bar keeps its pill shape.
- **Clear Text (✕)** empties the field and returns to the blank pre-search body (`search.md`), and the
  bar reverts to its flat full-width shape.
- Back / system BACK returns to the originating tab.
- Tapping a result opens it exactly as in a list (file → preview, folder → the folder screen); "⋯"
  opens the item sheet.
- No paging control was seen; the list scrolled continuously.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Search bar | the same expanded `SearchBar` as `search.md` — do **not** reshape it between states; M3's `SearchBar` already draws one pill and TwoDrive should keep it constant |
| "Results from all files" + view button | a pinned 48 dp `Row` above the `LazyColumn`: `Text(style = labelLarge)` at 16 dp and an `IconButton(Icons.Filled.Tune)` at the end |
| Result rows | reuse the `ListItem` row composable from `my-files-list.md` unchanged; add an optional `supportingContent` second line for the parent folder path, which OneDrive lacks |
| View menu | reuse the `DropdownMenu` from `view-menu.md` with the **same labels** ("List" / "Tile") — one `ViewMode` enum for the whole app |
| Empty state | `Column(horizontalAlignment = CenterHorizontally)` with a 192 dp illustration, `headlineSmall` title and `bodyMedium` supporting text; use `Icons.Filled.SearchOff` until an illustration exists |
| Loading | TwoDrive should show a `LinearProgressIndicator` or a shimmer list — the blank gap OneDrive leaves reads as "no results" |

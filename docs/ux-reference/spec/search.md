# Screen: search (the empty search screen, before a query is submitted)

Observed 2026-08-29 on **Eric's Pixel 8a** (`44271JEKB17967`), 1080×2400 px @ 420 dpi (density 2.625).

## How to reach it
1. Files ▸ any tab (Home, My files, Shared, or inside a folder).
2. Tap the floating search pill "🔍 Search your files" at [88,2064][803,2211] px.

Screenshot: **committed** — `docs/ux-reference/23-search.png` (no user content: the screen is empty
apart from the search bar and the system keyboard).

## Structure, top → bottom

| # | Component | Bounds (px) | Position & size (dp) |
|---|---|---|---|
| 1 | Status bar | [0,0][1080,121] | 46.1 dp |
| 2 | `search_bar` container | [21,121][1059,352] | y 46.1..134.1 |
| 2a | · `search_bar_background` | [21,163][1059,310] | x **8.0**, y **62.1**, **395.4 × 56** — **flat white, no fill, no outline, no shadow** |
| 2b | · `action_back_button`, desc "Back", glyph ← | [21,174][147,300] | x 8.0, **48 × 48** |
| 2c | · `search_view_edit_text`, hint **"Folders, files"** | [147,163][1059,310] | text starts x **56**, ≈ 18 sp, hint grey `#5F5F5F`, caret blue |
| 3 | Body | [0,352][1080,…] | **completely blank white** |
| 4 | System IME | bottom ≈ 1200 px | opens automatically and takes focus |

The transition is a **full-screen replacement**, not an overlay: the pivot tabs, the sort bar, the
list, the search pill and the FAB are all gone. There is no scrim.

### What is *not* there
- **No recent searches, no suggestion list, no "Try searching for…" hint, no filter chips.** The body
  stays empty until a query is submitted. (The backlog's M4.1 wording "search screen with recent
  queries" describes something OneDrive does not do on Android.)
- No voice-search or scan button in the bar.
- No typeahead: typing does not change the body at all.

### When the field has text
| Change | Bounds (px) | dp |
|---|---|---|
| `action_button_search` appears — desc **"Clear Text"**, glyph ✕ | [917,174][1043,300] | x **349.3**, **48 × 48** |
| The `EditText` shrinks to make room | [147,163][917,310] | width 293.3 dp |

## Behaviours
- The keyboard's IME action is a **magnifier**; pressing it (or ENTER) submits the query and switches
  to `search-results.md`.
- **Clear Text (✕)** empties the field and returns to the blank body; it does **not** leave the screen.
- **Back arrow** and the system BACK both return to the tab the search was opened from, with its
  scroll position preserved.
- Search is **account-wide**: opening it from inside a folder still returns "Results from all files",
  with no scope chip and no way to search within the current folder.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Whole screen | `SearchBar` in its **expanded** state (`expanded = true`), which already gives the full-screen white surface, the leading back icon, the trailing clear icon and the empty content slot |
| Collapsed entry point | the floating pill on the list screens is the **collapsed** `SearchBar` / `SearchBarDefaults.InputField` — see `my-files-list.md` item 9 |
| Hint | `placeholder = { Text("Folders, files") }` |
| Clear | `trailingIcon` shown only when `query.isNotEmpty()`: `IconButton(Icons.Filled.Close, contentDescription = "Clear text")` |
| Submit | `onSearch = { … }`, `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)` |
| Empty body | TwoDrive **should** fill it — a recent-queries list (`ListItem` + `Icons.Filled.History`) is a genuine improvement over OneDrive's blank screen, and M4.1 already asks for it |

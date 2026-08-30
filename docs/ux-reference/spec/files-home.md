# Screen: files-home (Files ▸ Home)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → the app opens on **Photos**; tap **Files** in the app-mode switcher.
2. Files opens on the **Home** pivot by default (`pivot_home`, px 16,289..199,447 — tap ≈ 107,398).

Screenshot: **not committed** — the recent-files list shows real file names and photo thumbnails.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/files-home.png` (+ `files-home.xml`).
The repo already carries `docs/ux-reference/10-files-root.png`.

## Structure

### 1. App bar (`toolbar`, [0,142][1080,289] px → y 54.1..110.1 dp, **56 dp tall**)
| Part | px | dp |
|---|---|---|
| Account avatar `ImageButton` ("Open account drawer") | `[11,142][158,289]` | **56 dp** target at x 4.2 — a round photo avatar ≈40 dp inside it |
| `app_mode_switcher` / `tab_selector` | `[158,168][566,263]` | x 60.2..215.6, y 64..100.2 — **155.4 × 36.2 dp** |
| ├ "Photos" segment | `[158,168][390,263]` | 88.4 dp wide |
| └ "Files" segment | `[390,168][566,263]` | 67.0 dp wide |

The switcher is a **two-segment pill**: a light-grey fully-rounded track; the selected segment is a
**white chip with a thin grey outline and a bold near-black label**, the unselected one is a grey label
on the track. There is no title text and no trailing action in the app bar.

### 2. Pivot tabs (`pivot_bar_tabs`, [0,289][1080,447] px → y 110.1..170.3 dp, **60.2 dp tall**)
A `HorizontalScrollView` of five equal 216 px = **82.3 dp** cells, icon over label:

| Tab | Cell (px) | Icon (px, 24 dp) | Label (px) |
|---|---|---|---|
| **Home** | 0..216 | `[76,310][139,373]` | `[58,373][157,424]` |
| **My files** | 216..432 | `[292,310][355,373]` | `[260,373][388,424]` |
| **Shared** | 432..648 | `[508,310][571,373]` | `[482,373][598,424]` |
| **Vault** | 648..864 | `[724,310][787,373]` | `[713,373][798,424]` |
| **Offline** | 864..1080 | `[940,310][1003,373]` | `[919,373][1025,424]` |

- Icons sit at y 118.1..142.1 dp, labels at y 142.1..161.5 dp (≈13 sp).
- Icons are **outline** when unselected and **filled** when selected (Home = house, My files = folder,
  Shared = two people, Vault = safe with a lock, Offline = phone with a check).
- The selected tab tints **icon and label blue** and draws a **≈2–3 dp blue indicator** under the label,
  ≈41 dp wide (label width, not cell width), sitting on the bar's bottom edge.
- All five tabs are always present; Vault and Offline are not hidden when empty.

### 3. Section "Recent files" (`home_section_header`, [0,447][1080,594] px → y 170.3..226.3 dp, 56 dp)
| Part | px | dp |
|---|---|---|
| `title` **"Recent files"** | `[42,447][870,594]` | x **16**, ≈22 sp bold near-black |
| `button1` **"See all"** | `[870,468][1080,594]` | right-aligned, blue text button, label right edge ≈395 dp |

### 4. Recent-files list (`items_list`, [0,588][1080,1662] px) — **exactly 6 rows**
Row pitch 179 px = **68.2 dp** — noticeably taller than the 64 dp My-files row, because Home rows carry
image thumbnails.

| Part | px (row 1) | dp |
|---|---|---|
| `skydrive_item_thumbnail` | `[43,625][148,730]` | **40 dp** at x 16.4 — a real image/video frame with ≈4 dp rounded corners, or a rendered first page for documents |
| `media_overlay_icon` (white play triangle) | `[75,836][117,878]` (row 2) | **16 dp**, centred on the thumbnail — videos only |
| `skydrive_item_type_overlay` | `[48,683][90,725]` | **16 dp** badge at the thumbnail's **bottom-left** — documents only (blue lined-page glyph) |
| `onedrive_item_name` | `[180,626][827,683]` | x **68.6**, ≈16 sp |
| `skydrive_item_size_modified_date` | `[180,683][848,729]` | x **68.6**, ≈14 sp grey |
| `action_button` "⋯" | `[927,609][1053,746]` | **48 × 52 dp** at x 353.1, description `"<name> more options"` |

Subtitle grammar seen here:
- file: `1 KB · Jun 18`
- **video: `0:12 · 28 MB · Jun 14`** — a leading `m:ss` duration joined by the same separator.
- The separator is `U+00A0 U+00B7 U+00A0`, as in My files.
- **The date drops the year inside the current year** ("Jun 18", "May 7") but keeps it otherwise
  ("Jan 18, 2024") — confirms the existing review item about hand-rolled date formatting.

No dividers between rows; no headers or date grouping inside the section.

### 5. Section "Offline files" — empty state
| Part | px | dp |
|---|---|---|
| `home_section_header` / `title` **"Offline files"** | `[42,1704][1038,1851]` | x 16, y 649.1..705.1, same ≈22 sp bold |
| `empty_card` | `[42,1845][1038,2011]` | x **16..395.4**, y 702.9..766.1 — **379.4 × 63.2 dp**, pale blue fill ≈#EAF4FC, corner radius ≈8 dp, no border, no icon |
| `empty_text` | `[84,1887][996,1969]` | x **32** (16 dp inset inside the card), 2 lines ≈14 sp dark grey |

Copy: `Tap the ⋯ icon next to a file, and select "Make available offline" to view it wherever you go.`
The "⋯" is an **inline glyph inside the sentence**, not a separate icon.

### 6. Floating layer (unchanged across every Files tab)
`search_transition_container` pill at `[88,2151][803,2298]` and `create_fab_button` at
`[845,2151][992,2298]`; see the M1.7 backlog item for metrics. Note the pill's **content description is
`"Search your photos"` while its icon description and label both say "Search your files"** — a OneDrive
bug TwoDrive should not copy.

## Behaviours
- Home has **only these two sections** and does not scroll: content ends at y 2053 px, well above the
  2424 px screen. There is no "Shared with me", no "Photos" and no "For you" band on this account.
- Tapping a recent row opens the file preview; tapping "⋯" opens the item sheet (see
  `item-bottom-sheet.md` — note the Home variant offers a **reduced** action set).
- "See all" opens a full recents list.
- Two-finger swipe on the section grid refreshes (`swipe_to_refresh_layout`).
- Switching pivots keeps the app bar and the floating layer fixed; only the body changes.
- No loading skeleton was observed — the section renders populated on the first frame from cache.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| App bar | `CenterAlignedTopAppBar(navigationIcon = { IconButton { avatar } }, title = { modeSwitcher() })` — the switcher lives in the **title** slot, not as an action |
| Mode switcher | `SingleChoiceSegmentedButtonRow` with 2 `SegmentedButton`s; override the colours so the selected one is `surface` with an outline, not the M3 `secondaryContainer` fill, and drop the default leading check icon |
| Pivot tabs | `ScrollableTabRow(edgePadding = 0.dp)` + `Tab(text, icon)`; `indicator = { TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(...), height = 3.dp) }`, and swap filled/outlined `ImageVector`s on selection |
| Section header | `Row(verticalAlignment = CenterVertically)` = `Text(style = titleLarge, fontWeight = Bold)` + `Spacer(weight(1f))` + `TextButton("See all")` |
| Recent row | the same list row as `my-files-list.md` but `Modifier.height(68.dp)`; thumbnail = `AsyncImage(Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))` with a `Box` overlay for the play badge / type badge |
| Empty card | `Surface(color = <pale blue>, shape = RoundedCornerShape(8.dp)) { Text(Modifier.padding(16.dp), style = bodyMedium) }` — **not** an M3 `Card` (no elevation, no border) |
| Page | `LazyColumn` of sections so a third section can be added later without a rewrite |

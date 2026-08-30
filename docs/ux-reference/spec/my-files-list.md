# Screen: my-files-list (Files ▸ My files, list view)

Observed 2026-08-29 on emulator-5554 (Pixel 9, API 36, 1080×2424 px, 420 dpi → density 2.625).
All dp below = uiautomator px ÷ 2.625. Screen is 411 × 923 dp.

## How to reach it
1. `adb shell am start -n com.microsoft.skydrive/.MainActivity`
2. The app opens on the Photos/Files pill with **Files** selected and the secondary tab **My files** selected. (My files is the landing tab, not Home.)

Screenshot: **not committed** — the list shows real folder names. Raw capture kept at
`/Users/ericshen/Claude/twodrive-ref/observed/my-files-list-top.png`.
The repo already carries an equivalent at `docs/ux-reference/11-myfiles.png`.

## Structure, top → bottom

| # | Component | Text / icon | Bounds (px) | Position & size (dp) |
|---|---|---|---|---|
| 1 | Status bar | system | 0..~120 y | 0..46 dp tall |
| 2 | Account avatar, `ImageButton`, desc "Open account drawer" | circular profile photo | [11,142][158,289] | x 4.2, y 54.1, **56×56** touch target; avatar circle ≈40 dp |
| 3 | Segmented control `tab_selector` | "Photos" \| "Files" | [158,168][566,263] | x 60.2, y 64.0, **155.4 × 36.2** |
| 3a | · unselected segment | "Photos" | [158,168][390,263] | 88.4 dp wide, grey label |
| 3b | · selected segment | "Files" | [390,168][566,263] | 67.0 dp wide, **white** fill + hairline outline, pill radius = h/2 |
| 4 | Secondary tab row, 5 tabs, icon over label | Home / My files / Shared / Vault / Offline | labels y [373,424] | labels y 142.1..161.5; row ≈ y 116..142 for icons |
| 4a | · Home | house outline | [58,157] x | 22.1..59.8 |
| 4b | · **My files (selected)** | filled folder, blue | [260,388] x | 99.0..147.8 |
| 4c | · Shared | two-person outline | [482,598] x | 183.6..227.8 |
| 4d | · Vault | safe outline | [713,798] x | 271.6..304.0 |
| 4e | · Offline | phone-with-check outline | [919,1025] x | 350.1..390.5 |
| 4f | · selection indicator | 3 dp blue bar under the selected label, label-width | ~y 356..364 | y ≈ 168..170 |
| 5 | Sort chip `sort_spinner`, desc "Name Option Selected for Sort By" | "↓ Name ⌄" | [0,447][270,573] | x 0, y 170.3, **102.9 × 48**; label starts x 16 |
| 6 | View-options `ImageButton` | sliders / "tune" glyph | [938,447][1064,573] | x 357.3, y 170.3, **48×48** |
| 7 | List `skydrive_browse_swipelayout`, desc "List view, two finger swipe to refresh" | — | [0,573][1080,2424] | y 218.3 → bottom; fills width |
| 8 | Item row (repeats) | see below | pitch 169 px | **row pitch 64.4 dp**, no divider |
| 9 | Floating search pill `search_transition_container` | 🔍 "Search your files" | [88,2151][803,2298] | x 33.5, y 819.4, **272.4 × 56**, radius 28 |
| 10 | FAB `create_fab_button`, desc "Add items" | "+" | [845,2151][992,2298] | x 321.9, y 819.4, **56×56** circle |

### Item row anatomy (list view)
| Part | Bounds (px) | dp |
|---|---|---|
| Leading icon `skydrive_item_thumbnail`, desc "Folder" | [43,662][148,767] | x 16.4, **40 × 40** |
| Name `onedrive_item_name` | [180,663][827,720] | x 68.6, width up to 246, height 21.7 |
| Subtitle `skydrive_item_size_modified_date` | [180,720][848,766] | x 68.6, height 17.5, sits directly under the name |
| Shared badge `skydrive_item_shared_overlay` (only if shared) | [180,1229][222,1271] | **16 × 16**, inline **before** the subtitle; subtitle then starts at x 88.8 |
| Trailing overflow `action_button`, desc "`<name>` more options" | [927,651][1053,778] | x 353.1, **48 × 48**, glyph "⋯" (horizontal ellipsis) |

Gap icon → name = 12.2 dp. Name and subtitle are left-aligned on the same x.

### Copy rules seen in the subtitle
- Format is **`size · date`**, middle dot with non-breaking spaces: `9.2 MB · Jul 12, 2024`.
- **Folders show a size, not an item count**: `hackathon` → `294.6 MB · Jul 25, 2018`.
- Size units observed: `0 KB`, `11 KB`, `181 KB`, `1.2 MB`, `294.6 MB`, `1.4 GB`, `90.8 GB` — one decimal for MB/GB, no decimal for KB.
- Dates in the **current year drop the year**: `126.6 MB · Feb 4`. Other years: `Jul 12, 2024` (`MMM d, yyyy`).
- Personal Vault, when not set up, shows the literal subtitle **`Tap to set up`** instead of size · date.

### Colors observed
| Role | Value |
|---|---|
| Surface / status bar / list background | `#FFFFFF` |
| Primary text (name, selected pill label) | `#242424` |
| Secondary text (subtitle, unselected labels) | ≈ `#5F5F5F` |
| Folder icon | amber, body `#FFD143`, tab/edge darker `#F2B705`-ish |
| Selected secondary tab + indicator + FAB | OneDrive blue ≈ `#1F6FD0` (FAB has a subtle gradient to a violet-blue) |
| Search pill fill | very pale blue `#EAF4FC`, soft drop shadow |

## Behaviours
- **Tap a folder row** → pushes the folder screen (see `folder.md`).
- **Tap "⋯"** → item bottom sheet (see `item-bottom-sheet.md`).
- **Long-press a row** → enters multi-select (see `multiselect.md`).
- **Tap the sort chip** → sort menu (see `sort-menu.md`); the chip's chevron flips ⌄ → ⌃ while open.
- **Tap the view-options button** → "View as:" menu (see `view-menu.md`).
- **Tap the avatar** → account drawer slides in from the left.
- **Tap the search pill** → search screen.
- **Tap the FAB** → "Add items" menu.
- **Two-finger swipe down** refreshes (per the container's content description); a one-finger pull does not.
- The sort/view bar is **pinned**: only the list under it scrolls.
- The search pill and FAB **float over** the list and never scroll away; list content passes underneath them.
- A dismissable notice card can appear as the first list item (observed: title "Changes to Privacy Settings", body text, a text button "Privacy Settings", and an ✕ `dismiss_button` at the top-right of the card). It scrolls with the list.

## Material 3 mapping for TwoDrive
| OneDrive part | M3 component |
|---|---|
| Avatar + Photos/Files pill row | `TopAppBar` (`CenterAligned` off) with a `navigationIcon` `IconButton` holding a Coil avatar; the pill itself is out of scope (TwoDrive has no Photos tab) — drop it and use the title slot |
| Secondary tab row | `SecondaryTabRow` + `Tab(icon =, text =)`; indicator = `TabRowDefaults.SecondaryIndicator` |
| Sort chip | `AssistChip` — `leadingIcon` `Icons.Filled.ArrowDownward`, label, `trailingIcon` `Icons.Filled.KeyboardArrowDown` — or a plain `TextButton`; keep it left-aligned at 16 dp |
| View-options button | `IconButton` with `Icons.Filled.Tune` |
| Sort/view bar | a `Row` **outside** the `LazyColumn` so it stays pinned |
| Item row | `ListItem` — `leadingContent` 40 dp icon, `headlineContent` name, `supportingContent` "size · date", `trailingContent` `IconButton(Icons.Filled.MoreHoriz)`; `ListItemDefaults.colors(containerColor = surface)`, no `HorizontalDivider` |
| Shared badge | 16 dp `Icon(Icons.Filled.People)` inside the supporting `Row` before the text |
| Search pill | `SearchBar` collapsed, or a `Surface(shape = CircleShape, tonalElevation)` in a `Box(Alignment.BottomStart)` |
| "+" | `FloatingActionButton` (56 dp, `containerColor = primary`) |
| Notice card | `Card` as the first `LazyColumn` item with a trailing `IconButton(Icons.Filled.Close)` |
| Refresh | `PullToRefreshBox` (TwoDrive may use one-finger pull; OneDrive's two-finger gesture is not worth copying) |

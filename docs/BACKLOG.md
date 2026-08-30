# TwoDrive backlog

Ranked. Take the topmost unchecked item. Each item must be small enough to finish to the Definition of Done (docs/DEFINITION_OF_DONE.md) in one session. Split an item rather than half-finishing it — add the split as new unchecked sub-items directly below.

Scope reminder (frozen 2026-08-29): personal Microsoft accounts only; Files tab only. OUT: Photos, Personal Vault, Office editing, offline/background sync, camera backup.

## Milestone 1 — skeleton runs in demo flavor
- [x] M1.0 (dab6c62) Theme: disable dynamic color; fixed light/dark palette with OneDrive-like primary blue (#0F6CBD), white surfaces, grey "size · date" secondary text; folder icon yellow (#FFB900-ish) and file icons by type as in docs/ux-reference/11-myfiles.png. Re-record Roborazzi screenshots.
- [x] M1.1 (ef743a1) Welcome screen (docs/ux-reference/02-welcome.png): illustration placeholder, "Sign in" button; demo flavor's fake AuthRepository signs in immediately; Roborazzi test.
- [x] R: M1.1 (e11134e) a grey tagline ("Your OneDrive files, in a third-party app built on Microsoft Graph.") sits under the headline; 02-welcome.png has the headline alone with no subheading
- [ ] R: M1.1 the headline is headlineMedium at regular weight and renders as one thin line; 02-welcome.png is bold and wraps to two centred lines
- [ ] R: M1.1 02-welcome.png has an outlined "Create new account" button under "Sign in" and a "Skip to my photos" text button; TwoDrive shows only "Sign in" — record the decision in AGENTS.md (Photos is out of scope, account creation is not)
- [ ] R: M1.1 sign-in failures show raw exception text: MainActivityViewModel.signIn() puts e.message on the screen and MsalAuthRepository builds it as hardcoded English carrying the MSAL error code ("Sign-in failed: invalid_grant") — map failures to string resources
- [ ] R: M1.1 the sign-in error Text has no live-region semantics, so TalkBack never announces it when it appears after a failed tap on Sign in
- [x] M1.2 (1f53e4a) Files ▸ My files list from `DriveItemsRepository` (demo JSON): rows = icon, name, "size · date", trailing overflow icon; Roborazzi test with the demo tree.
- [x] R: M1.2 (1f53e4a) row subtitle is "date · size"; OneDrive is "size · date" ("9.2 MB · Jul 12, 2024" in 11-myfiles.png) — FilesScreen.kt subtitle()
- [ ] R: M1.2 folder rows show "N items"; OneDrive shows the folder's size ("hackathon 294.6 MB · Jul 25, 2018")
- [ ] R: M1.2 the date is built by hand from the English month enum and always carries the year; use a localized formatter and drop the year for the current year ("Feb 4")
- [ ] R: M1.2 trailing icon is ⋮ MoreVert, not clickable and contentDescription null; OneDrive uses ⋯ and it opens the item sheet — make it an IconButton with a description
- [ ] R: M1.2 item icon is 40dp; docs/ux-reference/README.md specifies a 48dp icon/thumbnail
- [ ] R: M1.2 the screenshot test and FilesScreenTest declare SortOrder.NAME_ASCENDING but demoDriveChildren() hands back the demo assets in file order (Documents, Pictures, Music, Personal Vault, Resume 2026.docx, Household budget.xlsx); FilesViewModel sorts before it renders, so the three checked-in FilesScreenRoot PNGs show an order the app never draws — sort in the helper
- [ ] R: M1.2 every folder in core/network/src/demo/assets/items.json has "size": 0, so the folder-size row the item above asks for cannot be shown from the demo drive; give the demo folders the recursive size Graph returns
- [ ] R: M1.2 the row name is maxLines = 1 with the default Clip overflow, so a long name is cut mid-letter instead of ellipsized; the subtitle has no maxLines at all, so a long one wraps and makes that row taller than its neighbours
- [ ] M1.3 Secondary tab row Home / My files / Shared / Vault / Offline with only My files functional; others show empty states from docs/ux-reference (17/18).
- [ ] M1.4 Folder navigation: tapping a folder pushes a screen titled with the folder name, back arrow, same list; navigation is type-safe.
- [ ] M1.5 Sort chip + menu (13-sort-menu.png): Name / Modified / File size, A→Z / Z→A; persisted in DataStore; unit test for ordering.
- [ ] R: M1.5 sort control is a plain label + icon button; OneDrive has a chip "↓ Name ⌄" at the left and a view-options icon at the right (11-myfiles.png)
- [ ] R: M1.5 sort menu is a flat 5-entry DropdownMenu; 13-sort-menu.png is Name / Modified / File size, then A to Z / Z to A, with checkmarks
- [ ] R: M1.5 SortOrder has no SIZE_SMALLEST_FIRST, so File size cannot be reversed the way Name and Modified can
- [ ] R: M1.5 the sort bar is a LazyColumn item so it scrolls away; OneDrive pins it above the list with a divider
- [ ] R: M1.5 FilesViewModelTest.changingSortOrderReordersItems asserts nothing — NAME_ASCENDING and SIZE_LARGEST_FIRST give the same order for driveItemsTestData; add data that separates them
- [ ] M1.6 View options List / Tile (14-view-options.png); tile view shows thumbnails (Coil) for images; persisted.
- [ ] M1.7 Files ▸ Home: "Recent files" (top 5 by lastModified) with "See all"; search pill + FAB placeholders laid out as in 10-files-root.png.
- [ ] R: DefaultTestDevices.PHONE is 640x360dp landscape, so no screenshot in the repo captures a portrait phone; the welcome screen's centred illustration and bottom-pinned button are only verified on foldable and tablet
- [ ] R: M1.0 no dark-theme screenshots: dab6c62 shipped DarkColorScheme and deleted captureMultiTheme, and captureForDevice still takes darkMode but nothing passes true, so all 9 PNGs are light
- [ ] R: core:database is the only module with no tests; add DAO tests for getRootChildren()'s is_root subquery, upsertDriveItems and deleteDriveItems
- [ ] R: docs the backlog cites 19-account-drawer.png (M2.4) and 16-shared.png (M4.2), and docs/ux-reference/README.md cites 20-myfiles-scrolled.png; none of the three are committed — capture them or mark them private
- [ ] R: docs the Definition of Done asks for the backlog tick "with the commit hash, in the same commit", which no commit can satisfy; M1.1 and M1.2 each needed a follow-up docs commit (58e4718, d28fd07) — reword it so the tick commit may follow the code commit
- [ ] R: 49f29af committed tools/device scripts carrying the personal sign-in address and the test phone's adb serial 44271JEKB17967; e7b8dea took them out of the tree, but the repo is public and both stay in its history — decide whether that is acceptable or rewrite those commits
- [ ] O: M1.2 list row metrics from the emulator: row pitch 64dp, leading icon 40dp at x=16dp, name and subtitle both left-aligned at x=68.6dp, trailing "⋯" a 48dp IconButton at x=353dp, and no divider between rows — docs/ux-reference/spec/my-files-list.md
- [ ] O: M1.2 docs/ux-reference/README.md says the row icon/thumbnail is 48dp; the measured value is 40dp (105px ÷ 2.625). Correct the README so the "icon is 40dp" review item is not actioned the wrong way
- [ ] O: M1.2 a shared item shows a 16dp two-person badge inline at the start of the subtitle row, pushing "size · date" to x=88.8dp; model `isShared` on DriveItem and render the badge
- [ ] O: M1.2 size copy is one decimal for MB/GB and none for KB ("0 KB", "181 KB", "1.2 MB", "294.6 MB", "1.4 GB"); the subtitle separator is a middle dot with a non-breaking space on each side (U+00A0 U+00B7 U+00A0)
- [ ] O: M1.3 the secondary tab row is icon-over-label, not label-only: Home house, My files filled folder, Shared two-person, Vault safe, Offline phone-with-check; selected tab tints icon+label blue with a 3dp label-width indicator
- [ ] O: M1.5 the sort menu is an anchored DropdownMenu ~246dp wide, left-aligned to the 16dp gutter under the chip, with a hairline divider between the key group and the direction group and a 24dp leading check slot reserved on unchecked rows; opening it flips the chip chevron ⌄→⌃ — docs/ux-reference/spec/sort-menu.md
- [ ] O: M1.5 the direction labels "A to Z / Z to A" were only observed for the Name key; decide and document what TwoDrive shows for Modified and File size (e.g. "Newest first / Oldest first", "Largest / Smallest") — this is the same gap the "SortOrder has no SIZE_SMALLEST_FIRST" review item points at
- [ ] O: M1.6 the "View as:" menu has a non-clickable header row "View as:" above the two options, a leading checkmark on the selected one, and a trailing glyph per row (List = stacked lines, Tile = 2×2 grid) tinted primary blue when selected and grey when not — docs/ux-reference/spec/view-menu.md
- [ ] O: M1.6 tile grid metrics: 3 fixed columns, 18dp content inset, 101dp tile width, 137dp column pitch, 195dp row pitch, thumbnail 101×58dp; name, date and "⋯" are all centered under the thumbnail — docs/ux-reference/spec/my-files-tile.md
- [ ] O: M1.6 tile subtitle is the date alone (no size) and the folder badge is an item count ("2", "14") drawn bottom-left inside the thumbnail — the opposite of list view, where a folder shows a size and never a count
- [ ] O: M1.6 tile names use middle ellipsis ("animati…hillhop", "Email at…ments"), not tail ellipsis; use TextOverflow.MiddleEllipsis
- [ ] O: M1.7 the search pill and the "+" FAB float over the content on every Files tab, not only Home, and never scroll away: pill bottom-left at x=33.5dp, y=819dp, 272×56dp, radius 28, pale blue #EAF4FC; FAB bottom-right at x=322dp, 56dp
- [ ] O: M1.7 Personal Vault appears as an ordinary row in My files with the literal subtitle "Tap to set up" instead of "size · date"; decide whether TwoDrive lists it at all (Vault is out of scope) and what its row says
- [ ] O: M1.1 welcome layout: illustration ~200x124dp centred, headline "Protect your files and access them anywhere" ~24sp bold centred in a ~312dp column, then a bottom-anchored stack — Sign in filled 379x48dp with a 4dp radius (not the M3 pill), 8dp gap, then the secondary buttons TwoDrive drops — docs/ux-reference/spec/welcome.md
- [ ] O: M1.3 the app bar's title slot holds a two-segment "Photos | Files" pill (155x36dp; selected segment = white chip with a thin outline and a bold label, unselected = grey label on a light-grey track), not a text title; TwoDrive has no Photos mode, so decide what occupies that slot
- [ ] O: M1.3 the account avatar is a 56dp IconButton at x=4.2dp in the navigation slot and its only job is to open the drawer; the pivot bar is a separate 60.2dp ScrollableTabRow below the 56dp app bar
- [ ] O: M1.7 Files ▸ Home is exactly two sections and does not scroll on this account: "Recent files" and "Offline files"; a section header is 56dp tall with a ~22sp bold title at the 16dp gutter and an optional right-aligned blue "See all" TextButton — docs/ux-reference/spec/files-home.md
- [ ] O: M1.7 Recent files shows 6 rows, not 5 — align the M1.7 "top 5 by lastModified" wording with what OneDrive actually renders
- [ ] O: M1.7 Home rows are 68.2dp tall (taller than the 64dp My files row) because they carry a 40dp image thumbnail with 4dp rounded corners; video adds a 16dp white play badge centred on the thumbnail and a document adds a 16dp type badge at its bottom-left
- [ ] O: M1.7 a video subtitle carries a leading m:ss duration joined by the same separator: "0:12 · 28 MB · Jun 14"
- [ ] O: M1.7 the modified date drops the year inside the current year ("Jun 18", "May 7") and keeps it otherwise ("Jan 18, 2024") — the concrete rule behind the existing "use a localized formatter" review item
- [ ] O: M1.7 an empty section is a pale-blue (#EAF4FC) filled card, 379.4x63.2dp at the 16dp gutter, 8dp radius, 16dp inner padding, two lines of ~14sp text, no icon and no border — reuse this shape for empty folders and the offline empty state, and note the copy embeds "⋯" as an inline glyph in the sentence
- [ ] O: M1.7 the search pill's contentDescription is "Search your photos" while its icon description and label both say "Search your files" — a OneDrive bug; TwoDrive's description must match its label

- [ ] O: M1.4 the folder screen has no app bar title, no pivot tabs and no avatar: a 56dp toolbar holding only a back arrow ("Navigate Up") at x=0, then an 89.9dp band with the folder name centred at ~24-26sp bold, then the same pinned 48dp sort/view bar, list starting at y=241dp — docs/ux-reference/spec/folder.md
- [ ] O: M1.4 there is no breadcrumb anywhere; nesting is conveyed only by the title and the back arrow, so the route needs the folder name as an argument to render before children load
- [ ] O: M1.4 OneDrive scrolls the whole header away on the first swipe — toolbar and title both — leaving only the sort bar pinned under the status bar and no back affordance at all; use M3's exitUntilCollapsedScrollBehavior, which keeps a collapsed bar with the back arrow, and note the deliberate divergence
- [ ] O: M1.4 the sort key is global, not per folder: a folder opens already sorted by whatever My files was set to
- [ ] O: M1.2 list rows are not a uniform height — a folder row is 169px (64.4dp) and a file row 179px (68.2dp) on the same screen; pick one 68dp row for TwoDrive rather than reproducing the split

## Milestone 2 — real Graph (prod flavor)
- [ ] M2.1 MSAL sign-in in prod flavor: `MsalAuthRepository` (single account, `consumers` authority, scopes User.Read Files.ReadWrite.All), silent token refresh, sign-out; OkHttp interceptor adds the bearer token; 401 → re-auth. Unit-test the interceptor with MockWebServer.
  - Redirect URI must follow the real package name: debug builds are `codes.fixmy.twodrive.debug`, release `codes.fixmy.twodrive`. Use `${applicationId}` as the `android:host` in the manifest's BrowserTabActivity filter and generate `redirect_uri` in msal_config from `BuildConfig.APPLICATION_ID` (or a `msal_config` per build type). Both hosts are registered in the Entra app with hash `J9AaPozhP6djyGWJSSzVi2DZpoU=`. Verify by installing prodDebug on an emulator: MSAL must not throw a redirect-URI mismatch.
- [ ] M2.2 `/me/drive/root/delta` sync into Room with delta token in DataStore; `DriveItemsRepository` is offline-first (Room is the source of truth); unit tests with MockWebServer JSON fixtures copied from real Graph responses (docs/graph-fixtures/).
- [ ] R: M2.2 a failed sync crashes the app: FilesViewModel.init launches sync() with no catch and OfflineFirstDriveItemsRepository.sync() rethrows, so an uncaught IOException reaches the default handler when the app starts offline
- [ ] R: M2.2 every FilesViewModel re-runs a full delta sync in init, so opening each folder re-syncs the whole drive; sync once (NiA's SyncManager/WorkManager)
- [ ] R: M2.2 sync() returns a Boolean that is always true and signals failure by throwing instead; return false on failure like NiA's Syncable, or return Unit
- [ ] R: M2.2 deleting a folder leaves its descendants in Room: apply() deletes only the ids named in the delta page and there is no cascade — verify what Graph sends for a deleted folder and sweep orphans
- [ ] M2.3 Thumbnails via `/items/{id}/thumbnails` (Coil, memory+disk cache); paging of children (`@odata.nextLink`).
- [ ] M2.4 Account drawer (19-account-drawer.png): avatar, email, storage bar from `/me/drive` quota, Sign out. Handle 507/quota-full state visibly.
- [ ] R: M2.4 tapping the account icon signs the user out on the spot (onActionClick = onSignOut in TwoDriveApp.kt); it should open the drawer, and the top bar's onNavigationClick is an empty stub
- [ ] M2.5 Error handling: 429 Retry-After backoff, offline snackbar, empty folder state.
- [ ] O: M2.4 drawer metrics: a 305dp modal sheet with rounded right corners; account-switcher header of 96dp tiles with 56dp avatars (a 2dp blue ring on the current one) over a 1dp tab-style blue indicator; a non-clickable email row; then 48.4dp link rows with 24dp icons at x=16dp and labels at x=56dp and no dividers between them — docs/ux-reference/spec/account-drawer.md
- [ ] O: M2.4 the quota block is a fixed footer that never scrolls: 24dp icon + "Microsoft storage" + "100.3 GB used of 100 GB (100%)" + a determinate bar + a full-width 273x52dp filled "View Plan" button. Over quota the icon, the used amount (bold) and the bar all turn red-orange #D83B01 and that is the only signal the account is full — no dialog and no snackbar anywhere else
- [ ] O: M2.4 Sign out is an ordinary drawer row: same 24dp grey icon, same 56dp label, no red tint and no separator above it
- [ ] O: M2.4 the drawer reserves a banner slot between the email row and the link list (24dp icon + title + body + a small outlined button); TwoDrive should use it for offline and sync-error notices rather than inventing a new surface

## Milestone 3 — file actions
- [ ] M3.1 Open file: download to cache with progress, then `ACTION_VIEW` via FileProvider (system viewer).
- [ ] M3.2 Item bottom sheet (12-item-more-options.png): Share / Delete tiles, Rename, Move, Details rows (Make available offline hidden).
- [ ] M3.3 Create folder from "+" menu (21-add-items-menu.png).
- [ ] M3.4 Rename + Delete (PATCH / DELETE), optimistic Room update, undo snackbar for delete.
- [ ] M3.5 Move: folder picker bottom sheet, PATCH parentReference.
- [ ] M3.6 Upload from "+" menu: system file picker, `createUploadSession` chunked upload for >4 MB, WorkManager, progress notification.
- [ ] M3.7 Share link: `createLink` (view/edit toggle), copy + system share sheet.
- [ ] M3.8 Details screen (name, size, dates, path, webUrl "Open in OneDrive").
- [ ] O: M3.2 the item sheet is content-height (skipPartiallyExpanded), never a half peek: 36.6x3.8dp drag handle, a centred 72dp thumbnail, centred name (~16sp) and "size · date" (~14sp grey), an 80dp row of action tiles, then 48.4dp rows with 24dp icons at 16dp and labels at 56dp — docs/ux-reference/spec/item-bottom-sheet.md
- [ ] O: M3.2 Delete is an action tile beside Share, not a list row, and carries no destructive styling; the tiles are grey 8dp-radius surfaces 80dp tall that split the width — two tiles for a folder (Share, Delete), three for a file (Share, Delete, Download)
- [ ] O: M3.2 the action set depends on the source list as well as the item: Home ▸ Recent files drops Delete, Move and Copy and offers Download and Comment instead, while My files offers the full set — model the actions per (item, source), not per item
- [ ] O: M3.2 the sheet has no "Open" or "Open with" entry: tapping the row is the only way to open a file, so M3.1 must not rely on a sheet action

- [ ] O: M3.3 there is no captured reference for the add menu: docs/ux-reference/21-add-items-menu.png is mis-named and actually shows the item bottom sheet for the folder "Email attachments" (a duplicate of 12-item-more-options.png). Rename or replace it, and design the add menu from M3 rather than from that file
- [ ] O: M3.3/M3.6 on an over-quota account the "+" FAB never opens a menu — it replaces the screen with a full-bleed "Your OneDrive will be frozen" page (illustration, title, body naming the account, a "Learn more" link, and bottom-pinned "Upgrade" / "Go to OneDrive" buttons). TwoDrive should keep the user on the list and surface quota refusals as a snackbar or a disabled FAB instead — docs/ux-reference/spec/add-menu.md

## Milestone 4 — polish
- [ ] M4.1 Search (`/me/drive/root/search(q=)`), search pill → search screen with recent queries.
- [ ] M4.2 Shared tab (`/me/drive/sharedWithMe`) read-only list grouped by month (16-shared.png).
- [ ] M4.3 Pull-to-refresh, list animations, dynamic color off (fixed blue), dark theme parity screenshots.
- [ ] M4.4 Large-screen adaptive layout (list-detail) like NIA.
- [ ] M4.5 Release build: R8 rules for MSAL/Room/Retrofit, signed release APK artifact in CI.
- [ ] O: M4.1 search is a full-screen replacement, not an overlay: flat white bar (x=8dp, 395x56dp, no fill/outline/shadow), back arrow 48dp at x=8, hint "Folders, files", keyboard auto-opens with a Search IME action, and a "Clear Text" X appears at x=349dp once there is text — docs/ux-reference/spec/search.md
- [ ] O: M4.1 OneDrive's pre-search body is completely blank: no recent searches, no suggestions, no typeahead, no filter chips. M4.1 asks for recent queries, so that is a deliberate improvement — record it rather than treating the blank screen as the target
- [ ] O: M4.1 the bar changes shape on submit (flat full-width -> a shadowed pill inset to x=24dp, 363x56dp); keep one constant M3 SearchBar in TwoDrive instead
- [ ] O: M4.1 the results screen is a pinned 48dp header row — "Results from all files" at the 16dp gutter plus a 48dp "View options" button at x=347dp — over a flat interleaved list of folders and files, with no sort control, no grouping and no parent-folder line on a row — docs/ux-reference/spec/search-results.md
- [ ] O: M4.1 search results report folders as "0 KB" because the search response carries no folder size; decide whether TwoDrive hides the size for folder results rather than printing 0 KB
- [ ] O: M4.1 search reuses the view menu but labels the modes "List / Grid" where My files says "List / Tile"; TwoDrive must use one ViewMode enum and one pair of labels everywhere
- [ ] O: M4.1 the no-results state is an illustration block sitting ~40% down the page (192dp image, "Couldn't find anything" ~22sp bold, then "It can take a few minutes for new or edited items to appear in search results."), and the results header row is hidden while it shows — docs/ux-reference/24-search-no-results.png
- [ ] O: M4.1 no loading indicator is shown between submit and results — the body stays blank, which reads as "no results"; TwoDrive should show a progress indicator or a shimmer list
- [ ] O: M4.1 search is account-wide even when opened from inside a folder; there is no scope chip and no search-within-this-folder option

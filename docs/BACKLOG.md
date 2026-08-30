# TwoDrive backlog

Ranked. Take the topmost unchecked item. Each item must be small enough to finish to the Definition of Done (docs/DEFINITION_OF_DONE.md) in one session. Split an item rather than half-finishing it — add the split as new unchecked sub-items directly below.

Scope reminder (frozen 2026-08-29): personal Microsoft accounts only; Files tab only. OUT: Photos, Personal Vault, Office editing, offline/background sync, camera backup.

## Milestone 1 — skeleton runs in demo flavor
- [x] M1.0 (dab6c62) Theme: disable dynamic color; fixed light/dark palette with OneDrive-like primary blue (#0F6CBD), white surfaces, grey "size · date" secondary text; folder icon yellow (#FFB900-ish) and file icons by type as in docs/ux-reference/11-myfiles.png. Re-record Roborazzi screenshots.
- [x] M1.1 (ef743a1) Welcome screen (docs/ux-reference/02-welcome.png): illustration placeholder, "Sign in" button; demo flavor's fake AuthRepository signs in immediately; Roborazzi test.
- [ ] R: M1.1 a grey tagline ("Your OneDrive files, in a third-party app built on Microsoft Graph.") sits under the headline; 02-welcome.png has the headline alone with no subheading
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

## Milestone 3 — file actions
- [ ] M3.1 Open file: download to cache with progress, then `ACTION_VIEW` via FileProvider (system viewer).
- [ ] M3.2 Item bottom sheet (12-item-more-options.png): Share / Delete tiles, Rename, Move, Details rows (Make available offline hidden).
- [ ] M3.3 Create folder from "+" menu (21-add-items-menu.png).
- [ ] M3.4 Rename + Delete (PATCH / DELETE), optimistic Room update, undo snackbar for delete.
- [ ] M3.5 Move: folder picker bottom sheet, PATCH parentReference.
- [ ] M3.6 Upload from "+" menu: system file picker, `createUploadSession` chunked upload for >4 MB, WorkManager, progress notification.
- [ ] M3.7 Share link: `createLink` (view/edit toggle), copy + system share sheet.
- [ ] M3.8 Details screen (name, size, dates, path, webUrl "Open in OneDrive").

## Milestone 4 — polish
- [ ] M4.1 Search (`/me/drive/root/search(q=)`), search pill → search screen with recent queries.
- [ ] M4.2 Shared tab (`/me/drive/sharedWithMe`) read-only list grouped by month (16-shared.png).
- [ ] M4.3 Pull-to-refresh, list animations, dynamic color off (fixed blue), dark theme parity screenshots.
- [ ] M4.4 Large-screen adaptive layout (list-detail) like NIA.
- [ ] M4.5 Release build: R8 rules for MSAL/Room/Retrofit, signed release APK artifact in CI.

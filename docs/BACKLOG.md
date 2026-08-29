# TwoDrive backlog

Ranked. Take the topmost unchecked item. Each item must be small enough to finish to the Definition of Done (docs/DEFINITION_OF_DONE.md) in one session. Split an item rather than half-finishing it — add the split as new unchecked sub-items directly below.

Scope reminder (frozen 2026-08-29): personal Microsoft accounts only; Files tab only. OUT: Photos, Personal Vault, Office editing, offline/background sync, camera backup.

## Milestone 1 — skeleton runs in demo flavor
- [ ] M1.1 Welcome screen (docs/ux-reference/02-welcome.png): illustration placeholder, "Sign in" button; demo flavor's fake AuthRepository signs in immediately; Roborazzi test.
- [ ] M1.2 Files ▸ My files list from `DriveItemsRepository` (demo JSON): rows = icon, name, "size · date", trailing overflow icon; Roborazzi test with the demo tree.
- [ ] M1.3 Secondary tab row Home / My files / Shared / Vault / Offline with only My files functional; others show empty states from docs/ux-reference (17/18).
- [ ] M1.4 Folder navigation: tapping a folder pushes a screen titled with the folder name, back arrow, same list; navigation is type-safe.
- [ ] M1.5 Sort chip + menu (13-sort-menu.png): Name / Modified / File size, A→Z / Z→A; persisted in DataStore; unit test for ordering.
- [ ] M1.6 View options List / Tile (14-view-options.png); tile view shows thumbnails (Coil) for images; persisted.
- [ ] M1.7 Files ▸ Home: "Recent files" (top 5 by lastModified) with "See all"; search pill + FAB placeholders laid out as in 10-files-root.png.

## Milestone 2 — real Graph (prod flavor)
- [ ] M2.1 MSAL sign-in in prod flavor: `MsalAuthRepository` (single account, `consumers` authority, scopes User.Read Files.ReadWrite.All), silent token refresh, sign-out; OkHttp interceptor adds the bearer token; 401 → re-auth. Unit-test the interceptor with MockWebServer.
- [ ] M2.2 `/me/drive/root/delta` sync into Room with delta token in DataStore; `DriveItemsRepository` is offline-first (Room is the source of truth); unit tests with MockWebServer JSON fixtures copied from real Graph responses (docs/graph-fixtures/).
- [ ] M2.3 Thumbnails via `/items/{id}/thumbnails` (Coil, memory+disk cache); paging of children (`@odata.nextLink`).
- [ ] M2.4 Account drawer (19-account-drawer.png): avatar, email, storage bar from `/me/drive` quota, Sign out. Handle 507/quota-full state visibly.
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

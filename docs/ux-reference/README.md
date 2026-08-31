# UX reference — OneDrive Android (com.microsoft.skydrive, captured 2026-08-29 on a Pixel 9 emulator, API 36)

TwoDrive copies these screens as closely as Material 3 + Compose allows. Match layout, copy, ordering and gestures; do not copy Microsoft logos, illustrations or brand assets — use Material icons and a neutral blue.

| File | Screen | Notes for implementation |
|---|---|---|
| 02-welcome.png | Welcome (signed out) | Illustration, "Protect your files and access them anywhere", primary "Sign in", outlined "Create new account", text "Skip to my photos" (TwoDrive: omit the last two) |
| 03-signin.png … 05-number.png | Microsoft sign-in | Provided by MSAL; nothing to build |
| (not committed: private) | Photos tab (out of scope) | Top bar = round avatar left, segmented pill control "Photos | Files" (selected segment white on grey); below it a 5-tab icon row |
| 10-files-root.png | Files ▸ Home | Sections "Recent files" (See all), "Offline files"; pill search bar bottom-left, blue FAB "+" bottom-right |
| 11-myfiles.png, 20-myfiles-scrolled.png | Files ▸ My files | Secondary tab row Home / My files / Shared / Vault / Offline; sort chip "↓ Name ⌄" left, view-options icon right; rows = 40dp folder/thumbnail, name, "size · date", trailing "⋯" |
| 12-item-more-options.png | Item bottom sheet | Big icon + name + "size · date"; two tiles Share / Delete; rows Make available offline (switch), Rename, Move, Details |
| 13-sort-menu.png | Sort menu | Name / Modified / File size, then A to Z / Z to A, checkmarks |
| 14-view-options.png | View as | List / Tile |
| 15-folder-contents.png | Folder | Back arrow, centered folder name title, same sort chip, same list |
| (not committed: private) | Shared | Sort chip "↓ Modified"; list grouped by month header ("January 2026"); rows = thumbnail, name, "<who> shared · date", trailing ⋯ (v1: read-only list) |
| 17-vault.png | Vault | Empty-state illustration + CTA (out of scope; show "not available in TwoDrive") |
| 18-offline.png | Offline | Empty-state (out of scope in v1; show empty state) |
| (not committed: private) | Account drawer (opens from avatar, slides from left, ~80% width) | Avatar tabs, email, Camera backup card, Recycle bin, Settings, Help and feedback, Sign out, storage bar "x GB used of y GB (z%)" + View Plan |
| 21-add-items-menu.png | "+" menu | Upload / Take a photo / Scan / Create folder (v1: Upload, Create folder) |
| 22-file-preview.png | File tap | Opens in-app viewer; TwoDrive v1: download then open with system viewer |

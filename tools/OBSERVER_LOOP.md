You are the TwoDrive observer loop. Nobody is watching; never ask a question.

Goal: derive the UX spec for TwoDrive by observing the real OneDrive Android app, and turn gaps into backlog items. Stop when every screen listed below has a spec file and its backlog items exist.

Environment:
- Eric's physical Pixel 8a, adb serial `44271JEKB17967` (1080x2400 @ 420 dpi, density 2.625 → dp = px ÷ 2.625), with OneDrive (`com.microsoft.skydrive`) signed in to his personal account. Prefer the `mobile-mcp` MCP tools (mobile_use_device with that serial, mobile_take_screenshot, mobile_list_elements_on_screen, mobile_click_on_screen_at_coordinates, mobile_press_button, mobile_swipe_on_screen); fall back to `adb -s 44271JEKB17967 shell input …` / `uiautomator dump` only if mobile-mcp is unavailable. Launch with `adb -s 44271JEKB17967 shell am start -n com.microsoft.skydrive/.MainActivity`. Ignore any other OneDrive builds on the phone (`com.microsoft.skydrive.test`, `.ai`) — never open them. If the phone shows a lock screen or a fingerprint/biometric prompt, stop the pass and memo it; do not try to unlock.
- READ-ONLY. Never tap Share, Delete, Move, Rename, Upload, Create, Sign out, Turn on, Allow, or any confirm button. Never type into fields except the search box. If a destructive dialog appears, press BACK.
- Work ONLY in /Users/ericshen/Claude/twodrive-observe (a separate clone). Start each turn with `git fetch origin && git reset --hard origin/main`. Never touch /Users/ericshen/Claude/twodrive.
- Privacy: the account contains real files and photos. Commit screenshots ONLY for screens with no user content (welcome, sign-in, empty states, menus, sort/view popups). For lists, drawers and previews, commit the spec text only; save the raw screenshot outside the repo in /Users/ericshen/Claude/twodrive-ref/observed/ (gitignored, not in the clone).

Screens to cover (Files tab only; Photos/Vault/Office are out of scope):
welcome, files-home, my-files-list, my-files-tile, folder, sort-menu, view-menu, item-bottom-sheet, add-menu, search, search-results, shared, offline-empty, vault-empty, account-drawer, file-preview-image, file-preview-doc, details, rename-dialog, new-folder-dialog, move-picker, share-sheet, multiselect.

For each screen write `docs/ux-reference/spec/<screen>.md`:
- how to reach it (taps from launch), screenshot filename if committed
- structure top→bottom: every component, its text, icon, approximate dp size/position (derive from uiautomator bounds ÷ 2.625 for this 420 dpi device), colors if obvious
- behaviours: what each tap/long-press/swipe does, empty and loading states seen
- Material 3 mapping: which M3 component TwoDrive should use for each part
Then compare with docs/BACKLOG.md: if an item is missing or under-specified for that screen, add `- [ ] O: <milestone> <precise item>` under the right milestone (never edit existing items). Commit `docs(observe): <screen>` and `git push origin main`; on rejection fetch/reset and retry once. Cover 2–4 screens per turn, then end the turn.

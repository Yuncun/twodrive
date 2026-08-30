# Screen: add-menu (the "+" FAB)

Observed 2026-08-29 on **Eric's Pixel 8a** (`44271JEKB17967`), 1080×2400 px @ 420 dpi (density 2.625).

## How to reach it
1. Files ▸ any tab (Home, My files, or inside a folder).
2. Tap the FAB `create_fab_button`, desc **"Add items"**, at [845,2064][992,2211] px
   (x 321.9 dp, 56 × 56 dp, bottom-right, floating over the list).

## What actually happens on this account — the menu never opens

Eric's OneDrive is **over quota** (`100.3 GB used of 100 GB`, see `account-drawer.md`). Tapping the
FAB does not expand a menu; it replaces the whole screen with a full-bleed blocking page
(`locked_account_fre`). This was reproduced twice; BACK returns to the file list unchanged.

Screenshot: **not committed** — the body text embeds the owner's email address.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/add-menu.png` (+ `.xml`).

### The frozen-account page, top → bottom
| # | Component | Text | Bounds (px) | dp |
|---|---|---|---|---|
| 1 | `image` — illustration | orange "!" over a pale triangle with a violet dot | [204,498][876,1170] | x 77.7, y 189.7, **256 × 256** |
| 2 | `title` | **"Your OneDrive will be frozen"** | [118,1170][961,1297] | y 445.7..494.1, ≈ 22 sp bold, centred |
| 3 | `summary` | "You can't add new files while you're over the storage limit. Your account, *\<email\>*, will be frozen unless you delete some files or upgrade your storage." (the email is **bold** inside the sentence) | [42,1297][1038,1455] | x 16, ≈ 16 sp, centred, 3 lines |
| 4 | `link_button` | **"Learn more about frozen accounts"** — blue text link | [255,1455][824,1517] | y 554.3..577.9, centred |
| 5 | `primary_button` | **"Upgrade"** — filled blue, full width | [42,1937][1038,2074] | x 16, **379.4 × 52.2**, radius ≈ 4 |
| 6 | `tertiary_button` | **"Go to OneDrive"** — blue text button, full width | [42,2095][1038,2232] | x 16, **379.4 × 52.2**, 8 dp under the primary |

Buttons live in a bottom-pinned `buttons_container` [42,1895][1038,2274] — the same
bottom-anchored button stack as the welcome screen (`welcome.md`).

## The real add menu — not observable here
The normal OneDrive "+" opens a bottom sheet of create/upload actions. It cannot be reached from this
account, and the file the backlog cites as its reference, **`docs/ux-reference/21-add-items-menu.png`,
is mis-named** — it is a screenshot of the *item* bottom sheet for the folder "Email attachments"
(Share/Delete tiles, Make available offline, Rename, Move, Details), i.e. a duplicate of
`12-item-more-options.png`. There is therefore **no** captured reference for the add menu anywhere in
the repo.

Read-only rule: the individual entries could not be exercised even if the menu opened, because every
one of them (Create folder, Upload, Scan, Take a photo) is a create action.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| "+" | `FloatingActionButton(containerColor = primary)`, 56 dp, `Icons.Filled.Add`, `contentDescription = "Add items"` |
| The menu it opens | `ModalBottomSheet` with `ListItem` rows (24 dp leading icon at 16 dp, label at 56 dp, 48 dp rows) — the same row metrics as `item-bottom-sheet.md`, so the two sheets share one row composable |
| Quota-blocked state | do **not** copy the full-screen takeover. Keep the user on the list and surface it as a `Snackbar` with an action, or disable the FAB with a supporting message; a full-page interstitial fired from a FAB tap is a dead end that loses the user's place |
| Illustration + title + body + link + bottom button stack | if a full page is ever needed, it is the same shape as `welcome.md`: centred illustration, `headlineSmall`, `bodyMedium`, a `TextButton` link, then a bottom-anchored `Button` + `TextButton` |

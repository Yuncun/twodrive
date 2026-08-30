# Screen: sort-menu ("Sort by" dropdown)

Observed 2026-08-29, emulator-5554, 1080×2424 px @ 420 dpi (density 2.625).

## How to reach it
1. Launch → Files ▸ My files.
2. Tap the sort chip "↓ Name ⌄" at the left of the sort bar (px 135,510).

Screenshot: **not committed** — the anchored menu leaves real file names visible around it.
Raw capture at `/Users/ericshen/Claude/twodrive-ref/observed/sort-menu.png`.
The repo already carries `docs/ux-reference/13-sort-menu.png`.

## Structure
A single white rounded card **anchored below the sort chip**, left-aligned to the screen's 16 dp gutter.
No scrim and no dimming — the list stays fully visible behind it; the card sits on an elevation shadow.

| Property | px | dp |
|---|---|---|
| Card left / right | 42 / 687 | 16.0 / 261.7 (**245.7 dp wide**) |
| Card top / bottom | 607 / 1298 | 231.2 / 494.5 (**263.3 dp tall**) |
| Corner radius | ~30 | ~11 |
| Row pitch | 127 | **48.4** |
| Checkmark x | 110 | 41.9 (**24 dp glyph**) |
| Label x | 168 | **64.0** |
| Divider y (between groups) | ~1007 | ~383.6, full card width, 1 px hairline |

### Rows, in order
| Group | Label | Bounds (px) | State when opened |
|---|---|---|---|
| Sort key | **Name** | [168,662][627,719] | ✓ checked |
| Sort key | **Modified** | [168,789][627,846] | — |
| Sort key | **File size** | [168,916][627,973] | — |
| *(hairline divider)* | | | |
| Direction | **A to Z** | [168,1048][627,1105] | ✓ checked |
| Direction | **Z to A** | [168,1175][627,1232] | — |

The two groups are independent: one key **and** one direction are checked at the same time.
The checkmark is a leading grey "✓" (`Icons.Filled.Check`), not a radio button.

## Behaviours
- Opening the menu flips the chip's trailing chevron **⌄ → ⌃**.
- Tapping any row applies the choice and closes the menu; the chip label becomes the chosen key
  and the leading arrow shows the direction (**↓** for A to Z, **↑** for Z to A).
- Tapping outside the card, or BACK, closes it with no change.
- The direction labels are **key-dependent copy** in OneDrive's design language — for `Name` they read
  "A to Z / Z to A". TwoDrive must decide whether `Modified` and `File size` reuse the same two labels
  (observed only for `Name`; not exercised, because switching the key would change persisted state).
- The chip's content description is `"<key> Option Selected for Sort By"`.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Container | `DropdownMenu(expanded, onDismissRequest)` anchored to the chip inside an `ExposedDropdownMenuBox`-style `Box`; default M3 menu already gives the white `surfaceContainer`, 4 dp corner and elevation |
| Row | `DropdownMenuItem(text = { Text(label) }, leadingIcon = { if (selected) Icon(Icons.Filled.Check) else Spacer(24.dp) })` — reserve the 24 dp leading slot on unchecked rows so labels stay aligned at 64 dp |
| Group divider | `HorizontalDivider()` between the third and fourth item |
| Width | fix at ~246 dp (`Modifier.width(246.dp)`) rather than letting it wrap the labels |
| Chip | `AssistChip` whose `leadingIcon` swaps `ArrowDownward`/`ArrowUpward` and whose `trailingIcon` swaps `KeyboardArrowDown`/`KeyboardArrowUp` |

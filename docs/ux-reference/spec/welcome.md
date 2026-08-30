# Screen: welcome (signed-out first run)

Observed from the committed capture `docs/ux-reference/02-welcome.png`
(emulator-5554, 1080×2424 px @ 420 dpi, density 2.625). Not re-observed live: reaching it
again would require signing the owner's account out, which this loop must never do.

## How to reach it
1. Fresh install / signed-out launch of `com.microsoft.skydrive`.
2. The splash (`01-launch.png`) hands straight over to this screen.

Screenshot: **committed** — `docs/ux-reference/02-welcome.png` (no user content).

## Structure
A single white full-bleed screen. No app bar, no back affordance, no scrim.
Content is one vertically centred illustration + headline block, and a bottom-anchored button stack.

| Part | px | dp | Notes |
|---|---|---|---|
| Illustration | x 267..794, y 685..1012 | x 101.7..302.5, y 261..385.5 (**~201 × 125 dp**) | horizontally centred; a laptop + two floating photo cards in purple/orange/green gradients over a pale grey device |
| Headline | x 131..951, y 1212..1339 | x 50..362, y 461.7..510 | **"Protect your files and access them anywhere"** — centred, 2 lines, bold, ≈24 sp, near-black on white; wraps inside a ~312 dp column (~50 dp side gutters, wider than the 16 dp list gutter) |
| *(empty flexible space)* | y 1339..1870 | 510..712.4 | ~200 dp of deliberate whitespace — the buttons are bottom-anchored, not tucked under the headline |
| **Sign in** button | x 42..1036, y 1870..1997 | x 16..394.7, y 712.4..760.8 (**378.7 × 48.4 dp**) | filled, OneDrive blue (≈#0F6CBD), white label ≈16 sp medium, corner radius ~4 dp (nearly square, *not* the M3 pill) |
| **Create new account** button | x 42..1036, y 2019..2148 | y 769.2..818.3 (**378.7 × 49.1 dp**) | outlined: white fill, 1 px blue border, blue label, same width and radius; 8 dp gap above |
| **Skip to my photos** | centred, y ≈ 2245..2305 | y ≈ 855..878 | text button, blue label, no border, full-width touch target |
| Gesture bar | y 2380 | 906.7 | ~33 dp of bottom inset below the last button |

Order top→bottom: illustration → headline → spacer → Sign in → Create new account → Skip to my photos.

## Behaviours
- **Sign in** → the Microsoft sign-in web flow (`03-signin.png` → `04-auth.png` → number match `05-number.png`),
  then straight into Files ▸ Home. There is no interstitial "welcome back" or permissions screen.
- **Create new account** → the same web flow on the account-creation entry point. Out of scope for TwoDrive.
- **Skip to my photos** → a signed-out local-photos mode. Out of scope for TwoDrive (Photos is out of scope).
- No carousel: this is a single static page, not a paged onboarding — there are no page dots.
- No "Terms / Privacy" footnote on this screen.

## Material 3 mapping for TwoDrive
| Part | M3 |
|---|---|
| Screen | `Scaffold` with no top bar; `Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = CenterHorizontally)` |
| Layout | `Spacer(Modifier.weight(1f))` above and below the illustration+headline block so the buttons sit on the bottom inset (`Modifier.navigationBarsPadding()`) |
| Illustration | `Image(painterResource(...), Modifier.size(width = 200.dp, height = 124.dp))` — a vector placeholder until art exists |
| Headline | `Text(style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 312.dp))` |
| Sign in | `Button(shape = RoundedCornerShape(4.dp), modifier = Modifier.fillMaxWidth().height(48.dp))` — override the default pill shape |
| Create new account | `OutlinedButton`, same shape and height |
| Skip to my photos | `TextButton` |
| Button stack | `Arrangement.spacedBy(8.dp)` |

TwoDrive ships only **Sign in** (M1.1, done in ef743a1); the other two are Microsoft-account and
Photos features that the frozen scope excludes. Keep the bottom-anchored single-button layout so the
screen still reads as the same design.

# TwoDrive Project

TwoDrive is a native Android application written in Kotlin. It is a third-party OneDrive client: it signs in a personal Microsoft account and lists, opens, organizes, uploads and shares the user's files through the Microsoft Graph API. The UI copies the official OneDrive Android app; the code follows Google's Now in Android architecture.

## Architecture

This project is a modern Android application that follows the official architecture guidance from Google. It is a reactive, single-activity app that uses the following:

-   **UI:** Built entirely with Jetpack Compose, including Material 3 components and adaptive layouts for different screen sizes.
-   **State Management:** Unidirectional Data Flow (UDF) is implemented using Kotlin Coroutines and `Flow`s. `ViewModel`s act as state holders, exposing UI state as streams of data.
-   **Dependency Injection:** Hilt is used for dependency injection throughout the app, simplifying the management of dependencies and improving testability.
-   **Navigation:** Navigation is handled by Jetpack Navigation 3 for Compose (`core/navigation` wraps it in a `Navigator` with one back stack per top-level tab).
-   **Data:** The data layer is implemented using the repository pattern.
    -   **Local Data:** Room is the source of truth for drive items; DataStore (Preferences) holds user preferences and the Graph delta link.
    -   **Remote Data:** Retrofit and OkHttp call Microsoft Graph (`core/network`). A bearer token interceptor pulls tokens from `core/auth`.
    -   **Sync:** `OfflineFirstDriveItemsRepository.sync()` walks the Graph `/me/drive/root/delta` feed and applies upserts/deletes to Room.
-   **Auth:** `core/auth` exposes `AuthRepository` and `AccessTokenProvider`. The `prod` flavor implements them with MSAL for Android (personal accounts only); the `demo` flavor is a fake that is always signed in.

## Modules

The main Android app lives in the `app/` folder. Feature modules live in `feature/` (split into `api` for navigation keys and `impl` for screens) and core and shared modules in `core/`.

| Module | Purpose |
|--------|---------|
| `core:model` | Plain Kotlin domain types (`DriveItem`, `Drive`, `UserProfile`, `SortOrder`) |
| `core:auth` | Sign-in abstraction; MSAL in `prod`, fake in `demo` |
| `core:network` | Graph DTOs, Retrofit API, demo JSON data source in `src/demo/assets` |
| `core:database` | Room cache of drive items |
| `core:datastore` | Preferences (sort order, delta link) |
| `core:data` | Repositories, offline-first sync, network monitor |
| `core:designsystem` | Theme, icons, reusable components |
| `core:ui` | Compose helpers shared by features (previews, file icons, size formatting) |
| `core:testing` | Test doubles and Hilt test modules |
| `core:screenshot-testing` | Roborazzi helpers |
| `feature:files:*` | The Files tab: folder browsing and sorting |

## Commands to Build & Test

The app and Android libraries have two product flavors: `demo` (bundled JSON drive, always signed in) and `prod` (real Graph + MSAL), and two build types: `debug` and `release`.

- Build: `./gradlew assemble{Variant}`. Typically `assembleDemoDebug`.
- Fix linting/formatting: `./gradlew spotlessApply`
- Run local tests: `./gradlew {variant}Test`, typically `testDemoDebugUnitTest`
- Run single test: `./gradlew testDemoDebugUnitTest --tests "codes.fixmy.twodrive.MyTestClass"`
- Run local screenshot tests: `./gradlew verifyRoborazziDemoDebug`
- Re-record screenshots after an intended UI change: `./gradlew recordRoborazziDemoDebug` (PNGs are checked in under `src/test/screenshots`)

### Instrumented tests

- Gradle-managed devices run on-device tests: `./gradlew pixel4api30aospatdDemoDebugAndroidTest` (fastest), also `pixel6api31aospDemoDebugAndroidTest` and `pixelcapi30aospatdDemoDebugAndroidTest`.
- The demo flavor needs no account, so instrumented tests always run against the bundled drive.

### Creating tests

#### Instrumented tests

- Tests for UI features should only use `ComposeTestRule` with a `ComponentActivity`.
- Bigger tests live in the `:app` module and they can start activities like `MainActivity`.

#### Local tests

- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) for most assertions
- [cashapp/turbine](https://github.com/cashapp/turbine) for complex coroutine tests
- `kotlin.test` assertions
- MockWebServer for Retrofit tests; `JvmUnitTestDemoAssetManager` to read the demo JSON in JVM tests

## Definition of Done

A change is done when all of these pass locally:

```
./gradlew spotlessCheck assembleDemoDebug testDemoDebugUnitTest verifyRoborazziDemoDebug
```

and, for UI changes, the Gradle-managed device smoke test above. CI (`.github/workflows/Build.yaml`) runs the same set.

## Continuous integration

- The workflows are defined in `.github/workflows/*.yaml` and they contain various checks.
- Screenshot PNGs are committed; CI verifies them rather than regenerating them.

## Decisions

- Copied Now in Android's build-logic and module layout; dropped Firebase, Jacoco, Dependency Guard, baseline profiles, the custom lint module and the protobuf DataStore (replaced by Preferences DataStore) because they add setup without serving a sample OneDrive client.
- Designsystem component screenshot tests from Now in Android were not carried over; screenshot coverage starts at the feature screens.
- A debug keystore is committed in `keystore/` so the MSAL redirect URI (signature-hash based) is identical on every machine and in CI.
- MSAL is a `prodImplementation` dependency only; the `demo` flavor never links it.

## Deliberate UX divergences

- Welcome screen: `docs/ux-reference/02-welcome.png` also has an outlined "Create new account" button and a "Skip to my photos" text button. TwoDrive v1 deliberately ships only **Sign in** — account creation is not a v1 feature, and the Photos tab is out of v1 scope.
- Personal Vault: OneDrive's My files list gives the "Personal Vault" row a special "Tap to set up" subtitle. Vault is out of the frozen scope, so TwoDrive has no set-up flow: the demo drive's "Personal Vault" folder renders as an ordinary folder row ("size · date"), and the Vault pivot keeps its empty state.

## Version control and code location

- The project uses git and is hosted in https://github.com/Yuncun/twodrive.
- Commit messages follow Conventional Commits.

## Device testing

- The demo flavor never needs a Microsoft account; CI runs only demo-flavor checks.
- Prod-flavor tests against a real account need a phone you own with Microsoft Authenticator. Keep the scripts and secrets for that in an untracked `.harness/` directory (gitignored); nothing in this repo assumes a particular device, account, or agent tool.

## Personal instructions

Your own preferences go in `AGENTS.local.md` (gitignored). See [docs/PERSONAL_INSTRUCTIONS.md](docs/PERSONAL_INSTRUCTIONS.md).

@AGENTS.local.md

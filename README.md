# TwoDrive

A third-party OneDrive client for Android, built on the [Microsoft Graph](https://learn.microsoft.com/graph/onedrive-concept-overview) API and structured after Google's [Now in Android](https://github.com/android/nowinandroid) reference app.

It signs in a personal Microsoft account with [MSAL for Android](https://github.com/AzureAD/microsoft-authentication-library-for-android), keeps a local cache of the drive with Graph's `delta` feed, and aims to match the look of the official OneDrive Android app screen for screen.

## Build

Requires JDK 17+ and the Android SDK (`local.properties` with `sdk.dir`, or `ANDROID_HOME`).

| Task | Command |
|------|---------|
| Build (demo data, no account needed) | `./gradlew assembleDemoDebug` |
| Format | `./gradlew spotlessApply` |
| Unit tests | `./gradlew testDemoDebugUnitTest` |
| Screenshot tests | `./gradlew verifyRoborazziDemoDebug` (re-record with `recordRoborazziDemoDebug`) |
| Device smoke test | `./gradlew pixel4api30aospatdDemoDebugAndroidTest` |
| Real Graph backend | `./gradlew assembleProdDebug` then sign in with a personal Microsoft account |

The `demo` flavor serves a fixed drive from JSON assets and is always signed in, so everything above runs without network or credentials. The `prod` flavor talks to Graph.

## Sign-in setup

The app registration (client id `eed23e1d-ad50-46ab-936a-02ec40532b29`, personal accounts only) lives in `core/auth/src/prod/res/raw/msal_config.json`. Its Android redirect URI is derived from the checked-in debug certificate in `keystore/`, so builds from any machine can sign in without re-registering.

## Architecture

See [AGENTS.md](AGENTS.md).

## Documentation

Backlog, definition of done, review log and UX reference screenshots live in [docs/](docs).

## License

Apache License 2.0. See [LICENSE](LICENSE).

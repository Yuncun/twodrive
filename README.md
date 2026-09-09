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

## Architecture

See [AGENTS.md](AGENTS.md).

## License

Apache License 2.0. See [LICENSE](LICENSE).

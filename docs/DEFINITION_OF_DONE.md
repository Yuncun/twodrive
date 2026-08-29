# Definition of Done

A backlog item is done only when ALL of these pass locally, in this order, and the change is committed and pushed to `main` with CI green:

```
./gradlew spotlessApply                 # then re-run spotlessCheck; commit the formatting
./gradlew spotlessCheck
./gradlew assembleDemoDebug
./gradlew testDemoDebugUnitTest
./gradlew verifyRoborazziDemoDebug      # record with recordRoborazziDemoDebug when a screen intentionally changed; commit the PNGs
./gradlew pixel6api31aospDemoDebugAndroidTest   # Gradle-managed device smoke test; demo flavor = fake Graph, no sign-in
```

Plus:
- New behaviour has a unit test (repository/ViewModel) and, for a new or changed screen, a Roborazzi screenshot test.
- The demo flavor works without network or a Microsoft account. Anything that needs the real Graph API lives behind an interface with a demo implementation.
- No TODO stubs, no hardcoded IDs outside `msal_config.json`, no new top-level module without a matching `feature/<name>/{api,impl}` or `core/<name>` convention.
- `docs/BACKLOG.md`: the item is ticked `[x]` with the commit hash, in the same commit.
- If an item cannot be completed as written, do not tick it: add a sub-bullet "BLOCKED: <reason>" and move to the next item. Never ask a human; leave a note.

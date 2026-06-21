# Panashe Bible App

Kotlin Multiplatform mobile app for Panashe Bible.

The project follows an iOS-first version of the NuvioMobile shape:

- `composeApp/` contains shared Kotlin Multiplatform and Compose Multiplatform UI.
- `composeApp/src/commonMain/` contains shared routes, content, theme, and screens.
- `composeApp/src/commonMain/composeResources/files/bible/` contains the bundled Bible data exported from `panashe-bible-shared`.
- `composeApp/src/iosMain/` contains iOS-specific Compose entry points.
- `iosApp/` is the native iOS host.

## Routes

- `/` - Daily Reading
- `/bible` - Bible
- `/bible/church` - Communion
- `/about` - About
- `/privacy` - Privacy Policy

## Development

Open the repository in the Kotlin IDE, then open `iosApp` in Xcode when working on the native host.

Useful checks:

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

## Shared Data

Bible data is not authored in this repo. Update the canonical data in `panashe-bible-shared`, then export the app bundle:

```bash
cd ../panashe-bible-shared
npm run export:app
```

The app should read Scripture, search data, and Communion seed data from `composeResources/files/bible`. Keep app-specific state separate from the bundled corpus.

# Panashe Bible App

Kotlin Multiplatform mobile app for Panashe Bible.

The project follows an iOS-first version of the NuvioMobile shape:

- `composeApp/` contains shared Kotlin Multiplatform and Compose Multiplatform UI.
- `composeApp/src/commonMain/` contains shared routes, content, theme, and screens.
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


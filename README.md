# Mantiq

Mantiq is an Android-first intelligent keyboard built in Kotlin. The project is local-first: typing and snippet expansion work without an account or network connection.

## Current MVP — 0.3.0

- Native `InputMethodService` keyboard.
- Deterministic standard Arabic layout and English QWERTY layout.
- Numbers and symbols mode, English Shift/Caps Lock, and repeat backspace.
- Direct time and date actions in the keyboard toolbar.
- User-controlled key haptics and sound stored locally.
- Jetpack Compose Material 3 application with Home, Snippets, AI, Themes, and Settings destinations.
- User-created snippets encrypted with an AES-GCM key stored in Android Keystore.
- Per-snippet application allowlists: an empty selection means the snippet runs nowhere.
- Four keyboard color themes with live previews in the app.
- Guided activation screen with live enabled/selected status and a test field.
- Adaptive Mantiq application icon.
- Safe-mode launcher UI and local crash diagnostics.
- Fallback keyboard view if the full input view cannot be created.
- Exact in-memory snippet lookup.
- Dynamic time and date templates.
- Fractional offsets such as `{{time+1.5h}}` and bare `{{time+1.5}}`.
- Compound offsets such as `{{time+1h30m}}` and `{{time+01:30}}`.
- Automatic protection that disables expansion in password fields.
- GitHub Actions tests and debug APK builds.

The Android app now uses explicit visual row ordering instead of relying on vendor-specific RTL reversal. This keeps the Arabic letters in the same positions across Android and manufacturer skins.

### Template variables for user snippets

| Template | Expansion |
|---|---|
| `{{time+1.5h}}` | Current Sudan time plus 1 hour 30 minutes |
| `{{time+4h}}` | Current Sudan time plus 4 hours |
| `{{date}}` | Current date |
| `{{time}}` | Current Sudan time |

Time and date are built-in toolbar actions. All text-expansion snippets are created and managed by the user in the app.

## Build

The Android app compiles and targets API 36. It uses JDK 17, Gradle 9.5.0, Android Gradle Plugin 9.3.2, and AGP built-in Kotlin.

The supported runtime range is Android 8.0 (API 26) through Android 16 (API 36). CI builds the APK, runs unit tests, launches the app, registers and selects the IME, and verifies that its input window appears on Android emulator API 26 and API 36.

```bash
./gradlew test assembleDebug
```

## Privacy baseline

- No typing analytics.
- No network permission in the MVP.
- No cloud keys in the APK or repository.
- Snippet expansion is disabled in detected password fields.
- Clipboard and AI features are not enabled until their encrypted designs are implemented.

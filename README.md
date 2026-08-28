# Mantiq

Mantiq is an Android-first intelligent keyboard built in Kotlin. The project is local-first: typing and snippet expansion work without an account or network connection.

## Current MVP

- Native `InputMethodService` keyboard.
- Arabic and English layouts.
- Exact in-memory snippet lookup.
- Dynamic time and date templates.
- Fractional offsets such as `{{time+1.5h}}` and bare `{{time+1.5}}`.
- Compound offsets such as `{{time+1h30m}}` and `{{time+01:30}}`.
- Automatic protection that disables expansion in password fields.
- GitHub Actions tests and debug APK builds.

### Built-in development snippets

| Trigger | Expansion |
|---|---|
| `!بعد1.5` | Current Sudan time plus 1 hour 30 minutes |
| `!بعد4` | Current Sudan time plus 4 hours |
| `!تاريخ` | Current date |
| `!الوقت` | Current Sudan time |

These defaults are temporary. User-managed encrypted snippets will replace them in the next storage milestone.

## Build

The Android app uses API 36, JDK 17, Gradle 9.5.0, Android Gradle Plugin 9.3.2, and AGP built-in Kotlin. The standalone core module uses Kotlin 2.4.10.

```bash
./gradlew test assembleDebug
```

Without an Android SDK, the pure Kotlin engine can still be tested with:

```bash
./gradlew :core:test
```

## Privacy baseline

- No typing analytics.
- No network permission in the MVP.
- No cloud keys in the APK or repository.
- Snippet expansion is disabled in detected password fields.
- Clipboard and AI features are not enabled until their encrypted designs are implemented.

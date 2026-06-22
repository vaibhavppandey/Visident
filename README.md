# Visident

Session-based Android camera app — capture, organize, and search photo sessions. Built with Jetpack Compose and Material 3 Expressive.

![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat-square)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=flat-square)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg?style=flat-square)
![Material 3](https://img.shields.io/badge/Design-M3%20Expressive-teal.svg?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-brightgreen.svg?style=flat-square)

## Features

- **Session-based capture** — group photos into named sessions
- **CameraX preview** with flash, front/rear lens switch, tap-to-focus
- **Metadata** stored in Room: id, name, age, timestamp, image count
- **Gallery** with pinch-to-zoom image viewer
- **Reactive search** by name or session ID (SQL-filtered)
- **Edit / delete** sessions — swipe-to-delete, folder cleanup on remove
- **Material 3 Expressive** UI, Material You dynamic color (API 31+), dark/light
- **Scoped storage** — app-specific, no storage permission required

## Tech stack

| Layer | Tools |
|-------|-------|
| UI | Jetpack Compose `1.12.0-alpha03`, Material 3 Expressive `1.5.0-alpha21`, Coil 3, Google Fonts |
| Logic | MVVM, Hilt `2.59.2`, StateFlow, Coroutines, Repository pattern |
| Data | Room `2.8.4` (reactive Flow), app-specific scoped storage |
| Camera | CameraX `1.6.1` (Compose integration) |
| Navigation | Navigation Compose `2.9.8`, type-safe routes via kotlinx.serialization |
| Build | AGP `9.2.1`, Gradle `9.5.1`, Kotlin `2.3.21`, KSP, Java 17 |
| SDK | min 26 (Android 8.0) · target/compile 37 |

## Project structure

```
app/src/main/java/dev/vaibhavp/visident/
├── data/        # Room database, SessionDao, SessionEntity
├── di/          # Hilt modules (Database, Repository)
├── repo/        # SessionRepository — data access abstraction
├── ui/          # Compose screens, components, navigation, theme
├── util/        # CameraUtility, FileUtils (scoped storage)
└── viewmodel/   # CaptureViewModel (camera), LibraryViewModel (list/search)
```

## Flow

Start → capture session photos → end-session form → saved to Room, images moved to a per-session folder. Search → session list → details + gallery.

Images live under `Android/media/dev.vaibhavp.visident/Sessions/<session_id>/`, cleaned up automatically on uninstall.

## Build & run

```bash
git clone https://github.com/vaibhavppandey/Visident.git
cd Visident
./gradlew assembleDebug      # or run from Android Studio (Ladybug 2024.2+)
```

Requires Android Studio Ladybug (2024.2)+ and JDK 17.

## License

MIT — see [LICENSE](LICENSE).

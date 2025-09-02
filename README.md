# Visident

 CameraX + Room + MVVM sample that captures images session-wise, stores session metadata in Room (SQLite), saves images to app-specific external media, and provides search + session details UI.

## Features
- Camera capture using CameraX (Compose viewfinder).
- Session-based capture: multiple images per session.
- End-session metadata entry: SessionID, Name, Age.
- Session metadata persisted with Room (SQLite).
- Images saved to app-specific external media under a `Sessions/<SessionID>/` folder.
- Search sessions by ID or name and view metadata + images.
- MVVM architecture with Hilt for dependency injection.

## Quick status vs requirements
- Start Session → capture images: Implemented (`ui/session/CameraCaptureScreen.kt`, `viewmodel/SessionViewModel.kt`).
- End Session → enter metadata: Implemented (`ui/session/EndSessionScreen.kt`).
- Metadata → SQLite: Implemented (`data/db/AppDB.kt`, `data/db/SessionDao.kt`, `data/model/SessionEntity.kt`).
- Images → app-specific storage: Implemented (`util/FileUtils.kt` + `repo/SessionRepository.kt`).
- Search by SessionID → view metadata & images: Implemented (`ui/search/SearchSessionScreen.kt`, `ui/session/SessionDetailScreen.kt`).

## Project structure (top-level inside `app/`)
```
MainActivity.kt
VisidentApplication.kt
data/
  db/
    AppDB.kt
    SessionDao.kt
  model/
    SessionEntity.kt
di/
  DatabaseModule.kt
  RepositoryModule.kt
repo/
  SessionRepository.kt
ui/
  components/
    SessionDetailsCard.kt
  navigation/
    AppNavHost.kt
    AppNavigation.kt
  search/
    SearchSessionScreen.kt
  session/
    CameraCaptureScreen.kt
    EndSessionScreen.kt
    SessionDetailScreen.kt
    StartSessionScreen.kt
  theme/
    Color.kt, Theme.kt, Type.kt
util/
  CameraUtility.kt
  FileUtils.kt
viewmodel/
  SessionViewModel.kt
```

## Architecture & patterns
- MVVM: `SessionViewModel` exposes UI state as StateFlow and orchestrates camera binding, image capture, session finalization, and search.
- Dependency Injection: Hilt provides `AppDB`, `SessionDao`, and `SessionRepository` (see `di/`).
- Data persistence: Room for session metadata.
- Camera: CameraX via `util/CameraUtility.kt`. Compose viewfinder uses `CameraXViewfinder`.
- Storage: images written as temp files in cache and moved into the app-specific external media `Sessions/<sessionId>/` folder on finalize.
- Navigation: Compose Navigation with typed routes (`ui/navigation/*.kt`) and `AppNavHost`.

## How sessions are handled (flow)
1. Start session: user taps "Start Session" (`StartSessionScreen`) → `CameraCaptureScreen`.
2. Capture images: camera writes to a temp file (`util/FileUtils.createTempImageFile`); `SessionViewModel` tracks `pictureCount`.
3. End session: user opens `EndSessionScreen`, provides SessionID (UUID by default), Name and Age.
4. Save: `SessionViewModel.finalizeSession` inserts a `SessionEntity` into Room, moves cached images to `Sessions/<sessionId>/` via `FileUtils.moveCachedImagesToSession`, then clears cache.
5. Search & view: `SearchSessionScreen` filters sessions; choosing one opens `SessionDetailScreen` which loads metadata and images (`SessionRepository.getImagesForSession`).

## Image storage path
Images are stored in the app-specific external media directory returned by `context.externalMediaDirs.firstOrNull()` inside a `Sessions/<sessionId>/` folder. Typical resolved path:

`Android/media/<your-app-package-or-appName>/Sessions/<SessionID>/IMG_<timestamp>.jpg`

Notes:
- This approach follows scoped storage best practices (app-specific external media). No WRITE_EXTERNAL_STORAGE required.
- If you require an exact human-readable `<AppName>` folder root, modify `util/FileUtils.getAppFolder` carefully with scoped storage considerations.

## Permissions
- CAMERA (requested via Accompanist in `CameraCaptureScreen`).
- No broad external storage permission required because we use app-specific external media.

## Build & run (macOS / zsh)
Open in Android Studio (recommended) or use the terminal:

```bash
./gradlew assembleDebug
```

After build, debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Recommended small improvements
- Compute `imageCount` by scanning the session folder after moving images to guarantee DB consistency.
- Add a recovery UI for cached images if the app is killed before finalizing a session.
- Add unit tests for `FileUtils` operations and ViewModel behaviors.

## Demo checklist to record
- Start Session → capture multiple images → End Session (enter ID, Name, Age) → Save.
- Open Search → find session → open details → verify images are present.

---
Licensed under the MIT License — see `LICENSE`.

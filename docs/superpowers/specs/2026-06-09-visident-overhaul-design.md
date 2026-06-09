# Visident Overhaul — Design Spec

**Date:** 2026-06-09
**Status:** Approved (design), pending implementation plan

## Goal

Modernize the Visident Android app along four axes the owner called out:

1. Update all dependencies to the latest **stable** versions.
2. Overhaul the whole app onto the latest **Material 3 Expressive** design system.
3. Fix functional gaps found along the way.
4. De-vibecode the source: keep comments, but rewrite the jokey/LLM-style ones into
   professional, human-written comments; fix unprofessional UI strings and docs.

## Context

Single-module Jetpack Compose app (`dev.vaibhavp.visident`), 21 Kotlin source files,
no XML layouts. Session-based camera tool: create a session, capture N photos, fill a
metadata form (id / name / age), persist to Room + move cached images into a per-session
folder under app-specific external media storage. Plus search → session detail (metadata +
image grid + zoom).

Stack today: AGP 8.12.2, Kotlin 2.2.10, compileSdk 36, minSdk 26, Compose BOM 2025.08.01,
Material3, Room 2.7.2, Hilt 2.57.1, CameraX 1.5.0-rc01, Coil 2.7.0, Java 11.

### Problems found

- **Theming scattered.** Partial M3 color scheme (only primary/secondary/tertiary set, all
  other roles defaulted); dynamic color hardcoded OFF behind commented-out dead code; no
  `Shapes` defined; `CircleShape` applied to `OutlinedTextField`s and the photo counter
  (looks wrong); incomplete dark scheme (onX/background/surface all commented placeholders).
- **Dead / risky deps.** Navigation3 (`navigation3-runtime`, `navigation3-ui` alpha08),
  `lifecycle-viewmodel-navigation3` alpha04, and `material3-adaptive-navigation3` **SNAPSHOT**
  are declared and pull in a pinned `androidx.dev/snapshots/builds/13508953` maven repo — but
  the app actually navigates with stable `navigation-compose`. The Nav3 stack is unused and
  the snapshot URL is fragile. `androidx.hilt:hilt-compiler` appears unused (no androidx-hilt
  integrations). Coil is on 2.x (3.x current).
- **Vibecode smell.** Joke/LLM comments: `// hilt dagger hehe`, `// final blow`, `// @ top`,
  `// she knows`, `// the useless app provides context`, `// hilt init lmao`, `// empty for
  hilt ofc`, `// only 1`, `// only imazes`, `// TODO: migrate to hilt nav later on`. Casual
  strings: `"zoomed in imaze yeah"`, `"session img"`, `"Can't move forward w/out perms"`,
  `Timber.wtf(...)` used as a debug log. README peppered with emojis and "hehe/lmao".
- **Functional gaps.**
  - Zoom dialog in `SessionDetailScreen` has `onDismissRequest = { }` — back gesture cannot
    close it (bug).
  - No back navigation anywhere (no top-bar back arrows on non-root screens).
  - `SessionDao` returns `List`, not `Flow` — lists never auto-refresh after writes.
  - Search re-fetches **all** rows from DB and filters in memory on every keystroke.
  - No delete / edit session. No pinch-zoom/pan on full-screen images.
  - Camera has no flash, no front/back switch, no tap-to-focus; capture errors are swallowed
    (uri set null, no user feedback); no recovery path when CAMERA permission is permanently
    denied.
  - One god `SessionViewModel` shared across the entire nav graph — camera state leaks into
    the search/detail screens.

## Decisions (locked with owner)

- Dependency strategy: **latest stable + cleanup** (drop Nav3/snapshot, migrate Coil 2→3, Java 17).
- Redesign depth: **full Material 3 Expressive overhaul**, all screens.
- Functional gaps: **fix all four groups** — nav/dialog bugs, reactive data, session management
  (delete + edit + pinch-zoom), camera UX (flash/switch/focus/error toasts/settings path).
- Verification: **build + fix after each phase** (`./gradlew assembleDebug`); SDK present at
  `~/Library/Android/sdk`, no emulator/device currently attached.
- AGP stays on the **8.x** line (8.13.2), not 9.2.1 — avoids stacking a Gradle-9 migration.
- Keep `accompanist-permissions` 0.37.3 (deprecated but newest; lowest risk vs. hand-rolling).

## Target version matrix (verified against live Maven metadata, 2026-06-09)

| Artifact | Current | Target (stable) |
|---|---|---|
| AGP | 8.12.2 | 8.13.2 |
| Gradle wrapper | 8.13 | 8.13 (keep) |
| Kotlin / Compose-compiler plugin | 2.2.10 | 2.3.21 |
| KSP | 2.2.10-2.0.2 | 2.3.9 |
| Compose BOM | 2025.08.01 | 2026.05.01 (Material3 1.4.x = Expressive stable) |
| Room | 2.7.2 | 2.8.4 |
| Hilt (dagger) | 2.57.1 | 2.58 (latest AGP-8-compatible; 2.59+ needs AGP 9) |
| hilt-navigation-compose | 1.2.0 | 1.3.0 |
| CameraX (core/compose/lifecycle/camera2) | 1.5.0-rc01 | 1.6.1 |
| navigation-compose | 2.9.3 | 2.9.8 |
| lifecycle (runtime-ktx / viewmodel-compose / runtime-compose) | 2.9.3 | 2.10.0 |
| Coil | 2.7.0 (`io.coil-kt`) | 3.4.0 (`io.coil-kt.coil3`) |
| core-ktx | 1.17.0 | 1.18.0 (1.19 needs AGP 9.1/compileSdk 37) |
| activity-compose | 1.10.1 | 1.13.0 |
| kotlinx-serialization | 1.9.0 | 1.11.0 |
| ui-text-google-fonts | 1.9.0 (explicit) | via BOM (drop explicit version) |
| timber | 5.0.1 | 5.0.1 (keep) |
| accompanist-permissions | 0.37.3 | 0.37.3 (keep) |
| Java source/target | 11 | 17 |

**Remove:** `androidx.navigation3:navigation3-runtime`, `androidx.navigation3:navigation3-ui`,
`androidx.lifecycle:lifecycle-viewmodel-navigation3`,
`androidx.compose.material3.adaptive:adaptive-navigation3`, the `androidx.dev` snapshot maven
repo in `settings.gradle.kts` (both `pluginManagement` and `dependencyResolutionManagement`),
and `androidx.hilt:hilt-compiler` (verify unused at build).

## Architecture changes

### Data layer (reactive)
- `SessionDao`:
  - `getAllSessions(): Flow<List<SessionEntity>>`
  - `searchSessions(query: String): Flow<List<SessionEntity>>` via SQL
    `WHERE name LIKE '%'||:query||'%' OR sessionId LIKE '%'||:query||'%' ORDER BY createdAt DESC`
  - `getSessionById(id): SessionEntity?` (keep; or Flow)
  - `deleteSession(session)` / `deleteById(id)`
  - `updateSession(session)` (for edit)
- DB version stays **1** (no schema change; only new query/CRUD methods → no migration needed).
- `SessionRepository`: expose Flows; `deleteSession` removes the DB row **and** the image folder
  via `FileUtils`.

### ViewModels (split the god VM)
- **`CaptureViewModel`** — camera binding, capture, picture count, finalize. Shared by
  `CameraCaptureScreen` + `EndSessionScreen`.
- **`LibraryViewModel`** — observe `getAllSessions()` / `searchSessions()` as `StateFlow`
  (`stateIn`), selected session + images, delete, edit. Used by `SearchSessionScreen` +
  `SessionDetailScreen`.
- Scope each to its screens (no longer one instance hoisted across the whole graph).

### Theme
- `Color.kt`: complete light + dark `ColorScheme` role sets seeded from the Visident teal
  (Material Theme Builder style — primary/secondary/tertiary + their containers, surface family,
  outline, error). Replace the 3-color stub.
- `Theme.kt`: `MaterialExpressiveTheme(colorScheme, motionScheme, shapes, typography)`;
  re-enable dynamic color on API 31+ (`dynamicLight/DarkColorScheme`), seeded scheme as fallback;
  remove commented dead code. `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` where needed.
- New `Shape.kt`: M3 shape scale; fixes the `CircleShape`-on-textfields misuse.
- `Type.kt`: keep Inter family, tidy.

### UI redesign (M3 Expressive) + gap fixes per screen
- **StartSessionScreen:** hero landing, large flexible top app bar, expressive primary/tonal
  buttons (or button group).
- **CameraCaptureScreen:** expressive capture button + floating toolbar →
  flash toggle, front/back switch (`CameraSelector`), tap-to-focus (`MeteringPoint`), counter
  badge, end-session action; capture-error → toast/snackbar; permission rationale screen with a
  **Settings deep-link** (`ACTION_APPLICATION_DETAILS_SETTINGS`) when permanently denied.
- **EndSessionScreen:** proper-shaped `OutlinedTextField`s (no Circle), read-only session id,
  validation, loading state on save, correct back-stack on finalize.
- **SearchSessionScreen:** M3 `SearchBar`; result cards with **swipe-to-delete** + confirm
  dialog; empty state.
- **SessionDetailScreen:** header card; top-bar **delete + edit** actions (edit name/age via
  dialog/sheet); image grid → full-screen **pinch-zoom/pan pager**; fix zoom dismissal
  (`onDismissRequest` sets state null + `BackHandler`).
- **SessionDetailsCard:** clean Material card, whole-card click to navigate, overflow menu
  (view / delete).
- **Navigation:** back arrows (`navigationIcon`) on every non-root screen.

### De-vibecode
- Rewrite the joke/LLM comments listed above into professional comments that preserve intent;
  drop pure-noise comments. Keep genuinely useful comments.
- Fix casual UI strings and `contentDescription`s; replace misused `Timber.wtf`.
- Clean `README.md`: remove emoji/"hehe/lmao", update the version/tech table to the new matrix.

## Phasing (build after each)

- **Phase 0** — deps & build cleanup (`libs.versions.toml`, `app/build.gradle.kts`,
  `build.gradle.kts`, `settings.gradle.kts`, Java 17, Coil 2→3 import migration). `assembleDebug`.
- **Phase 1** — theme (`Color.kt`, `Theme.kt`, new `Shape.kt`, `Type.kt`). `assembleDebug`.
- **Phase 2** — reactive data + VM split (`SessionDao`, `SessionRepository`, `CaptureViewModel`,
  `LibraryViewModel`, DI updates, nav wiring). `assembleDebug`.
- **Phase 3** — screens redesign + gap fixes (all five screens + card + nav). `assembleDebug`.
- **Phase 4** — de-vibecode comments + strings + README. `assembleDebug` + lint.

## Risks & mitigations

- **Toolchain compat** (AGP/Gradle/KSP/Kotlin/Hilt): conservative pins (AGP 8.13.2 on existing
  Gradle 8.13, Kotlin 2.3.21 matched to KSP 2.3.9) + build-after-each-phase catches breakage early.
- **M3 Expressive experimental APIs** (`@ExperimentalMaterial3ExpressiveApi`): opt-in per file.
- **Dynamic color** re-enable changes appearance on API 31+: seeded scheme keeps brand identity
  on older devices; dynamic is the intended modern behavior.
- **Coil 3 migration**: import/coordinate changes (`coil3.compose.AsyncImage`); verified at build.
- **No emulator attached**: verification is compile-level (`assembleDebug`) + previews, not runtime.

## Out of scope

- No backend/cloud, no new persisted columns (DB stays v1), no auth, no analytics.
- AGP 9 / Gradle 9 migration (deferred — optional future bump).
- Replacing Accompanist permissions with a hand-rolled solution.

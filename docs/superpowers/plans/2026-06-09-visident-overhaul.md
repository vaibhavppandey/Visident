# Visident Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Modernize Visident — bump all dependencies to latest stable, rebuild the UI on Material 3 Expressive, make the data layer reactive, fix functional gaps (navigation/dialog bugs, delete/edit, pinch-zoom, camera controls), and de-vibecode the source.

**Architecture:** Single-module Compose app. Data: Room (reactive `Flow` DAO) → `SessionRepository` → two scoped Hilt ViewModels (`CaptureViewModel` for the camera sub-flow, `LibraryViewModel` for search/detail). UI: `MaterialExpressiveTheme` with a teal-seeded color scheme + dynamic color, expressive components per screen. Navigation: `navigation-compose` with a nested `CaptureGraph` so camera + end-session share one `CaptureViewModel`.

**Tech Stack:** Kotlin 2.3.21, AGP 8.13.2, Compose BOM 2026.05.01 (Material3 1.4.x Expressive), Room 2.8.4, Hilt 2.59.2, CameraX 1.6.1, Coil 3.4.0, Java 17.

---

## Verification approach (read first)

This plan is **build-verified**, not unit-TDD'd, by deliberate choice:
- The app is UI/integration-heavy; there is no runnable JVM unit suite and almost no pure logic to unit-test (search moves into SQL).
- Room DAO tests need an **instrumented device**, and none is attached.
- The owner explicitly chose "build + fix after each phase".

So the per-task verification gate is **`./gradlew assembleDebug` → `BUILD SUCCESSFUL`**, plus Android Studio `@Preview` rendering for screens. Each phase ends with a green build before moving on. Keep each `@Preview` compiling — previews are the cheapest UI smoke test.

**Toolchain note:** `./gradlew` must run on **JDK 17+** (AGP 8.13 requirement). If a build fails with `invalid source release: 17` or `Unsupported class file major version`, point Gradle at Android Studio's bundled JBR, e.g. run with `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug` or set `org.gradle.java.home` in `gradle.properties`.

---

## Task 0.0: Establish a baseline build

**Files:** none (diagnostic only).

- [ ] **Step 1: Try to build the current code**

Run: `./gradlew assembleDebug`

Expected: This MAY FAIL. The current `settings.gradle.kts` pins a snapshot maven repo `https://androidx.dev/snapshots/builds/13508953/...` for `material3-adaptive-navigation3` SNAPSHOT. Snapshot builds expire after ~1–2 months, so resolution of that artifact will likely fail with `Could not resolve androidx.compose.material3.adaptive:adaptive-navigation3`. **This is expected** — Phase 0 deletes that dependency and repo. The first GREEN build is at the end of Phase 0. If the build instead succeeds, great — note it and continue.

- [ ] **Step 2: Confirm the JDK works**

Run: `./gradlew --version`
Expected: prints Gradle 8.13 and a JVM version ≥ 17. If the JVM is < 17, fix `JAVA_HOME` per the toolchain note above before continuing.

---

# Phase 0 — Dependencies & build cleanup

## Task 0.1: Rewrite the version catalog

**Files:**
- Modify: `gradle/libs.versions.toml` (full replace)

- [ ] **Step 1: Replace the entire file with the new catalog**

```toml
[versions]
agp = "8.13.2"
kotlin = "2.3.21"
ksp = "2.3.9"
coreKtx = "1.19.0"
activityCompose = "1.13.0"
lifecycle = "2.10.0"
composeBom = "2026.05.01"
roomRuntime = "2.8.4"
hiltAndroid = "2.58"
hiltNavigationCompose = "1.3.0"
camerax = "1.6.1"
accompanist = "0.37.3"
navigationCompose = "2.9.8"
kotlinxSerialization = "1.11.0"
coil = "3.4.0"
timber = "5.0.1"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-ui-text-google-fonts = { module = "androidx.compose.ui:ui-text-google-fonts" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }

androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "roomRuntime" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "roomRuntime" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "roomRuntime" }

androidx-camera-core = { module = "androidx.camera:camera-core", version.ref = "camerax" }
androidx-camera-compose = { module = "androidx.camera:camera-compose", version.ref = "camerax" }
androidx-camera-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
androidx-camera-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }

accompanist-permissions = { module = "com.google.accompanist:accompanist-permissions", version.ref = "accompanist" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hiltAndroid" }
hilt-android-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hiltAndroid" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }

junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
jetbrains-kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hiltAndroid" }
```

**Compat constraint:** Hilt 2.59+ hard-requires AGP 9.0+, so on the AGP-8 line Hilt is pinned to **2.58** (its latest AGP-8-compatible release), not 2.59.x.

Changes vs. old: dropped all `navigation3-*`, `lifecycle-viewmodel-navigation3`, `material3-adaptive-navigation3`, `androidx-hilt-compiler`; swapped `coil-compose` to `io.coil-kt.coil3`; swapped `kotlinx-serialization-core` → `-json`; added `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`, `material-icons-extended`; moved `ksp` + `hilt` to the `[plugins]` table; unified serialization plugin to the Kotlin version; removed explicit `ui-text-google-fonts` version (now BOM-managed).

## Task 0.2: Rewrite the app module build script

**Files:**
- Modify: `app/build.gradle.kts` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.vaibhavp.visident"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.vaibhavp.visident"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // Opt in once here instead of annotating every call site with @OptIn.
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.compose)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.accompanist.permissions)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.coil.compose)
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
```

Key changes: KSP + Hilt applied via catalog aliases; `kotlinOptions` block removed in favor of the top-level `kotlin { compilerOptions { } }`; Java 11 → 17; global expressive/permissions opt-ins; Coil3, icons-extended, lifecycle-compose added; Nav3 removed.

## Task 0.3: Rewrite root build script and settings

**Files:**
- Modify: `build.gradle.kts` (root)
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Replace `build.gradle.kts` (root)**

```kotlin
// Top-level build file. Plugins are declared here (apply false) and applied per-module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 2: Replace `settings.gradle.kts` (drop the snapshot repos)**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Visident"
include(":app")
```

The `androidx.dev/snapshots/builds/13508953` repo is gone from both blocks — it only existed for the now-deleted Nav3 snapshot dependency.

## Task 0.4: Migrate the one existing Coil call site so the module still compiles

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/session/SessionDetailScreen.kt:47`

`SessionDetailScreen` is fully rewritten in Phase 3, but Phase 0 must compile. The only Coil 2 import in the codebase is here.

- [ ] **Step 1: Change the Coil import**

Replace:
```kotlin
import coil.compose.AsyncImage
```
with:
```kotlin
import coil3.compose.AsyncImage
```
(`AsyncImage(model = imageFile, ...)` with a `java.io.File` model is valid in Coil 3 unchanged — no other edit needed here in Phase 0.)

## Task 0.5: Build Phase 0

- [ ] **Step 1: Build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`. This is the first green build. If Room/Hilt KSP errors appear, confirm `ksp` + `hilt` plugins resolved (Task 0.1 `[plugins]`). If `material-icons-extended` fails to resolve, confirm the BOM line is present.

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts build.gradle.kts settings.gradle.kts app/src/main/java/dev/vaibhavp/visident/ui/session/SessionDetailScreen.kt
git commit -m "build: bump deps to latest stable, drop Nav3/snapshot, migrate Coil 2->3, Java 17"
```

---

# Phase 1 — Material 3 Expressive theme

## Task 1.1: Replace the color palette with full light + dark schemes

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/theme/Color.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Material 3 color scheme seeded from the Visident teal brand. Every role is defined so the
// theme is self-contained and doesn't fall back to defaults. Used as the brand scheme when
// dynamic color is unavailable (API < 31) or disabled.

val VisidentLightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9EF2E4),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF051F1B),
    tertiary = Color(0xFF456179),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCE5FF),
    onTertiaryContainer = Color(0xFF001E31),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF4FBF8),
    onBackground = Color(0xFF161D1B),
    surface = Color(0xFFF4FBF8),
    onSurface = Color(0xFF161D1B),
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceTint = Color(0xFF006A60),
    surfaceBright = Color(0xFFF4FBF8),
    surfaceDim = Color(0xFFD5DBD8),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F2),
    surfaceContainer = Color(0xFFE9EFEC),
    surfaceContainerHigh = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFDEE4E1),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBEC9C5),
    inverseSurface = Color(0xFF2B3230),
    inverseOnSurface = Color(0xFFECF2EF),
    inversePrimary = Color(0xFF82D5C8),
    scrim = Color(0xFF000000),
)

val VisidentDarkColors = darkColorScheme(
    primary = Color(0xFF82D5C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF9EF2E4),
    secondary = Color(0xFFB1CCC6),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF324B47),
    onSecondaryContainer = Color(0xFFCCE8E2),
    tertiary = Color(0xFFACC9E5),
    onTertiary = Color(0xFF143349),
    tertiaryContainer = Color(0xFF2D4961),
    onTertiaryContainer = Color(0xFFCCE5FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFDEE4E1),
    surface = Color(0xFF0E1513),
    onSurface = Color(0xFFDEE4E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    surfaceTint = Color(0xFF82D5C8),
    surfaceBright = Color(0xFF343B39),
    surfaceDim = Color(0xFF0E1513),
    surfaceContainerLowest = Color(0xFF090F0E),
    surfaceContainerLow = Color(0xFF161D1B),
    surfaceContainer = Color(0xFF1A211F),
    surfaceContainerHigh = Color(0xFF252B29),
    surfaceContainerHighest = Color(0xFF2F3633),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    inverseSurface = Color(0xFFDEE4E1),
    inverseOnSurface = Color(0xFF2B3230),
    inversePrimary = Color(0xFF006A60),
    scrim = Color(0xFF000000),
)
```

## Task 1.2: Add the shape scale

**Files:**
- Create: `app/src/main/java/dev/vaibhavp/visident/ui/theme/Shape.kt`

- [ ] **Step 1: Create the file**

```kotlin
package dev.vaibhavp.visident.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 shape scale. Components read these via MaterialTheme.shapes; call sites should
// reference the scale (e.g. MaterialTheme.shapes.large) rather than hard-coding shapes — this
// is what replaces the previous CircleShape-on-text-field usage.
val VisidentShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
```

## Task 1.3: Rebuild the theme entry point on MaterialExpressiveTheme

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/theme/Theme.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun VisidentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color is available on Android 12 (API 31)+. On older devices, or when
    // disabled, the app falls back to the Visident brand scheme.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> VisidentDarkColors
        else -> VisidentLightColors
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = VisidentShapes,
        typography = Typography,
        content = content,
    )
}
```

(`MaterialExpressiveTheme` and `MotionScheme.expressive()` need `ExperimentalMaterial3ExpressiveApi`, satisfied by the global opt-in from Task 0.2. `Typography` is the existing Inter typography from `Type.kt`, unchanged.)

## Task 1.4: Build Phase 1

- [ ] **Step 1: Build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`. Existing screens still call `VisidentTheme { }` — the signature is unchanged, so they keep compiling.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/dev/vaibhavp/visident/ui/theme/
git commit -m "feat(theme): Material 3 Expressive theme with seeded teal scheme, dynamic color, shapes"
```

---

# Phase 2 — Reactive data layer + ViewModel split

In this phase the new data methods and ViewModels are added **alongside** the existing `SessionViewModel` so the project keeps compiling. The old VM and the legacy DAO/repo methods are deleted in Phase 3 once screens migrate.

## Task 2.1: Make the DAO reactive and add CRUD

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/data/db/SessionDao.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.vaibhavp.visident.data.model.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getSessionById(id: String): SessionEntity?

    /** Emits the full session list and re-emits whenever the table changes. */
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun observeAllSessions(): Flow<List<SessionEntity>>

    /** Case-insensitive search by name or session id, filtered in SQL rather than in memory. */
    @Query(
        "SELECT * FROM sessions " +
            "WHERE name LIKE '%' || :query || '%' OR sessionId LIKE '%' || :query || '%' " +
            "ORDER BY createdAt DESC",
    )
    fun searchSessions(query: String): Flow<List<SessionEntity>>

    // Retained only for the legacy SessionViewModel; removed in Phase 3 once screens use Flows.
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<SessionEntity>
}
```

(DB version stays `1` — `@Update`/`@Delete`/new `@Query` methods don't change the schema, so no migration.)

## Task 2.2: Add folder deletion to FileUtils (and clean its comments)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/util/FileUtils.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.util

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Helpers for the app's session image storage, rooted at the app-specific external media
 * directory (no storage permission required; removed on uninstall).
 */
object FileUtils {

    private fun getAppFolder(context: Context): File {
        val appSpecificMediaDir = context.externalMediaDirs.firstOrNull()
            ?: throw IOException("External media storage is not available.")
        val sessionsDir = File(appSpecificMediaDir, "Sessions")
        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs()
        }
        return sessionsDir
    }

    fun createSessionFolder(context: Context, sessionId: String): File {
        val sessionFolder = File(getAppFolder(context), sessionId)
        if (!sessionFolder.exists()) {
            sessionFolder.mkdirs()
        }
        return sessionFolder
    }

    fun getSessionImages(context: Context, sessionId: String): List<File> {
        val folder = createSessionFolder(context, sessionId)
        // Return image files only, sorted by capture time (filename carries the timestamp).
        return folder.listFiles { file -> file.isImage() }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    /** Removes a session's image folder and all its contents. Used when deleting a session. */
    fun deleteSessionFolder(context: Context, sessionId: String) {
        val folder = File(getAppFolder(context), sessionId)
        if (folder.exists() && !folder.deleteRecursively()) {
            Timber.w("Failed to fully delete session folder: %s", sessionId)
        }
    }

    private fun getCacheFolder(context: Context): File {
        val cacheFolder = File(context.cacheDir, "temp")
        if (!cacheFolder.exists()) cacheFolder.mkdirs()
        return cacheFolder
    }

    fun createTempImageFile(context: Context): File {
        val cacheFolder = getCacheFolder(context)
        return File(cacheFolder, "IMG_${System.currentTimeMillis()}.jpg")
    }

    fun getCachedImages(context: Context): List<File> {
        return getCacheFolder(context).listFiles { file -> file.isImage() }?.toList() ?: emptyList()
    }

    fun clearCache(context: Context) {
        getCacheFolder(context).listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
    }

    fun moveCachedImagesToSession(context: Context, sessionId: String) {
        val cachedImages = getCachedImages(context)
        if (cachedImages.isEmpty()) return

        val sessionFolder = createSessionFolder(context, sessionId)
        cachedImages.forEach { file ->
            val targetFile = File(sessionFolder, file.name)
            try {
                // Prefer an atomic rename; fall back to copy+delete across filesystem boundaries.
                if (!file.renameTo(targetFile)) {
                    file.copyTo(targetFile, overwrite = true)
                    file.delete()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to move cached image %s", file.name)
            }
        }
        clearCache(context)
    }

    private fun File.isImage(): Boolean =
        isFile && (name.endsWith(".jpg", true) ||
            name.endsWith(".jpeg", true) ||
            name.endsWith(".png", true))
}
```

## Task 2.3: Expose reactive reads + delete/update from the repository

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/repo/SessionRepository.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.repo

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.util.FileUtils
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Single point of access to session metadata (Room) and session images (file storage). */
@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
    @param:ApplicationContext private val context: Context,
) {

    suspend fun saveSession(session: SessionEntity) {
        dao.insertSession(session)
        FileUtils.createSessionFolder(context, session.sessionId)
    }

    suspend fun updateSession(session: SessionEntity) = dao.updateSession(session)

    /** Deletes the session row and its image folder together. */
    suspend fun deleteSession(session: SessionEntity) {
        dao.deleteSession(session)
        FileUtils.deleteSessionFolder(context, session.sessionId)
    }

    suspend fun getSession(id: String): SessionEntity? = dao.getSessionById(id)

    fun observeAllSessions(): Flow<List<SessionEntity>> = dao.observeAllSessions()

    fun searchSessions(query: String): Flow<List<SessionEntity>> = dao.searchSessions(query)

    fun getImagesForSession(sessionId: String): List<File> =
        FileUtils.getSessionImages(context, sessionId)

    fun moveCachedImagesToSession(sessionId: String) =
        FileUtils.moveCachedImagesToSession(context, sessionId)

    fun createTempImageFile(): File = FileUtils.createTempImageFile(context)

    fun clearCache() = FileUtils.clearCache(context)

    // Retained for the legacy SessionViewModel; removed in Phase 3.
    suspend fun getAllSessions(): List<SessionEntity> = dao.getAllSessions()
}
```

(Note: `@field:ApplicationContext` changed to `@param:ApplicationContext` — the correct target for a constructor parameter that becomes a property; functionally equivalent here but matches Hilt's documented usage.)

## Task 2.4: Add the CaptureViewModel (camera flow)

**Files:**
- Create: `app/src/main/java/dev/vaibhavp/visident/viewmodel/CaptureViewModel.kt`

- [ ] **Step 1: Create the file**

```kotlin
package dev.vaibhavp.visident.viewmodel

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.repo.SessionRepository
import dev.vaibhavp.visident.util.CameraUtility
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/** Owns the camera session: preview binding, capture, flash/lens/focus controls, and finalize. */
@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val repository: SessionRepository,
) : ViewModel() {

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _pictureCount = MutableStateFlow(0)
    val pictureCount: StateFlow<Int> = _pictureCount.asStateFlow()

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled.asStateFlow()

    private val _hasFlashUnit = MutableStateFlow(false)
    val hasFlashUnit: StateFlow<Boolean> = _hasFlashUnit.asStateFlow()

    // Changing this re-keys the binding effect in the screen, which rebinds with the new lens.
    private val _lensFacing = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val lensFacing: StateFlow<CameraSelector> = _lensFacing.asStateFlow()

    private val _captureError = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val captureError: SharedFlow<String> = _captureError.asSharedFlow()

    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var meteringPointFactory: SurfaceOrientedMeteringPointFactory? = null

    private val imageCapture: ImageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
            // A metering-point factory in the preview buffer's coordinate space, for tap-to-focus.
            meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }

    /**
     * Binds preview + capture to [lifecycleOwner] using the current lens, then suspends until the
     * caller's scope is cancelled (e.g. leaving the screen or switching lens), unbinding on exit.
     * Call from a [lensFacing]-keyed LaunchedEffect so switching the lens rebinds.
     */
    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val provider = try {
            ProcessCameraProvider.awaitInstance(appContext)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to obtain camera provider")
            _surfaceRequest.update { null }
            return
        }
        try {
            provider.unbindAll()
            val camera: Camera = provider.bindToLifecycle(
                lifecycleOwner,
                _lensFacing.value,
                previewUseCase,
                imageCapture,
            )
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
            _hasFlashUnit.value = camera.cameraInfo.hasFlashUnit()
            applyFlashMode()
            awaitCancellation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Camera use case binding failed")
            _surfaceRequest.update { null }
        } finally {
            provider.unbindAll()
            cameraControl = null
            cameraInfo = null
        }
    }

    fun toggleLens() {
        _lensFacing.value =
            if (_lensFacing.value == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
    }

    fun toggleFlash() {
        _flashEnabled.update { !it }
        applyFlashMode()
    }

    private fun applyFlashMode() {
        imageCapture.flashMode =
            if (_flashEnabled.value) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
    }

    /** [bufferOffset] must already be transformed into preview-buffer coordinates by the viewfinder. */
    fun focusOnPoint(bufferOffset: Offset) {
        val factory = meteringPointFactory ?: return
        val control = cameraControl ?: return
        val point = factory.createPoint(bufferOffset.x, bufferOffset.y)
        control.startFocusAndMetering(FocusMeteringAction.Builder(point).build())
    }

    fun takePicture(context: Context) {
        viewModelScope.launch {
            val outputFile: File = repository.createTempImageFile()
            CameraUtility.takePicture(
                context = context,
                imageCaptureUseCase = imageCapture,
                outputFile = outputFile,
                onImageSaved = { uri -> if (uri != null) _pictureCount.update { it + 1 } },
                onError = { exc -> _captureError.tryEmit(exc.message ?: "Couldn't capture photo") },
            )
        }
    }

    fun finalizeSession(
        sessionId: String,
        name: String,
        age: Int,
        imageCount: Int,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.saveSession(
                SessionEntity(sessionId = sessionId, name = name, age = age, imageCount = imageCount),
            )
            repository.moveCachedImagesToSession(sessionId)
            repository.clearCache()
            _pictureCount.update { 0 }
            onComplete()
        }
    }
}
```

## Task 2.5: Add the LibraryViewModel (search + detail + delete/edit)

**Files:**
- Create: `app/src/main/java/dev/vaibhavp/visident/viewmodel/LibraryViewModel.kt`

- [ ] **Step 1: Create the file**

```kotlin
package dev.vaibhavp.visident.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.repo.SessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/** Backs the search list and session-detail screens with reactive reads and delete/edit. */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SessionRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // The list re-queries automatically as the query changes, and re-emits on any table change.
    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<SessionEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.observeAllSessions()
            else repository.searchSessions(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedSession = MutableStateFlow<SessionEntity?>(null)
    val selectedSession: StateFlow<SessionEntity?> = _selectedSession.asStateFlow()

    private val _selectedSessionImages = MutableStateFlow<List<File>>(emptyList())
    val selectedSessionImages: StateFlow<List<File>> = _selectedSessionImages.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _selectedSession.value = repository.getSession(sessionId)
            _selectedSessionImages.value = repository.getImagesForSession(sessionId)
        }
    }

    fun deleteSession(session: SessionEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteSession(session)
            onDeleted()
        }
    }

    fun updateSession(session: SessionEntity, onUpdated: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateSession(session)
            _selectedSession.value = session
            onUpdated()
        }
    }
}
```

## Task 2.6: Build Phase 2

- [ ] **Step 1: Build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`. The old `SessionViewModel` and screens are untouched and still compile; the new VMs compile but are not wired in yet.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/dev/vaibhavp/visident/data/db/SessionDao.kt \
        app/src/main/java/dev/vaibhavp/visident/util/FileUtils.kt \
        app/src/main/java/dev/vaibhavp/visident/repo/SessionRepository.kt \
        app/src/main/java/dev/vaibhavp/visident/viewmodel/CaptureViewModel.kt \
        app/src/main/java/dev/vaibhavp/visident/viewmodel/LibraryViewModel.kt
git commit -m "feat(data): reactive Flow DAO, delete/update, split into Capture + Library ViewModels"
```

---

# Phase 3 — Screen redesign (M3 Expressive) + gap fixes

Wires the new ViewModels in, redesigns every screen, fixes the gaps, then removes the legacy code. Each task keeps the build compiling on its own where possible; the final removal task (3.9) is what deletes `SessionViewModel`, so the build is only guaranteed green again after **3.9**. Run the Phase 3 build at 3.10.

## Task 3.1: Add a reusable ZoomableImage component

**Files:**
- Create: `app/src/main/java/dev/vaibhavp/visident/ui/components/ZoomableImage.kt`

- [ ] **Step 1: Create the file**

```kotlin
package dev.vaibhavp.visident.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale

/**
 * Pinch-to-zoom and pan image. Scale is clamped to [minScale, maxScale]; pan resets when the
 * image returns to its original size.
 */
@Composable
fun ZoomableImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 5f,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                    offset = if (scale == 1f) Offset.Zero else offset + pan
                }
            },
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        )
    }
}
```

## Task 3.2: Add navigation routes for the capture sub-graph

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/navigation/AppNavigation.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object StartSessionRoute

/** Parent graph for the capture flow so Camera + EndSession share one CaptureViewModel. */
@Serializable
object CaptureGraph

@Serializable
object CameraCaptureRoute

@Serializable
object EndSessionRoute

@Serializable
object SearchSessionsRoute

@Serializable
data class SessionDetailsRoute(val sessionID: String)
```

## Task 3.3: Rewrite the NavHost (nested graph + back wiring + scoped VMs)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/navigation/AppNavHost.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import dev.vaibhavp.visident.ui.search.SearchSessionScreen
import dev.vaibhavp.visident.ui.session.CameraCaptureScreen
import dev.vaibhavp.visident.ui.session.EndSessionScreen
import dev.vaibhavp.visident.ui.session.SessionDetailScreen
import dev.vaibhavp.visident.ui.session.StartSessionScreen
import dev.vaibhavp.visident.viewmodel.CaptureViewModel

@Composable
fun VisidentNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = StartSessionRoute) {
        composable<StartSessionRoute> {
            StartSessionScreen(
                onStartNewSessionClick = { navController.navigate(CaptureGraph) },
                onSearchSessionClick = { navController.navigate(SearchSessionsRoute) },
            )
        }

        navigation<CaptureGraph>(startDestination = CameraCaptureRoute) {
            composable<CameraCaptureRoute> { entry ->
                CameraCaptureScreen(
                    viewModel = entry.captureGraphViewModel(navController),
                    onBack = { navController.popBackStack() },
                    onEndSessionClick = { navController.navigate(EndSessionRoute) },
                )
            }
            composable<EndSessionRoute> { entry ->
                EndSessionScreen(
                    viewModel = entry.captureGraphViewModel(navController),
                    onBack = { navController.popBackStack() },
                    onNavigateToStart = {
                        navController.navigate(StartSessionRoute) {
                            // Clear the capture flow off the back stack after saving.
                            popUpTo(StartSessionRoute) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable<SearchSessionsRoute> {
            SearchSessionScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateToSessionDetails = { id -> navController.navigate(SessionDetailsRoute(id)) },
            )
        }

        composable<SessionDetailsRoute> { entry ->
            SessionDetailScreen(
                sessionID = entry.toRoute<SessionDetailsRoute>().sessionID,
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}

/** One CaptureViewModel scoped to the whole capture sub-graph, shared by its screens. */
@Composable
private fun NavBackStackEntry.captureGraphViewModel(
    navController: NavHostController,
): CaptureViewModel {
    val parentEntry = remember(this) { navController.getBackStackEntry(CaptureGraph) }
    return hiltViewModel(parentEntry)
}
```

## Task 3.4: Redesign StartSessionScreen

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/session/StartSessionScreen.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@Composable
fun StartSessionScreen(
    onStartNewSessionClick: () -> Unit,
    onSearchSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Visident") },
                subtitle = { Text("Session-based capture") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            Text("Capture a session", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a new photo session or browse your saved ones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))

            val height = ButtonDefaults.LargeContainerHeight
            Button(
                onClick = onStartNewSessionClick,
                modifier = Modifier.fillMaxWidth().heightIn(height),
                shapes = ButtonDefaults.shapesFor(height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(ButtonDefaults.iconSizeFor(height)))
                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                Text("Start session", style = ButtonDefaults.textStyleFor(height))
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onSearchSessionClick,
                modifier = Modifier.fillMaxWidth().heightIn(height),
                shapes = ButtonDefaults.shapesFor(height),
                contentPadding = ButtonDefaults.contentPaddingFor(height),
            ) {
                Icon(Icons.Filled.Search, null, Modifier.size(ButtonDefaults.iconSizeFor(height)))
                Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                Text("Search sessions", style = ButtonDefaults.textStyleFor(height))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartSessionScreenPreview() {
    VisidentTheme {
        StartSessionScreen(onStartNewSessionClick = {}, onSearchSessionClick = {})
    }
}
```

## Task 3.5: Redesign CameraCaptureScreen (controls + tap-focus + permission settings path)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/session/CameraCaptureScreen.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.session

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import dev.vaibhavp.visident.viewmodel.CaptureViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CameraCaptureScreen(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onEndSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasRequested by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA,
        onPermissionResult = { hasRequested = true },
    )
    val status = cameraPermissionState.status

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                status is PermissionStatus.Granted ->
                    CameraContent(
                        viewModel = viewModel,
                        onBack = onBack,
                        onEndSessionClick = onEndSessionClick,
                    )

                status is PermissionStatus.Denied && status.shouldShowRationale ->
                    PermissionRationale(
                        message = "Visident needs the camera to capture session photos.",
                        actionLabel = "Grant permission",
                        onAction = { cameraPermissionState.launchPermissionRequest() },
                    )

                !hasRequested ->
                    PermissionRationale(
                        message = "Camera access is required to start a session.",
                        actionLabel = "Request permission",
                        onAction = { cameraPermissionState.launchPermissionRequest() },
                    )

                else ->
                    // Permanently denied: only the system Settings page can re-grant it.
                    PermissionRationale(
                        message = "Camera permission is permanently denied. Enable it in Settings to continue.",
                        actionLabel = "Open Settings",
                        onAction = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        },
                    )
            }
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onEndSessionClick: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
    val flashEnabled by viewModel.flashEnabled.collectAsStateWithLifecycle()
    val hasFlashUnit by viewModel.hasFlashUnit.collectAsStateWithLifecycle()
    val lensFacing by viewModel.lensFacing.collectAsStateWithLifecycle()

    // Rebind whenever the lens changes; the binding suspends until this effect is cancelled.
    LaunchedEffect(lensFacing) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }
    // Surface one-off capture errors as a toast.
    LaunchedEffect(Unit) {
        viewModel.captureError.collectLatest { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val coordinateTransformer = remember { MutableCoordinateTransformer() }
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                coordinateTransformer = coordinateTransformer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            with(coordinateTransformer) { viewModel.focusOnPoint(tap.transform()) }
                        }
                    },
            )
        }

        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (hasFlashUnit) {
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        Icon(
                            if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (flashEnabled) "Flash on" else "Flash off",
                        )
                    }
                }
                IconButton(onClick = { viewModel.toggleLens() }) {
                    Icon(Icons.Filled.Cameraswitch, "Switch camera")
                }
            },
        )

        // Photo counter badge.
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, end = 16.dp)
                .size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$pictureCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        // Capture (center) + end-session (right) controls.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .padding(bottom = 36.dp),
        ) {
            LargeFloatingActionButton(
                onClick = { viewModel.takePicture(context) },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Icon(Icons.Filled.PhotoCamera, "Capture photo", Modifier.size(36.dp))
            }
            Button(
                onClick = onEndSessionClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp),
            ) {
                Text("End session")
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Rounded.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction) { Text(actionLabel) }
    }
}
```

(Note: `FilledIconButton` and `AlertDialog` imports are listed for parity with the dialog/icon usage; if the IDE flags them as unused after paste, remove the unused import — the compiler treats unused imports as warnings, not errors, so the build stays green either way.)

## Task 3.6: Redesign EndSessionScreen (proper shapes, back, loading)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/session/EndSessionScreen.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.session

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.vaibhavp.visident.viewmodel.CaptureViewModel
import java.util.UUID

@Composable
fun EndSessionScreen(
    viewModel: CaptureViewModel,
    onBack: () -> Unit,
    onNavigateToStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A session id is generated once and shown read-only; the user fills in name + age.
    val sessionId = remember { UUID.randomUUID().toString() }
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val pictureCount by viewModel.pictureCount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val ageValid = ageString.toIntOrNull()?.let { it > 0 } == true
    val isFormValid = name.isNotBlank() && ageValid

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("End session · $pictureCount photos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = sessionId,
                onValueChange = {},
                readOnly = true,
                label = { Text("Session ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                isError = name.isBlank() && ageString.isNotEmpty(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ageString,
                onValueChange = { input -> ageString = input.filter { it.isDigit() } },
                label = { Text("Age") },
                singleLine = true,
                isError = ageString.isNotEmpty() && !ageValid,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            if (isLoading) {
                LoadingIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.finalizeSession(
                            sessionId = sessionId,
                            name = name.trim(),
                            age = ageString.toInt(),
                            imageCount = pictureCount,
                            onComplete = {
                                isLoading = false
                                Toast.makeText(context, "Session saved", Toast.LENGTH_SHORT).show()
                                onNavigateToStart()
                            },
                        )
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save session")
                }
            }
        }
    }
}
```

## Task 3.7: Redesign SearchSessionScreen (SearchBar + swipe-to-delete)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/search/SearchSessionScreen.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.ui.components.SessionDetailsCard
import dev.vaibhavp.visident.viewmodel.LibraryViewModel

@Composable
fun SearchSessionScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    onNavigateToSessionDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<SessionEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Sessions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search by name or ID") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
                content = {},
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (sessions.isEmpty()) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            "No sessions yet. Start by creating one."
                        } else {
                            "No sessions match your search."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                    ) {
                        items(sessions, key = { it.sessionId }) { session ->
                            SwipeToDeleteContainer(onDelete = { pendingDelete = session }) {
                                SessionDetailsCard(
                                    session = session,
                                    onClick = { onNavigateToSessionDetails(session.sessionId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Filled.Delete, null) },
            title = { Text("Delete session?") },
            text = { Text("\"${session.name}\" and its ${session.imageCount} photos will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(session)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            // Always snap back; the actual delete is confirmed via the dialog.
            false
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        content()
    }
}
```

## Task 3.8: Redesign SessionDetailsCard (clickable card)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/components/SessionDetailsCard.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.ui.theme.VisidentTheme
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailsCard(
    session: SessionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(session.name, style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "Age ${session.age} · ID ${session.sessionId.take(8)}…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${session.imageCount} photos", style = MaterialTheme.typography.bodySmall)
                Text(session.createdAt.toDateString(), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun Long.toDateString(): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(this))

@Preview
@Composable
private fun SessionDetailsCardPreview() {
    VisidentTheme {
        SessionDetailsCard(
            session = SessionEntity(
                sessionId = "12345678-abcd",
                name = "John Doe",
                age = 30,
                createdAt = 1_733_000_000_000L,
                imageCount = 5,
            ),
            onClick = {},
        )
    }
}
```

(Preview uses a fixed `createdAt` constant rather than `System.currentTimeMillis()` so the render is deterministic.)

## Task 3.9: Redesign SessionDetailScreen (delete/edit + fixed zoom pager) and remove the legacy ViewModel

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/ui/session/SessionDetailScreen.kt` (full replace)
- Delete: `app/src/main/java/dev/vaibhavp/visident/viewmodel/SessionViewModel.kt`
- Modify: `app/src/main/java/dev/vaibhavp/visident/data/db/SessionDao.kt` (remove legacy method)
- Modify: `app/src/main/java/dev/vaibhavp/visident/repo/SessionRepository.kt` (remove legacy method)

- [ ] **Step 1: Replace `SessionDetailScreen.kt`**

```kotlin
package dev.vaibhavp.visident.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import dev.vaibhavp.visident.data.model.SessionEntity
import dev.vaibhavp.visident.ui.components.ZoomableImage
import dev.vaibhavp.visident.viewmodel.LibraryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    sessionID: String,
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val images by viewModel.selectedSessionImages.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    // The full-screen viewer is driven by a single nullable index, cleared only on dismiss.
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(sessionID) { viewModel.loadSession(sessionID) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Session details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }, enabled = session != null) {
                        Icon(Icons.Filled.Edit, "Edit session")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, enabled = session != null) {
                        Icon(Icons.Filled.Delete, "Delete session")
                    }
                },
            )
        },
    ) { paddingValues ->
        val currentSession = session
        if (currentSession == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                SessionInfo(currentSession, dateFormat)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text(
                    "Images",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))

                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No images for this session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(images) { imageFile ->
                            val index = images.indexOf(imageFile)
                            AsyncImage(
                                model = imageFile,
                                contentDescription = "Session image ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewerIndex = index },
                            )
                        }
                    }
                }
            }
        }
    }

    // Full-screen pinch-zoom viewer. onDismissRequest clears the index, so system back, scrim
    // tap, and the close button all dismiss it (fixes the previously stuck dialog).
    viewerIndex?.let { startIndex ->
        FullScreenImageViewer(
            images = images,
            startIndex = startIndex,
            onDismiss = { viewerIndex = null },
        )
    }

    if (showEdit && session != null) {
        EditSessionDialog(
            session = session!!,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                viewModel.updateSession(updated)
                showEdit = false
            },
        )
    }

    if (showDeleteConfirm && session != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.Delete, null) },
            title = { Text("Delete session?") },
            text = { Text("This permanently removes the session and its images.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(session!!, onDeleted = onBack)
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SessionInfo(session: SessionEntity, dateFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoRow("Session ID", session.sessionId)
        InfoRow("Name", session.name)
        InfoRow("Age", session.age.toString())
        InfoRow("Created", dateFormat.format(Date(session.createdAt)))
        InfoRow("Total images", session.imageCount.toString())
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FullScreenImageViewer(
    images: List<File>,
    startIndex: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
        ) {
            val pagerState = rememberPagerState(
                initialPage = startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
                pageCount = { images.size },
            )
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                ZoomableImage(
                    painter = rememberAsyncImagePainter(model = images[page]),
                    contentDescription = "Image ${page + 1}",
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
            ) {
                Icon(Icons.Filled.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun EditSessionDialog(
    session: SessionEntity,
    onDismiss: () -> Unit,
    onSave: (SessionEntity) -> Unit,
) {
    var name by remember { mutableStateOf(session.name) }
    var ageString by remember { mutableStateOf(session.age.toString()) }
    val ageValid = ageString.toIntOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = ageString,
                    onValueChange = { input -> ageString = input.filter { it.isDigit() } },
                    label = { Text("Age") },
                    singleLine = true,
                    isError = ageString.isNotEmpty() && !ageValid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(session.copy(name = name.trim(), age = ageString.toInt())) },
                enabled = name.isNotBlank() && ageValid,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
```

- [ ] **Step 2: Delete the legacy ViewModel**

```bash
git rm app/src/main/java/dev/vaibhavp/visident/viewmodel/SessionViewModel.kt
```

- [ ] **Step 3: Remove the legacy DAO method** in `SessionDao.kt` — delete these lines:

```kotlin
    // Retained only for the legacy SessionViewModel; removed in Phase 3 once screens use Flows.
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<SessionEntity>
```

- [ ] **Step 4: Remove the legacy repository method** in `SessionRepository.kt` — delete these lines:

```kotlin
    // Retained for the legacy SessionViewModel; removed in Phase 3.
    suspend fun getAllSessions(): List<SessionEntity> = dao.getAllSessions()
```

## Task 3.10: Rewrite MainActivity (drop experimental annotations, clean comment)

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/MainActivity.kt` (full replace)

- [ ] **Step 1: Replace the file**

```kotlin
package dev.vaibhavp.visident

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.vaibhavp.visident.ui.navigation.VisidentNavHost
import dev.vaibhavp.visident.ui.theme.VisidentTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VisidentTheme {
                VisidentNavHost(navController = rememberNavController())
            }
        }
    }
}
```

(The `@ExperimentalMaterial3Api` / `@ExperimentalPermissionsApi` activity annotations are gone — those opt-ins are now global compiler args.)

## Task 3.11: Build Phase 3

- [ ] **Step 1: Build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`. If `LargeFloatingActionButton`, `LoadingIndicator`, `LargeFlexibleTopAppBar`, or the expressive `ButtonDefaults.*ContainerHeight`/`shapesFor` symbols are unresolved, confirm the Compose BOM resolved to `2026.05.01` (Material3 1.4.x) — `./gradlew app:dependencies --configuration debugRuntimeClasspath | grep material3`.

- [ ] **Step 2: Commit**

```bash
git add -A
git commit -m "feat(ui): Material 3 Expressive redesign of all screens; back nav, delete/edit, pinch-zoom viewer, camera controls; remove legacy SessionViewModel"
```

---

# Phase 4 — De-vibecode comments, strings & docs

Earlier phases already wrote clean code for every file they touched. This phase cleans the remaining untouched files and the README.

## Task 4.1: Clean the DI modules and Application class

**Files:**
- Modify: `app/src/main/java/dev/vaibhavp/visident/VisidentApplication.kt`
- Modify: `app/src/main/java/dev/vaibhavp/visident/di/DatabaseModule.kt`
- Modify: `app/src/main/java/dev/vaibhavp/visident/di/RepositoryModule.kt`

- [ ] **Step 1: Replace `VisidentApplication.kt`**

```kotlin
package dev.vaibhavp.visident

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Annotated for Hilt so it can generate the DI component. */
@HiltAndroidApp
class VisidentApplication : Application()
```

- [ ] **Step 2: In `DatabaseModule.kt`**, replace the comment `// only 1` on the `@Singleton` above `provideAppDatabase` with nothing (delete the trailing comment), and replace `"visident_db" // Ensure this name is consistent` with just `"visident_db"`. Final file:

```kotlin
package dev.vaibhavp.visident.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vaibhavp.visident.data.db.AppDB
import dev.vaibhavp.visident.data.db.SessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDB {
        return Room.databaseBuilder(appContext, AppDB::class.java, "visident_db").build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(appDB: AppDB): SessionDao {
        return appDB.sessionDao()
    }
}
```

- [ ] **Step 3: Replace `RepositoryModule.kt`** (removes the `// she knows` / `// the useless app provides context` / `// tied to app lifecycle` joke comments):

```kotlin
package dev.vaibhavp.visident.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vaibhavp.visident.data.db.SessionDao
import dev.vaibhavp.visident.repo.SessionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao,
        @ApplicationContext context: Context,
    ): SessionRepository = SessionRepository(sessionDao, context)
}
```

(Note: `SessionRepository` already has `@Inject constructor` + `@Singleton`, so Hilt can construct it directly and this provider is technically redundant — but keep it for now to avoid touching the binding graph in a cleanup phase. Removing it is a safe optional follow-up.)

## Task 4.2: Clean the AndroidManifest comment

**Files:**
- Modify: `app/src/main/AndroidManifest.xml:28`

- [ ] **Step 1:** Remove the trailing comment `<!-- We'll replace this placeholder -->` after the `fontProviderRequests` meta-data value. Leave the meta-data element itself intact.

## Task 4.3: Rewrite the README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Make these concrete edits** (the rest of the README is accurate and stays):

1. **Tagline (line ~13):** replace `*A modern Android application demonstrating session-based image capture with metadata management using cutting-edge Android technologies.*` with `*A session-based camera app for capturing and organizing photo sessions, built with Jetpack Compose and Material 3 Expressive.*`

2. **Technologies table (lines ~141-149):** update versions to: Compose BOM `2026.05.01`, Navigation Compose `2.9.8`, Room `2.8.4`, Hilt `2.58`, CameraX `1.6.1`, Lifecycle `2.10.0`. Add rows: Kotlin `2.3.21`, AGP `8.13.2`, Coil `3.4.0`.

3. **Key Libraries block (lines ~153-177):** replace `kapt(...)` lines with `ksp(...)`; change `io.coil-kt:coil-compose:2.7.0` → `io.coil-kt.coil3:coil-compose:3.4.0`; `androidx.camera:camera-compose:1.5.0-rc01` → `:1.6.1`; `androidx.room:room-ktx:2.7.2` → `:2.8.4` and its compiler line to `ksp("androidx.room:room-compiler:2.8.4")`; `com.google.dagger:hilt-android:2.57.1` → `:2.58` (+ ksp compiler); `androidx.navigation:navigation-compose:2.9.3` → `:2.9.8`; replace `org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0` → `kotlinx-serialization-json:1.11.0`.

4. **Prerequisites (lines ~263-267):** `Java Version: 11` → `Java Version: 17`.

5. **Footer (lines ~369-374):** replace the `Made with ❤ ...` block and emoji line with a plain line: `Built with Jetpack Compose, Material 3 Expressive, Room, CameraX, and Hilt.`

6. Remove any remaining emoji decorations in headings if present; keep the shields.io badges.

## Task 4.4: Final build, lint, and commit

- [ ] **Step 1: Build**

Run: `./gradlew assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Lint**

Run: `./gradlew :app:lintDebug`
Expected: completes; review `app/build/reports/lint-results-debug.html` for new errors. Fix any **error**-severity lint (warnings are acceptable for this pass).

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "docs+chore: de-vibecode comments and strings, refresh README for new stack"
```

---

## Self-review notes (already reconciled in this plan)

- **Spec coverage:** deps→Phase 0; theme→Phase 1; reactive data + VM split→Phase 2; redesign + all four gap groups→Phase 3 (nav/back: 3.3/3.4/3.5/3.6/3.7/3.9; zoom-dialog-dismiss: 3.9 `FullScreenImageViewer`; delete/edit: 3.7/3.9; pinch-zoom: 3.1/3.9; camera flash/switch/focus/error/settings: 3.5; reactive search: 2.1/2.5/3.7); de-vibecode→clean code written inline in every touched file + Phase 4 for the rest.
- **Compile ordering:** old `SessionViewModel` + legacy DAO/repo methods are kept until Task 3.9 deletes them in the same commit that removes their last consumers, so every phase boundary builds.
- **Type consistency:** `CaptureViewModel` is used by Camera + EndSession via the `CaptureGraph`-scoped `hiltViewModel`; `LibraryViewModel` by Search + Detail. `SessionDetailsCard(session, onClick)` matches its caller in `SearchSessionScreen`. `ZoomableImage(painter, contentDescription)` matches its caller in `FullScreenImageViewer`.
- **Known soft spots to watch at build time:** expressive symbols require BOM 2026.05.01 (verify resolution); `material-icons-extended` provides `Cameraswitch`/`FlashOn`/`FlashOff`/`PhotoCamera`; a couple of imports in 3.5 may be unused after paste (warnings only).

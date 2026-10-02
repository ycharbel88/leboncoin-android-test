# Android RecruitmentTest App

**Offline-capable album browsing · Persistent favorites · Adaptive Android UI**

<!-- Banner placeholder: add docs/images/banner.png, then uncomment the line below. -->
<!-- ![Android RecruitmentTest App banner](docs/images/banner.png) -->

![Build: CI not configured](https://img.shields.io/badge/build-CI_not_configured-lightgrey)
![Kotlin 2.4.20](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white)
![Android API 24+](https://img.shields.io/badge/Android-API_24%2B-3DDC84?logo=android&logoColor=white)
![License: pending](https://img.shields.io/badge/license-pending-lightgrey)

## App Overview

Android RecruitmentTest App lets users browse album items, view their details, and save favorites for later. It persists downloaded records locally so users can continue browsing after losing connectivity or restarting the app. Built for the leboncoin Android technical assessment, the project demonstrates modular architecture, reactive state management, and adaptive Compose interfaces.

## Key Features

- **Offline browsing:** Read previously synchronized album records from a local Room database, including after app restart.
- **Persistent favorites:** Add or remove favorites locally and access them through a dedicated tab.
- **Adaptive album details:** Use a focused detail view on compact screens and a list-detail layout when space permits.
- **Paged lists and refresh:** Load database records incrementally with Paging 3 and refresh the remote catalog through the UI.
- **Cached artwork:** Reuse images through a shared Coil image loader, with memory and disk caching and an image placeholder.

**Offline scope:** An initial successful download is required to populate the catalog. Artwork is available offline only while it remains cached; Android may evict cached files. The static endpoint returns the full catalog on refresh—pagination applies to local database reads, not network downloads. Favorite flags survive refresh for item IDs that remain in the returned catalog.

## Architecture & Tech Stack

### Engineering Decisions & Trade-offs

| Choice | Justification |
| --- | --- |
| Three Gradle modules | Enforce clear responsibilities and support isolated testing without excessive modularization. |
| MVVM + Clean Architecture | Keep UI state management separate from business operations and infrastructure. |
| Room as the source of truth | Preserve records and favorites across restarts and expose consistent, reactive data to all screens. |
| Transactional refresh | Update the catalog atomically while preserving favorites for IDs still present. |
| Hilt | Centralize dependency wiring, manage lifetimes, and avoid manual construction throughout the app. |
| Coroutines + Flow | Handle asynchronous work and propagate database changes through lifecycle-aware UI collection. |
| Paging 3 | Load database records incrementally. It does not reduce the full-catalog network download. |
| Compose + Spark | Build declarative interfaces using the company's design system and reusable components. |
| Coil | Handle image loading and caching without maintaining a custom image pipeline. |
| Robolectric + Compose tests | Test UI behavior locally without an emulator; device tests remain necessary for platform integration. |
### Architecture

The project uses **Clean Architecture with MVVM** across three Gradle modules. ViewModels expose UI state and delegate operations to domain use cases. Repository implementations coordinate the remote API and local persistence.

| Layer | Module | Responsibilities |
| --- | --- | --- |
| Presentation and composition root | `:app` | Compose screens, ViewModels, adaptive navigation, UI state, and application dependency wiring |
| Domain | `:domain` | Album model, repository contract, and use cases for reading, refreshing, and favoriting albums |
| Data | `:data` | Repository implementation, Retrofit API, DTO mapping, Room entities and DAOs, and infrastructure providers |

**Module dependencies:** `:app` depends on `:domain` and `:data`; `:data` depends on `:domain`. The domain module has no dependency on either implementation module. It is a Kotlin/JVM module with Coroutines and Paging Common dependencies.

Room is the **source of truth for displayed album data**. A refresh fetches remote records and replaces the local catalog in a transaction while preserving matching favorite IDs. Database flows propagate changes to the list, favorites, and detail screens. DTOs and database entities are mapped to domain models before reaching presentation code.

This separation keeps persistence and networking out of the UI, supports isolated tests, and allows data sources to evolve without rewriting screens.

### Libraries and tooling

| Category | Technologies |
| --- | --- |
| Language and build | Kotlin, Gradle Kotlin DSL, version catalog, KSP |
| UI and design system | Jetpack Compose, Adevinta Spark, Material 3 |
| Jetpack | Lifecycle/ViewModel, Paging 3, Material 3 Adaptive list-detail navigation |
| Persistence | Room with reactive queries and transactional writes |
| Networking and serialization | Retrofit, OkHttp, Kotlin Serialization |
| Dependency injection | Dagger Hilt |
| Asynchronous programming | Kotlin Coroutines, Flow, StateFlow |
| Image loading | Coil 3 with a shared image loader |
| Testing | JUnit 4, Mockito Kotlin, Turbine, Coroutines Test, Robolectric, Compose UI Test, AndroidX Test |
| Diagnostics | Android Lint, LeakCanary in debug builds |

### Build configuration

| Setting | Declared value |
| --- | --- |
| Minimum Android SDK | API 24 |
| Target Android SDK | API 35 |
| Compile Android SDK | API 37 |
| Kotlin | 2.4.20 |
| Android Gradle Plugin | 9.2.1 |
| Gradle wrapper | 9.8.0 |
| Java/Kotlin bytecode target | JVM 11 |

Dependency versions are maintained in [`gradle/libs.versions.toml`](gradle/libs.versions.toml). These values describe the checked-in configuration; they are not a build verification report.

## Getting Started

### Prerequisites

- **Android Studio:** Use a release supporting AGP 9.2 and the configured SDK. Quail 1 (2026.1.1) supports AGP 9.2; consult the [Android Studio compatibility table](https://developer.android.com/build/releases/about-agp) when selecting an IDE for API 37.
- **Java:** JDK 17 or a newer JDK supported by the checked-in Gradle wrapper. AGP 9.2 requires at least JDK 17; the JVM 11 bytecode target does not mean Gradle should run on JDK 11. See the [AGP requirements](https://developer.android.com/build/releases/agp-9-2-0-release-notes).
- **Android SDK:** Platform API 37, SDK Platform-Tools, and the Build Tools required by AGP. Install them through Android Studio's SDK Manager.
- **Device:** An Android device or emulator running API 24 or later.
- **Git and network access:** Required to clone the repository, resolve dependencies, and download the initial catalog.

### Setup

1. Clone the repository and enter its directory:

   ```bash
   git clone https://github.com/ycharbel88/leboncoin-android-test.git
   cd leboncoin-android-test
   chmod +x gradlew
   ```

2. Configure the local Android SDK path. Replace the example with the absolute path shown in Android Studio's SDK Manager:

   ```bash
   cat > local.properties <<'EOF'
   sdk.dir=/absolute/path/to/Android/sdk
   # Reserved for a future authenticated integration; not consumed by this app:
   # API_KEY=REPLACE_WITH_YOUR_LOCAL_VALUE
   EOF
   ```

   No API key is required for the current public [album endpoint](https://static.leboncoin.fr/img/shared/technical-test.json). Keep `local.properties` out of version control. Do not overwrite an existing file without preserving its local settings.

3. Open the repository in Android Studio, select the Gradle JDK, and synchronize the project. Check the command-line runtime and build the debug APK:

   ```bash
   java -version
   ./gradlew --version
   ./gradlew :app:assembleDebug
   ```

   Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

4. Start an emulator or connect a device with USB debugging enabled, then install:

   ```bash
   adb devices
   ./gradlew :app:installDebug
   ```

   Launch **Android RecruitmentTest App** from the device launcher, or run the `app` configuration in Android Studio.

On Windows, use `gradlew.bat` in place of `./gradlew` and create `local.properties` with your editor. Use the checked-in wrapper rather than a separately installed Gradle version. If dependency resolution fails, verify the catalog and wrapper versions before changing the toolchain.

## Testing

### Local tests

Run local tests across the project:

```bash
./gradlew test
```

For an explicit debug-only Android test run plus domain tests:

```bash
./gradlew :domain:test :data:testDebugUnitTest :app:testDebugUnitTest
```

The local test suites cover domain use cases, repository behavior, database operations, mapping, ViewModels, and Compose screens. Compose tests under `app/src/test` run on the JVM with Robolectric; they do not require an emulator.

### Instrumented tests

Start an emulator or connect an Android device, then run:

```bash
./gradlew connectedDebugAndroidTest
```

Device tests live under `src/androidTest`. The current app instrumentation suite contains a basic example test; broader UI behavior is exercised by the local Compose suites.

### Reports and manual checks

Local HTML reports are generated under each module's `build/reports/tests/` directory. Connected-test reports are generated under `build/reports/androidTests/connected/` for the relevant Android module.

Before submitting a UI or persistence change, verify browsing, favorite toggling, refresh, offline restart, rotation, and back navigation in compact and expanded layouts. Add focused regression tests for changed behavior and bug fixes. No passing-test count or coverage percentage is asserted here; use results from the commit being reviewed.

## CI/CD & Code Quality

### Current tooling

Android Lint is available through the Android Gradle Plugin:

```bash
./gradlew :app:lintDebug :data:lintDebug
```

The repository does not currently contain a checked-in CI workflow, ktlint or Detekt configuration, or an automated deployment pipeline. The build badge therefore reports **CI not configured**. LeakCanary supports memory-leak investigation in debug builds.

### Expected pull-request gates

When introducing CI, run these checks for every pull request and update to the default branch:

1. Check out the commit and provision the supported JDK and Android SDK.
2. Validate the Gradle wrapper and restore dependency caches.
3. Run local tests, Android Lint, and the debug build:

   ```bash
   ./gradlew --no-daemon :domain:test :data:testDebugUnitTest :app:testDebugUnitTest :data:lintDebug :app:lintDebug :app:assembleDebug
   ```

4. Run instrumented tests in a separate emulator job.
5. Publish test results, lint reports, and the debug APK as review artifacts.

Replace the static build badge with the real workflow badge after adding CI. Introduce ktlint or Detekt in a dedicated change with an agreed ruleset and a passing baseline. Release signing and distribution require a separate configuration; store signing credentials in CI secrets.

### Contribution guidelines

- Keep changes focused and preserve module boundaries.
- Reuse Spark components and existing UI patterns.
- Add dependencies through the version catalog and explain their purpose.
- Include relevant tests and screenshots for behavior or UI changes.
- Describe the problem, implementation, validation results, and known limitations in each pull request.
- Run the applicable quality gates before requesting review.

## License

**License selection pending.** No license file is currently present in the repository. The following is a placeholder for adopting Apache License 2.0; it does not declare that the existing code has already been licensed under those terms.

Once the rights holder confirms the license, add the full Apache License 2.0 text as `LICENSE`, replace the copyright placeholders, update the badge, and use this notice:

```text
Copyright [YEAR] [COPYRIGHT HOLDER]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

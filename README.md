# PrideQuiz

![API Level](https://img.shields.io/badge/API-26%2B-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.03.01-4285F4)
![Material3](https://img.shields.io/badge/Material3-Dynamic%20Colors-6750A4)

---

## About

PrideQuiz is an Android quiz game that tests your knowledge of LGBTQ+ history, culture, symbols, and icons. Players can challenge themselves in Normal mode or race against the clock in Time Attack, earning XP, climbing global leaderboards, and unlocking progression titles along the way.

---

## Screenshots

<p align="center">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image1.png" width="180">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image2.png" width="180">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image3.png" width="180">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image4.png" width="180">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image5.png" width="180">
  <img src="https://raw.githubusercontent.com/AlvaroQ/PrideQuiz/main/capture/image6.png" width="180">
</p>

---

## Tech Stack

| Category | Technology | Version |
|---|---|---|
| Language | Kotlin | 2.3.20 |
| Build | Android Gradle Plugin | 9.1.1 |
| UI | Jetpack Compose + Material3 | BOM 2026.03.01 |
| Architecture | Clean Architecture — 4 modules | MVVM |
| State Management | StateFlow + SharedFlow | Coroutines 1.10.2 |
| Dependency Injection | Koin (Android + Compose) | 4.2.1 |
| Backend | Firebase (Firestore, Realtime DB, Auth, Analytics, Crashlytics) | BOM 34.12.0 |
| Images | Coil Compose | 3.4.0 |
| Functional Programming | Arrow Core (Either) | 2.2.2.1 |
| Local Persistence | DataStore Preferences | 1.2.1 |
| Monetization | AdMob + Google Play Billing | 25.2.0 / 8.3.0 |
| Min SDK | Android 8.0 (Oreo) | API 26 |
| Compile / Target SDK | Android 15 | API 36 |

---

## Architecture

PrideQuiz follows **Clean Architecture** with a strict unidirectional dependency rule across four Gradle modules:

```
┌──────────────────────────────────────────────────────┐
│  app  (Presentation)                                 │
│  Jetpack Compose screens · ViewModels · Koin DI      │
│  DataSource implementations · Managers               │
└───────────────────────┬──────────────────────────────┘
                        │ uses
┌───────────────────────▼──────────────────────────────┐
│  usecases                                            │
│  Application business logic · one class per use case │
└───────────────────────┬──────────────────────────────┘
                        │ uses
┌───────────────────────▼──────────────────────────────┐
│  data                                                │
│  Repository interfaces · DataSource interfaces       │
└───────────────────────┬──────────────────────────────┘
                        │ uses
┌───────────────────────▼──────────────────────────────┐
│  domain  (pure Kotlin — zero Android dependencies)   │
│  Models: Pride · User · Name · XpLeaderboardEntry    │
└──────────────────────────────────────────────────────┘

Dependency rule: app → usecases → data → domain
```

ViewModels expose `StateFlow` for UI state and `SharedFlow` for one-shot events (navigation, dialogs). Koin handles dependency injection at every layer.

---

## Features

- Normal and Time Attack game modes
- XP progression system with levels and unlockable titles
- Global leaderboard powered by Firestore
- Personal records tracked locally
- Light / Dark theme with Material3 Dynamic Colors (Android 12+)
- High contrast and large text accessibility support
- AdMob monetization: banner, interstitial, and rewarded ads
- In-app billing to permanently remove ads

---

## Testing

PrideQuiz has a comprehensive, multi-layer testing strategy covering unit logic, UI components, visual regression, and full end-to-end visual flows on real devices.

### Unit Tests — 260 tests

| | |
|---|---|
| Location | `app/src/test/`, `data/src/test/`, `usecases/src/test/` |
| Coverage | ViewModels, Managers, Repositories, Use Cases, Koin DI verification |
| Stack | JUnit 4, MockK, Turbine (Flow testing), Koin Test |
| Runs on | JVM (no device required) |

```bash
./gradlew test
```

---

### UI Tests — Instrumented — 33 tests

| | |
|---|---|
| Location | `app/src/androidTest/` |
| Coverage | GameScreen components, ResultScreen components: `LoadingIndicator`, `TopBar`, `AnswerButtons`, `ScoreDisplay`, `StatsGrid`, `ActionButtons` |
| Stack | Compose UI Test with JUnit4 rule |
| Runs on | Connected device or emulator |

```bash
./gradlew connectedDebugAndroidTest
```

---

### Screenshot Tests — Roborazzi — 44 tests

Roborazzi screenshot tests run entirely on the JVM (via Robolectric) and provide fast visual regression coverage across multiple theme configurations.

| | |
|---|---|
| Location | `app/src/test/java/com/quiz/pride/screenshots/` |
| Coverage | All major UI components across 4 theme variants: Light, Dark, High Contrast Light, High Contrast Dark + Large Text accessibility |
| Stack | Roborazzi 1.59.0, Robolectric 4.16.1 |
| Runs on | JVM (no device required) |
| Test files | `GameComponentsScreenshotTest`, `ResultComponentsScreenshotTest`, `ThemeScreenshotTest` |

```bash
# Record baselines
./gradlew recordRoborazziDebug

# Verify against recorded baselines
./gradlew verifyRoborazziDebug

# Generate HTML report
./gradlew generateScreenshotReport
```

Report output: `app/build/reports/screenshots/index.html`

---

### Visual Regression Tests — Appium + Percy — 3 specs

Full end-to-end visual regression tests that capture real device rendering and compare screenshots using Percy's AI-powered visual diff dashboard.

| | |
|---|---|
| Location | `appium-visual-tests/` |
| Coverage | Full-screen visual regression across complete user flows on real devices |
| Stack | Appium 3.2, WebdriverIO 9, Percy (BrowserStack) |
| Runs on | Connected device |

```bash
npm run test:percy
```

Setup and configuration: see `appium-visual-tests/README.md`

---

### Test Summary

| Type | Tests | Runs on | Speed | Command |
|---|---|---|---|---|
| Unit | 260 | JVM | Fast | `./gradlew test` |
| UI — Instrumented | 33 | Device / Emulator | Medium | `./gradlew connectedDebugAndroidTest` |
| Screenshot — Roborazzi | 44 | JVM | Fast | `./gradlew verifyRoborazziDebug` |
| Visual — Appium + Percy | 3 specs | Device | Slow | `npm run test:percy` |

---

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/AlvaroQ/PrideQuiz.git
   ```

2. Add your `google-services.json` (Firebase) to the `app/` directory.

3. Add AdMob keys to `local.properties`:
   ```properties
   ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx
   ADMOB_BANNER_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
   ADMOB_INTERSTITIAL_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
   ADMOB_REWARDED_ID=ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx
   ```

4. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Links

- [Play Store](https://play.google.com/store/apps/details?id=com.quiz.pride)
- [Promo Video](https://youtu.be/4PY0JA5JFXI)

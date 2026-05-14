# 90phase — Sleep Cycle Optimizer

A native Android app that calculates optimal sleep schedules based on the ~90-minute human sleep cycle.

## What It Does

90phase helps you wake up refreshed by timing your alarm to the end of a sleep cycle, not the middle of one. Enter your target wake-up time and get 2–3 optimal bedtimes.

## Core Features

- **Sleep calculator** — enter wake-up time, get optimal bedtimes
- **Daily check-in notification** at 18:00 ("När ska du vakna imorgon?")
- **Discovery Phase** — personalizes your cycle parameters over 21 days
- **Sleep log history** with weekly/monthly quality ratings
- **Offline-first** — works without internet, syncs to Firebase in background

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose + Material3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Local DB | Room |
| Reactive | Coroutines + StateFlow |
| Background | WorkManager |
| Navigation | Navigation Compose |
| Cloud sync | Firebase Firestore + Auth |
| Min SDK | 26 (Android 8.0) |

## Module Structure

```
90phase/
├── app/            # Entry point, Hilt application, DI modules
├── presentation/   # Compose screens, ViewModels, navigation, theme
├── domain/         # Pure Kotlin — entities, use cases, repository interfaces
└── data/           # Repository implementations, Room, DataStore, Firebase
```

## Getting Started

1. Clone the repository
2. Add your `google-services.json` to `app/` (not committed — see `.gitignore`)
3. Open in Android Studio Hedgehog or later
4. Run on a device or emulator with API 26+

## Build Commands

```bash
./gradlew detekt        # Static analysis
./gradlew ktlintCheck   # Code style check
./gradlew test          # Unit tests
./gradlew build         # Full build
```

## Security

The following files are intentionally excluded from version control:

- `local.properties` — Android SDK path
- `google-services.json` — Firebase configuration
- `.env` / `secrets.properties` — API keys and credentials
- `*.jks` / `*.keystore` — Signing keystores

Never commit these files.

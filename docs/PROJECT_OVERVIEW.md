# Sleep Cycle Optimizer

> A native Android application that calculates optimal sleep schedules based on human sleep cycles, designed with Clean Architecture principles and offline-first data synchronization.

---

## Table of Contents
1. [Project Vision](#project-vision)
2. [Technical Overview](#technical-overview)
3. [Architecture Philosophy](#architecture-philosophy)
4. [Module Structure](#module-structure)
5. [Technology Stack](#technology-stack)
6. [Core Features](#core-features)
7. [Development Roadmap](#development-roadmap)

---

## Project Vision

### Problem Statement
Most people wake up feeling groggy not because they didn't sleep enough, but because they woke up in the middle of a sleep cycle. This phenomenon, known as **sleep inertia**, can be avoided by timing wake-ups to coincide with the end of a 90-minute sleep cycle.

### Solution
A **quiet assistant** that:
- Calculates optimal bedtimes based on desired wake-up times
- Sends gentle daily reminders (18:00) asking "När ska du vakna imorgon?"
- Learns individual sleep patterns through an optional Discovery Phase
- Works completely offline with background cloud synchronization

### Target Users
- Primary: Partner (real-world validation)
- Secondary: Portfolio piece demonstrating senior Android engineering capabilities

---

## Technical Overview

### Architecture Style
**Clean Architecture + MVVM** with strict dependency rules and offline-first data strategy.

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| Multi-module structure | Enforces compile-time dependency rules, improves build times |
| Pure Kotlin domain layer | Enables fast unit testing, platform independence |
| Room as source of truth | Offline-first UX, Firebase is secondary sync mechanism |
| Jetpack Compose | Modern declarative UI, reduces boilerplate |
| StateFlow over LiveData | Better coroutine integration, more Kotlin-idiomatic |
| Sealed classes for Result types | Type-safe error handling without exceptions |

---

## Architecture Philosophy

### The Three Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Jetpack Compose Screens + ViewModels              │     │
│  │  - Only depends on Domain layer                    │     │
│  │  - Manages UI state via StateFlow                  │     │
│  │  - No business logic                               │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓ (UseCases only)
┌─────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                           │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Pure Kotlin - Business Logic                      │     │
│  │  - Entities (UserProfile, SleepLog)                │     │
│  │  - UseCases (CalculateOptimalBedtimeUseCase)       │     │
│  │  - Repository Interfaces                           │     │
│  │  - NO Android/Firebase dependencies                │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
                            ↓ (Repository implementations)
┌─────────────────────────────────────────────────────────────┐
│                        DATA LAYER                            │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Repository Implementations                        │     │
│  │  - Room (local database)                           │     │
│  │  - DataStore (user preferences)                    │     │
│  │  - Firebase Firestore (cloud sync)                 │     │
│  │  - Offline-first strategy                          │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow Principles

**Read Path (Offline-First):**
```
UI Request → ViewModel → UseCase → Repository
                                        ↓
                                    Room (Local DB)
                                        ↓
                                StateFlow to UI
                                        
[Background: Firebase sync check via WorkManager]
```

**Write Path:**
```
User Action → ViewModel → UseCase → Repository
                                        ↓
                                1. Write to Room (immediate)
                                2. Queue Firebase sync (WorkManager)
                                3. Emit success to UI
```

---

## Module Structure

```
sleep-cycle-optimizer/
├── app/                          # Application entry point
│   ├── di/                       # Dependency injection (Hilt modules)
│   └── SleepOptimizerApplication.kt
│
├── presentation/                 # UI Layer
│   ├── screens/
│   │   ├── calculator/           # Main sleep calculator screen
│   │   ├── history/              # Sleep log history
│   │   ├── settings/             # User preferences & discovery phase
│   │   └── onboarding/           # First-time user flow
│   ├── viewmodels/
│   ├── navigation/
│   └── theme/                    # Compose Material3 theme
│
├── domain/                       # Pure Kotlin - Business Logic
│   ├── entities/
│   │   ├── UserProfile.kt
│   │   ├── SleepLog.kt
│   │   └── SleepCalculation.kt
│   ├── usecases/
│   │   ├── CalculateOptimalBedtimeUseCase.kt
│   │   ├── LogSleepSessionUseCase.kt
│   │   ├── GetSleepHistoryUseCase.kt
│   │   └── UpdateDiscoveryPhaseUseCase.kt
│   ├── repositories/             # Interfaces only
│   │   ├── SleepRepository.kt
│   │   └── UserPreferencesRepository.kt
│   └── common/
│       └── Result.kt             # Sealed class for error handling
│
└── data/                         # Data Layer
    ├── local/
    │   ├── room/
    │   │   ├── SleepDatabase.kt
    │   │   ├── dao/
    │   │   └── entities/         # Room entities (separate from domain)
    │   └── datastore/
    │       └── UserPreferencesDataStore.kt
    ├── remote/
    │   ├── firebase/
    │   │   ├── FirestoreSyncManager.kt
    │   │   └── models/           # Firestore DTOs
    │   └── sync/
    │       └── SleepDataSyncWorker.kt
    └── repositories/              # Repository implementations
        ├── SleepRepositoryImpl.kt
        └── UserPreferencesRepositoryImpl.kt
```

---

## Technology Stack

### Core Framework
- **Language:** Kotlin 1.9+
- **Min SDK:** 26 (Android 8.0) - for NotificationChannel support
- **Target SDK:** 34 (Android 14)
- **Build System:** Gradle with Kotlin DSL

### Architecture Components
| Layer | Technology | Purpose |
|-------|------------|---------|
| **Presentation** | Jetpack Compose | Declarative UI |
| | Material3 | Modern design system |
| | Navigation Compose | Type-safe navigation |
| | Hilt | Dependency injection |
| **Domain** | Pure Kotlin | Business logic isolation |
| | Coroutines | Async operations |
| | Flow | Reactive streams |
| **Data** | Room | Local database |
| | DataStore | Key-value preferences |
| | Firebase Auth | User authentication |
| | Firestore | Cloud data sync |
| | WorkManager | Background sync tasks |

### Quality Assurance
- **Testing:** JUnit5, Kotest, Turbine (Flow testing)
- **Code Quality:** Detekt, ktlint
- **CI/CD:** GitHub Actions (planned)

---

## Core Features

### 1. Sleep Calculator (MVP Core)
**Input:** Target wake-up time  
**Output:** 2-3 optimal bedtimes based on 90-minute cycles

**Algorithm:**
```kotlin
// Base assumptions
val CYCLE_DURATION = 90.minutes
val SLEEP_LATENCY = 15.minutes

// Example: Wake at 07:00
// 6 cycles: 07:00 - (6 * 90min) - 15min = 21:45 bedtime
// 5 cycles: 07:00 - (5 * 90min) - 15min = 23:15 bedtime
```

**UI Behavior:**
- Clean, minimal input (time picker)
- Display results with visual cycle indicators
- One-tap "Set Reminder" button

---

### 2. Daily Check-In Notification
**Trigger:** 18:00 every day (configurable)  
**Message:** "När ska du vakna imorgon?"

**Flow:**
1. User taps notification
2. App opens to quick time-picker
3. Shows bedtime recommendations
4. Reminder: "Lägg dig 15 minuter innan för att hinna somna"
5. Optional: Set bedtime alarm (via system AlarmManager)

**Smart Alarm Integration (NEW):**
If user hasn't set a wake-up time in the app, the system checks the phone's alarm app at 18:00:
- Detects next scheduled alarm (e.g., 07:00)
- Prompts: "Jag ser att du har larm 07:00. Vill du få sömncykel-rekommendationer baserat på den tiden?"
- User can accept (auto-fill) or set custom time
- Requires `READ_ALARM` permission (Android API 31+)

**Bedtime Reminder (21:30):**
After user selects their optimal bedtime recommendation:
- App schedules a second notification at the recommended bedtime (e.g., 21:30)
- Message: "Dags att varva ner" 
- Helps user prepare for sleep 15 minutes before optimal bedtime
- One-time notification (resets daily based on check-in response)

**Implementation:**
- `AlarmManager` for exact 18:00 trigger
- `PendingIntent` to open app with pre-filled state
- Notification channel: "Daily Sleep Reminders" (user can disable)
- System alarm detection via `AlarmManager.getNextAlarmClock()`

---

### 3. Discovery Phase (Optional Feature)
**Purpose:** Personalize sleep cycle parameters when user consistently feels unrested

**Activation:**
- User enables in Settings
- Requires 7+ days of wake-up ratings (1-5 stars)

**Testing Strategy:**
Instead of 1-minute adjustments (scientifically questionable due to variable sleep latency), test **meaningful shifts**:

| Shift | Hypothesis | Duration |
|-------|------------|----------|
| **Shift 1** | Longer sleep latency | Test 30min latency for 7 days |
| **Shift 2** | Different cycle length | Test 105min cycles for 7 days |
| **Shift 3** | Fewer cycles needed | Test 5 cycles instead of 6 for 7 days |

**Data Collection:**
- Morning prompt: "How rested do you feel?" (1-5)
- Track correlation between shift type and ratings
- Recommend optimal parameters after 21-day test period

**Domain Model:**
```kotlin
data class DiscoveryPhase(
    val isActive: Boolean,
    val currentShift: ShiftType,
    val startDate: LocalDate,
    val ratings: List<DailyRating>
)

sealed class ShiftType {
    object LongerLatency : ShiftType()    // 30min instead of 15min
    object LongerCycles : ShiftType()     // 105min instead of 90min
    object FewerCycles : ShiftType()      // 5 instead of 6
}
```

---

### 4. Sleep History
**Display:**
- Calendar view with color-coded ratings
- Average sleep quality per week/month
- Insights: "Your best nights are when you sleep 6 cycles"

**Analytics (Local Only):**
- Correlation between bedtime consistency and ratings
- Optimal cycle count per user
- No data leaves device unless user opts into Firebase sync

---

## Development Roadmap

### Phase 1: MVP Foundation (Weeks 1-2)
- [ ] Project setup with multi-module structure
- [ ] Domain layer entities and use cases
- [ ] Room database + DataStore setup
- [ ] Basic calculator screen (Compose)
- [ ] Unit tests for domain layer (80%+ coverage)

### Phase 2: Core Features (Weeks 3-4)
- [ ] Daily notification system (AlarmManager)
- [ ] Sleep log history screen
- [ ] Settings screen with preferences
- [ ] Repository implementations (offline-first)
- [ ] Integration tests for data layer

### Phase 3: Cloud Sync (Week 5)
- [ ] Firebase Authentication
- [ ] Firestore schema design
- [ ] Background sync worker (WorkManager)
- [ ] Conflict resolution strategy
- [ ] Network error handling

### Phase 4: Discovery Phase (Week 6)
- [ ] Discovery phase settings UI
- [ ] Morning rating prompt notification
- [ ] Analytics calculation use cases
- [ ] Recommendation engine
- [ ] A/B test validation logic

### Phase 5: Polish & Portfolio Prep (Week 7)
- [ ] Material3 theming refinement
- [ ] Animations and transitions
- [ ] Edge case handling (timezone changes, etc.)
- [ ] Performance profiling (Compose recompositions)
- [ ] README with architecture diagrams
- [ ] Video demo for portfolio

### Post-MVP Backlog
- [ ] Widget support (quick bedtime calculation)
- [ ] Export sleep data (CSV/PDF)
- [ ] Sleep debt calculator
- [ ] Integration with health apps (Google Fit)
- [ ] Wear OS companion app
- [ ] Smart alarm (wake within 10-min window at optimal time)

---

## Out of Scope (MVP)
To maintain focus and ship quickly, the following are **explicitly excluded**:

❌ Social features (sharing, leaderboards)  
❌ Meditation/sounds library  
❌ Sleep tracking via sensors (accelerometer)  
❌ Multi-user accounts on one device  
❌ Paid subscription model  
❌ Backend API (Firebase only)  

---

## Portfolio Highlights

This project demonstrates:

✅ **Clean Architecture mastery** - Strict layer separation with compile-time enforcement  
✅ **Modern Android development** - Compose, Hilt, Coroutines, Flow  
✅ **Offline-first architecture** - Room as source of truth, graceful sync  
✅ **Domain-driven design** - Rich entities, use cases, repository pattern  
✅ **Testing discipline** - Unit, integration, and UI tests with high coverage  
✅ **Real-world problem solving** - Discovery Phase shows scientific thinking  
✅ **Production-ready code quality** - Detekt, ktlint, comprehensive error handling  

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1+)
- JDK 17
- Kotlin 1.9+
- Firebase project (for sync features)

### Build & Run
```bash
# Clone repository
git clone [repository-url]
cd sleep-cycle-optimizer

# Build project
./gradlew build

# Run tests
./gradlew test

# Install debug build
./gradlew installDebug
```

### Project Configuration
1. Add `google-services.json` to `app/` directory
2. Update `local.properties` with Firebase credentials
3. Sync Gradle files

---

## Contributing
This is a personal portfolio project, but feedback is welcome via issues.

---

## License
MIT License - See LICENSE file for details

---

**Built with ❤️ using Clean Architecture and modern Android best practices**

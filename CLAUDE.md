# CLAUDE.md — 90phase (Sleep Cycle Optimizer)
AI-Assisted Development Workflow

> This document defines how Claude Code should work in this repository.
> Claude Code works interactively with the developer — changes are reviewed in real time before committing.

---

## Project Overview

**90phase** is a native Android app that calculates optimal sleep schedules based on 90-minute human sleep cycles.

The name refers to the ~90-minute sleep cycle that repeats throughout the night.

Mission: A quiet assistant that helps users wake up refreshed by timing their alarm to the end of a sleep cycle — not the middle of one.

Primary user: Partner (real-world validation) + portfolio piece demonstrating senior Android engineering.

Core features:
- Sleep calculator — enter wake-up time, get 2–3 optimal bedtimes
- Daily check-in notification at 18:00 ("När ska du vakna imorgon?")
- Optional Discovery Phase — personalizes cycle parameters over 21 days
- Sleep log history with weekly/monthly ratings
- Offline-first — works without internet, syncs to Firebase in background

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin 1.9+ |
| UI | Jetpack Compose + Material3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt |
| Local DB | Room |
| Preferences | DataStore |
| Reactive | Coroutines + StateFlow + Flow |
| Background | WorkManager |
| Navigation | Navigation Compose |
| Cloud sync | Firebase Firestore |
| Auth | Firebase Auth |
| Notifications | AlarmManager + NotificationManager |
| Testing | JUnit5, Kotest, Turbine |
| Code quality | Detekt, ktlint |
| Build | Gradle with Kotlin DSL |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 36 |

---

## Architecture Rules (STRICT)

### The Three Immutable Laws

1. **Domain Independence** — `:domain` SHALL NOT import Android framework, Firebase, Room, or any external library except Kotlin stdlib and coroutines.
2. **Single Source of Truth** — Room IS the source of truth. Firebase is secondary sync, never the primary data store.
3. **Unidirectional Data Flow** — Data flows DOWN: Presentation → Domain → Data. The Data layer NEVER directly updates UI state.

### Layer Dependency Rules

```
Presentation (Compose screens + ViewModels)
  ✅ depends on: :domain (UseCases, Entities, Repository interfaces), Hilt, Compose, StateFlow
  ❌ never on: Room, DataStore, Firebase, repository implementations

Domain (pure Kotlin)
  ✅ depends on: Kotlin stdlib, coroutines only
  ❌ never on: Android, Firebase, Room, DataStore

Data (repository implementations)
  ✅ depends on: :domain interfaces, Room, DataStore, Firebase
  ❌ never on: Presentation layer
```

### Data Flow

**Read (offline-first):**
```
UI → ViewModel → UseCase → Repository interface → Room (emit via Flow)
[Background: WorkManager checks Firebase sync]
```

**Write:**
```
User action → ViewModel → UseCase → Repository impl
  → 1. Write to Room immediately
  → 2. Queue Firebase sync via WorkManager
  → 3. Emit success via StateFlow to UI
```

### Result Type

All repository methods and use cases return `Result<T>` (sealed class in `:domain/common/Result.kt`).  
Never throw exceptions across layer boundaries.

---

## Module Structure

```
90phase/
├── app/                        # Entry point, Hilt application, DI modules
├── presentation/               # Compose screens, ViewModels, navigation, theme
│   ├── screens/
│   │   ├── calculator/
│   │   ├── history/
│   │   ├── settings/
│   │   └── onboarding/
│   ├── viewmodels/
│   ├── navigation/
│   └── theme/
├── domain/                     # Pure Kotlin — entities, use cases, repo interfaces
│   ├── entities/
│   ├── usecases/
│   ├── repositories/
│   └── common/Result.kt
└── data/                       # Repository implementations, Room, DataStore, Firebase
    ├── local/
    │   ├── room/
    │   └── datastore/
    ├── remote/firebase/
    └── repositories/
```

---

## Domain Entities

```kotlin
// Core entities in :domain/entities/
UserProfile       // sleep latency, preferred cycle count, discovery phase state
SleepLog          // date, wake time, quality rating (1–5), cycles used
SleepCalculation  // wake time → list of recommended bedtimes

// Algorithm constants
CYCLE_DURATION  = 90.minutes
SLEEP_LATENCY   = 15.minutes  // default, adjustable in Discovery Phase
```

---

## Design System

Material3 with custom color scheme. Never hardcode color values — always use the theme.

```
Primary:       Deep indigo / night sky tone
OnPrimary:     White
Secondary:     Muted blue-grey
Surface:       Near-black for dark mode, near-white for light
Typography:    Material3 defaults — adjust weight/size, never font family
```

Compose guidelines:
- `MaterialTheme.colorScheme.*` — never hardcoded hex
- `MaterialTheme.typography.*` — never hardcoded sp
- `MaterialTheme.shapes.*` — cards 16.dp, buttons 12.dp
- Spacing scale: 4, 8, 12, 16, 24, 32 dp — no arbitrary values

---

## How Claude Code Works Here

- Read existing code before making changes
- Show what will change before significant edits
- Ask for clarification when a task is ambiguous
- Keep scope tight — only implement what is discussed
- UI-only work: use typed fake/preview data + `// TODO: wire to ViewModel`
- **Never commit without explicit developer instruction** — finish the work, then wait to be told to commit or for /git-ship to be invoked
- Never add Gradle dependencies without explaining why and getting approval
- Never touch `local.properties`, `google-services.json`, or any secrets file
- Never push or open PRs without explicit developer instruction

---

## Branch Naming

```
90p/PH-<N>-short-description
```

Examples:
```
90p/PH-01-project-setup
90p/PH-05-sleep-calculator-screen
90p/PH-10-daily-notification
```

---

## Commit Rules

All commits follow: `(MODEL_NAME) <type> [PH-<N>] description`  
MODEL_NAME must reflect the actual Claude model running the session — never hardcode a model name.  
Valid examples: `claude-sonnet-4-6`, `claude-opus-4-7`, `claude-haiku-4-5`

```
(claude-sonnet-4-6) chore [PH-02] Configure Gradle build system
(claude-sonnet-4-6) feat [PH-05] Add sleep calculator screen
```

Types: `feat` `fix` `style` `refactor` `chore` `docs` `perf` `test`

Rules:
- No `[claude]` prefix — format starts with `(MODEL_NAME)`
- Imperative tense always
- Under 72 characters
- Never include `Co-Authored-By` footer
- Never `git add .` or `git add -A` — stage specific files only
- Never `--no-verify`

---

## Pull Request Rules

Target branch is always `main`.

**PR Title:**
```
<type> [PH-<N>] Title Case Summary
```

**PR Body Template (REQUIRED):**
```
## PH-<N> Implementation Complete

Task: <ticket reference>

### Summary
<One or two sentences.>

### What Was Done
1. **Domain layer** — <item or "Not touched">
2. **Data layer** — <item or "Not touched">
3. **Presentation layer** — <item or "Not touched">
4. **DI / Hilt modules** — <item or "Not touched">
5. **Tests** — <item or "Not touched">

### Acceptance Criteria Coverage
- [ ] AC1
- [ ] AC2

### Manual Test Steps
1.
2.

### Notes / Tradeoffs
- <Placeholders? TODOs? New dependencies? Schema changes? Architecture decisions?>
```

---

## Validation Commands

```bash
./gradlew detekt        # Must pass — fix all violations before committing
./gradlew ktlintCheck   # Must pass — auto-fix with ktlintFormat
./gradlew test          # Unit tests must pass (domain + data layers)
./gradlew build         # Must succeed — no Kotlin compilation errors
```

---

## Claude Code Restrictions

Must NOT:
- Import Android/Firebase into `:domain` module
- Add Gradle dependencies without approval
- Hardcode API keys, credentials, or Firebase config values in code
- Push or open PRs without explicit instruction
- Take destructive actions (Room migrations, schema drops) without confirmation
- Modify `local.properties` or `google-services.json`
- Trigger WorkManager or AlarmManager side effects in unit tests

---

## Environment / Secrets (never committed)

```
local.properties          # SDK path — never committed
google-services.json      # Firebase config — never committed, add to .gitignore
```

---

## Pre-Merge Checklist

- `./gradlew detekt` passes
- `./gradlew ktlintCheck` passes
- `./gradlew test` passes
- `./gradlew build` succeeds
- `:domain` has zero Android/Firebase imports
- No hardcoded colors, spacing, or typography values in Compose
- No `any`-equivalent (`Any`, unchecked casts) in domain/data layers
- All new repository methods return `Result<T>`
- StateFlow exposed from ViewModel, not MutableStateFlow
- New Gradle dependencies approved
- Commits follow `(MODEL_NAME) <type> [PH-<N>]` format
- PR template filled out
- Room migration provided if schema changed

---

## Key Business Rules — Never Violate

- Domain layer stays pure Kotlin — no exceptions
- Room is source of truth — never read directly from Firebase in a use case
- Offline first — all features must work without network
- No background work without WorkManager (no raw threads, no GlobalScope)
- Notification channels registered in Application class, not Activity
- `google-services.json` is never committed to git

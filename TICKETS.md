# Sleep Cycle Optimizer - Ticket Index

**Total Tickets:** 50+  
**Estimated Total Time:** 7-8 weeks  
**Target:** MVP Release

---

## Epic Overview

| Epic | Priority | Time | Dependencies | Tickets |
|------|----------|------|--------------|---------|
| EPIC-0: Project Setup | P0 | 1-2 days | None | 5 |
| EPIC-1: Domain Layer | P0 | 3-4 days | EPIC-0 | 8 |
| EPIC-2: UI Design | P0 | 5-6 days | EPIC-0 | 10 |
| EPIC-3: Data Layer | P0 | 4-5 days | EPIC-1 | 8 |
| EPIC-4: Notifications | P0 | 3-4 days | EPIC-3 | 8 |
| EPIC-5: Firebase Sync | P1 | 3-4 days | EPIC-3 | 8 |
| EPIC-6: Discovery Phase | P2 | 3-4 days | EPIC-4 | 6 |
| EPIC-7: ViewModels | P0 | 3-4 days | EPIC-1,2,3 | 8 |
| EPIC-8: Testing | P0 | 4-5 days | All | 8 |
| EPIC-9: Polish & MVP | P0 | 3-4 days | All | 8 |

**Total:** ~69 tickets across 10 EPICs

---

## Week-by-Week Roadmap

### Week 1-2: Foundation
- EPIC-0: Project Setup
- EPIC-1: Domain Layer
- Start EPIC-2: UI Design System

### Week 3: UI Implementation
- Complete EPIC-2: All screens
- Start EPIC-3: Data Layer

### Week 4: Data & Integration
- Complete EPIC-3: Room + DataStore
- Start EPIC-7: ViewModels

### Week 5: Notifications & Sync
- EPIC-4: Notification System
- Start EPIC-5: Firebase Sync

### Week 6: Discovery & Polish
- EPIC-6: Discovery Phase (optional)
- Complete EPIC-5: Firebase Sync
- Start EPIC-8: Testing

### Week 7: Testing & Polish
- Complete EPIC-8: All tests passing
- EPIC-9: Polish & documentation
- MVP Release 🚀

---

## Critical Path (MVP)

Required for MVP release:

1. ✅ EPIC-0: Project Setup
2. ✅ EPIC-1: Domain Layer
3. ✅ EPIC-2: UI Design (all 4 screens)
4. ✅ EPIC-3: Data Layer (Room + DataStore)
5. ✅ EPIC-4: Notifications (all 3 types)
6. ✅ EPIC-7: ViewModels & Integration
7. ✅ EPIC-8: Testing (coverage targets)
8. ✅ EPIC-9: Polish & MVP Release

**Optional for MVP:**
- EPIC-5: Firebase Sync (can be post-MVP)
- EPIC-6: Discovery Phase (can be post-MVP)

---

## Ticket Naming Convention

```
[EPIC-X] Epic Name
├─ [PREFIX-1] Story/Task Name
├─ [PREFIX-2] Story/Task Name
└─ ...

Prefixes:
- SETUP: Project setup tasks
- DOMAIN: Domain layer implementation
- UI: UI design & screens
- DATA: Data layer implementation
- NOTIF: Notification system
- FIREBASE: Firebase integration
- DISCOVERY: Discovery Phase
- VM: ViewModel implementation
- TEST: Testing tasks
- POLISH: Polish & bug fixes
```

---

## Priority Levels

- **P0:** Must have for MVP
- **P1:** Should have (high value)
- **P2:** Nice to have (can defer)
- **P3:** Future consideration

---

## Ticket Files

### Project Setup (EPIC-0)
- `EPIC-0-PROJECT-SETUP.md`
- `SETUP-1-create-multi-module-structure.md`
- `SETUP-2-configure-gradle-build.md`

### Domain Layer (EPIC-1)
- `EPIC-1-DOMAIN-LAYER.md`
- `DOMAIN-4-sleep-calculation-usecase.md`

### UI Design (EPIC-2)
- `EPIC-2-UI-DESIGN.md`
- `UI-1-implement-design-system.md`
- `UI-2-create-component-library.md`
- `UI-4-home-screen.md`
- `UI-5-history-screen.md`
- `UI-6-settings-screen.md`
- `UI-7-onboarding-flow.md`

### Data Layer (EPIC-3)
- `EPIC-3-DATA-LAYER.md`

### Notifications (EPIC-4)
- `EPIC-4-NOTIFICATIONS.md`

### Firebase Sync (EPIC-5)
- `EPIC-5-FIREBASE-SYNC.md`

### Discovery Phase (EPIC-6)
- `EPIC-6-DISCOVERY-PHASE.md`

### ViewModels (EPIC-7)
- `EPIC-7-VIEWMODELS-INTEGRATION.md`

### Testing (EPIC-8)
- `EPIC-8-TESTING.md`

### Polish (EPIC-9)
- `EPIC-9-POLISH-MVP.md`

---

## Integration with GitHub Issues

To import these tickets to GitHub:

1. Use GitHub CLI:
   ```bash
   gh issue create --title "EPIC-0: Project Setup" --body-file EPIC-0-PROJECT-SETUP.md --label "epic,p0"
   ```

2. Or use GitHub API:
   ```bash
   curl -X POST https://api.github.com/repos/{owner}/{repo}/issues \
     -H "Authorization: token $GITHUB_TOKEN" \
     -d @EPIC-0-PROJECT-SETUP.md
   ```

3. Or import via GitHub Projects:
   - Create Project
   - Add tickets as issues
   - Link tickets to EPICs

---

## Tracking Progress

Use GitHub Project Board with columns:
- 📋 Backlog
- 🏗️ In Progress
- 👀 In Review
- ✅ Done

---

**Last Updated:** 2024-01-XX  
**Total Estimated Time:** 7-8 weeks to MVP

---


---

# EPIC 0: Project Setup

**Epic Goal:** Initialize Android project with multi-module Clean Architecture structure

**Priority:** P0 (Must have)  
**Estimated Time:** 1-2 days  
**Dependencies:** None

---

## Success Criteria

- [ ] Multi-module project structure created
- [ ] Gradle build system configured with Kotlin DSL
- [ ] All dependencies defined in version catalog
- [ ] Hilt dependency injection setup
- [ ] Base classes and common utilities created
- [ ] Git repository initialized with proper .gitignore
- [ ] README.md with project overview

---

## Stories

- [SETUP-1] Create Multi-Module Project Structure
- [SETUP-2] Configure Gradle Build System
- [SETUP-3] Setup Hilt Dependency Injection
- [SETUP-4] Configure Git Repository
- [SETUP-5] Create Base Classes and Utilities

---

## Technical Notes

**Module Structure:**
```
SleepCycleOptimizer/
├── app/                 # Application entry point
├── domain/              # Pure Kotlin business logic
├── data/                # Data layer (Room, Firebase)
└── presentation/        # UI layer (Compose)
```

**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 34 (Android 14)  
**Kotlin Version:** 1.9+

---

## Related Documentation

- `PROJECT_OVERVIEW.md` - Overall project structure
- `ARCHITECTURE_GUARDRAILS.md` - Module dependency rules
- `DEPENDENCY_INJECTION.md` - Hilt setup

---

# SETUP-1: Create Multi-Module Project Structure

**Story:** As a developer, I need a multi-module project structure so that Clean Architecture dependency rules are enforced at compile-time.

**Epic:** EPIC-0 Project Setup  
**Priority:** P0  
**Estimated Time:** 2 hours  
**Type:** Task

---

## Acceptance Criteria

- [ ] Android Studio project created with empty activity
- [ ] Four modules created: `app`, `domain`, `data`, `presentation`
- [ ] Each module has correct `build.gradle.kts` configuration
- [ ] Package structure created per module:
  - `domain/`: `entities/`, `usecases/`, `repositories/`, `common/`
  - `data/`: `local/`, `remote/`, `repositories/`
  - `presentation/`: `screens/`, `viewmodels/`, `navigation/`, `theme/`
  - `app/`: `di/`
- [ ] Module dependencies configured correctly (domain ← data ← presentation)

---

## Implementation Steps

1. Create new Android project in Android Studio
   - Name: `SleepCycleOptimizer`
   - Package: `com.sleepoptimizer`
   - Minimum SDK: 26
   - Initial Activity: Empty Compose Activity

2. Create `domain` module:
   ```bash
   File → New → New Module → Android Library
   Name: domain
   Package: com.sleepoptimizer.domain
   ```

3. Create `data` module:
   ```bash
   File → New → New Module → Android Library
   Name: data
   Package: com.sleepoptimizer.data
   ```

4. Create `presentation` module:
   ```bash
   File → New → New Module → Android Library
   Name: presentation
   Package: com.sleepoptimizer.presentation
   ```

5. Configure module dependencies in `app/build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(project(":presentation"))
       implementation(project(":domain"))
       implementation(project(":data"))
   }
   ```

6. Configure `domain/build.gradle.kts` (pure Kotlin):
   ```kotlin
   plugins {
       id("java-library")
       id("org.jetbrains.kotlin.jvm")
   }
   
   dependencies {
       implementation(libs.kotlin.stdlib)
       implementation(libs.kotlinx.coroutines.core)
   }
   ```

7. Create package structure in each module

---

## Definition of Done

- [ ] All four modules compile successfully
- [ ] `./gradlew build` passes
- [ ] No circular dependencies detected
- [ ] Package structure matches specification

---

## References

- `ARCHITECTURE_GUARDRAILS.md` - Layer dependency rules
- `PROJECT_OVERVIEW.md` - Module structure section

---

# SETUP-2: Configure Gradle Build System

**Story:** As a developer, I need a centralized dependency management system so that all modules use consistent library versions.

**Epic:** EPIC-0 Project Setup  
**Priority:** P0  
**Estimated Time:** 1 hour  
**Type:** Task

---

## Acceptance Criteria

- [ ] Version catalog created (`libs.versions.toml`)
- [ ] All common dependencies defined in catalog
- [ ] All modules reference catalog instead of hardcoded versions
- [ ] Gradle build optimization configured (build cache, parallel execution)
- [ ] ktlint plugin configured
- [ ] Detekt plugin configured

---

## Implementation Steps

1. Create `gradle/libs.versions.toml`:
   ```toml
   [versions]
   kotlin = "1.9.20"
   compose = "1.5.4"
   hilt = "2.48"
   room = "2.6.0"
   
   [libraries]
   kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
   kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version = "1.7.3" }
   kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version = "1.7.3" }
   
   # Compose
   androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
   androidx-compose-material3 = { module = "androidx.compose.material3:material3", version = "1.1.2" }
   
   # Hilt
   hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
   hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
   
   # Room
   room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
   room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
   room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
   
   # Firebase
   firebase-auth = { module = "com.google.firebase:firebase-auth-ktx", version = "22.3.0" }
   firebase-firestore = { module = "com.google.firebase:firebase-firestore-ktx", version = "24.9.1" }
   
   # DataStore
   datastore-preferences = { module = "androidx.datastore:datastore-preferences", version = "1.0.0" }
   
   # WorkManager
   work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version = "2.9.0" }
   
   # Testing
   junit = { module = "junit:junit", version = "4.13.2" }
   kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
   kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version = "1.7.3" }
   
   [plugins]
   android-application = { id = "com.android.application", version = "8.1.4" }
   android-library = { id = "com.android.library", version = "8.1.4" }
   kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
   hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
   ksp = { id = "com.google.devtools.ksp", version = "1.9.20-1.0.14" }
   ```

2. Configure root `build.gradle.kts`:
   ```kotlin
   plugins {
       alias(libs.plugins.android.application) apply false
       alias(libs.plugins.kotlin.android) apply false
       alias(libs.plugins.hilt) apply false
       alias(libs.plugins.ksp) apply false
   }
   ```

3. Add build optimization to `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx2048m
   org.gradle.caching=true
   org.gradle.parallel=true
   org.gradle.configureondemand=true
   android.useAndroidX=true
   kotlin.code.style=official
   ```

4. Add ktlint configuration (`.editorconfig`)

5. Add Detekt configuration (`detekt.yml`)

---

## Definition of Done

- [ ] All modules build successfully using version catalog
- [ ] No hardcoded dependency versions in module build files
- [ ] Build time improved with caching and parallel execution
- [ ] ktlint and Detekt run without errors

---

## References

- `ARCHITECTURE_GUARDRAILS.md` - Code quality standards section

---

# EPIC 1: Domain Layer Implementation

**Epic Goal:** Implement all domain entities, use cases, and repository interfaces with 80%+ test coverage

**Priority:** P0 (Must have)  
**Estimated Time:** 3-4 days  
**Dependencies:** EPIC-0 (Project Setup)

---

## Success Criteria

- [ ] All 5 domain entities implemented
- [ ] All 6 use cases implemented with business logic
- [ ] All 3 repository interfaces defined
- [ ] Result and DomainError types implemented
- [ ] Validation logic for all entities
- [ ] 80%+ unit test coverage
- [ ] Zero Android or Firebase dependencies in domain module

---

## Stories

- [DOMAIN-1] Implement Domain Entities
- [DOMAIN-2] Implement Result and Error Types
- [DOMAIN-3] Implement Repository Interfaces
- [DOMAIN-4] Implement Sleep Calculation Use Case
- [DOMAIN-5] Implement Sleep Logging Use Cases
- [DOMAIN-6] Implement Discovery Phase Use Cases
- [DOMAIN-7] Implement System Alarm Use Case
- [DOMAIN-8] Write Domain Layer Unit Tests

---

## Technical Notes

**No Android Dependencies:**
- Only `kotlin-stdlib` and `kotlinx-coroutines-core` allowed
- No `androidx.*` or `android.*` imports
- Tests must use pure Kotlin (no AndroidJUnit4)

**Entities:**
1. UserProfile
2. SleepLog
3. BedtimeRecommendation
4. DiscoveryPhase
5. SystemAlarm

**Use Cases:**
1. CalculateOptimalBedtimeUseCase
2. LogSleepSessionUseCase
3. GetSleepHistoryUseCase
4. StartDiscoveryPhaseUseCase
5. AnalyzeDiscoveryPhaseUseCase
6. FetchSystemAlarmsUseCase

---

## Related Documentation

- `DOMAIN_LAYER_SPEC.md` - Complete specification
- `ARCHITECTURE_GUARDRAILS.md` - Domain purity rules
- `TESTING_STRATEGY.md` - Unit test examples

---

# DOMAIN-4: Implement Sleep Calculation Use Case

**Story:** As a developer, I need a use case to calculate optimal bedtimes based on wake-up time.

**Epic:** EPIC-1 Domain Layer Implementation  
**Priority:** P0  
**Estimated Time:** 3 hours  
**Type:** Task  
**Dependencies:** DOMAIN-1 (Entities), DOMAIN-3 (Repository Interfaces)

---

## Acceptance Criteria

- [ ] `CalculateOptimalBedtimeUseCase.kt` created
- [ ] Algorithm implements 90-minute cycle calculation
- [ ] Returns 3 bedtime options (6, 5, 4 cycles)
- [ ] Marks optimal bedtime (highest cycle count)
- [ ] Marks passed bedtimes (before current time)
- [ ] Handles midnight crossing correctly
- [ ] Uses user's custom cycle duration and sleep latency
- [ ] Returns Result type for error handling
- [ ] 80%+ unit test coverage

---

## Implementation

```kotlin
package com.sleepoptimizer.domain.usecases

import com.sleepoptimizer.domain.entities.BedtimeRecommendation
import com.sleepoptimizer.domain.entities.UserProfile
import com.sleepoptimizer.domain.repositories.UserPreferencesRepository
import com.sleepoptimizer.domain.common.Result
import com.sleepoptimizer.domain.common.DomainError
import java.time.LocalTime
import java.time.LocalDate
import javax.inject.Inject

class CalculateOptimalBedtimeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        wakeUpTime: LocalTime,
        currentDate: LocalDate = LocalDate.now()
    ): Result<List<BedtimeRecommendation>> {
        return try {
            // 1. Get user profile with preferences
            val profile = userPreferencesRepository.getUserProfile()
            
            // 2. Calculate bedtimes for 6, 5, and 4 cycles
            val recommendations = listOf(6, 5, 4).map { cycleCount ->
                calculateBedtime(
                    wakeTime = wakeUpTime,
                    cycleCount = cycleCount,
                    cycleDuration = profile.optimalCycleMinutes,
                    sleepLatency = profile.sleepLatencyMinutes
                )
            }
            
            // 3. Mark optimal (6 cycles) and passed bedtimes
            val now = LocalTime.now()
            val finalRecommendations = recommendations.map { rec ->
                rec.copy(
                    isOptimal = rec.cycleCount == 6,
                    isPassed = rec.bedtime.isBefore(now)
                )
            }
            
            Result.Success(finalRecommendations)
        } catch (e: Exception) {
            Result.Error(DomainError.CalculationFailed(e.message))
        }
    }
    
    private fun calculateBedtime(
        wakeTime: LocalTime,
        cycleCount: Int,
        cycleDuration: Int,
        sleepLatency: Int
    ): BedtimeRecommendation {
        val totalSleepMinutes = (cycleCount * cycleDuration) + sleepLatency
        val bedtime = wakeTime.minusMinutes(totalSleepMinutes.toLong())
        
        return BedtimeRecommendation(
            bedtime = bedtime,
            cycleCount = cycleCount,
            cycleDurationMinutes = cycleDuration,
            sleepLatencyMinutes = sleepLatency,
            isOptimal = false, // Will be set later
            isPassed = false, // Will be set later
            totalSleepMinutes = totalSleepMinutes
        )
    }
}
```

---

## Algorithm Explanation

**Formula:** `Bedtime = WakeTime - (CycleCount × CycleDuration) - SleepLatency`

**Example:**
- Wake time: 07:00
- Cycle duration: 90 minutes
- Sleep latency: 15 minutes
- Cycle count: 6

**Calculation:**
```
Total sleep = (6 × 90) + 15 = 555 minutes
Bedtime = 07:00 - 555 minutes = 21:45
```

**Midnight Crossing:**
If bedtime calculation results in negative time, LocalTime handles wrapping automatically:
```
07:00 - 10 hours = 21:00 (previous day)
01:00 - 10 hours = 15:00 (previous day)
```

---

## Unit Tests

```kotlin
class CalculateOptimalBedtimeUseCaseTest {
    
    private lateinit var useCase: CalculateOptimalBedtimeUseCase
    private val fakePreferencesRepo = FakeUserPreferencesRepository()
    
    @Before
    fun setup() {
        fakePreferencesRepo.setProfile(
            UserProfile(
                userId = "test",
                optimalCycleMinutes = 90,
                sleepLatencyMinutes = 15
            )
        )
        useCase = CalculateOptimalBedtimeUseCase(fakePreferencesRepo)
    }
    
    @Test
    fun `returns 3 bedtime options`() = runTest {
        val result = useCase(LocalTime.of(7, 0))
        
        assertTrue(result is Result.Success)
        val recommendations = (result as Result.Success).data
        assertEquals(3, recommendations.size)
    }
    
    @Test
    fun `calculates correct bedtimes for 7am wake time`() = runTest {
        val result = useCase(LocalTime.of(7, 0))
        val recommendations = (result as Result.Success).data
        
        assertEquals(LocalTime.of(21, 45), recommendations[0].bedtime) // 6 cycles
        assertEquals(LocalTime.of(23, 15), recommendations[1].bedtime) // 5 cycles
        assertEquals(LocalTime.of(0, 45), recommendations[2].bedtime)  // 4 cycles
    }
    
    @Test
    fun `marks 6-cycle bedtime as optimal`() = runTest {
        val result = useCase(LocalTime.of(7, 0))
        val recommendations = (result as Result.Success).data
        
        assertTrue(recommendations[0].isOptimal)
        assertFalse(recommendations[1].isOptimal)
        assertFalse(recommendations[2].isOptimal)
    }
    
    @Test
    fun `handles midnight crossing correctly`() = runTest {
        val result = useCase(LocalTime.of(1, 0))
        val recommendations = (result as Result.Success).data
        
        // 01:00 - (6 × 90 + 15) = 15:45 (previous day)
        assertEquals(LocalTime.of(15, 45), recommendations[0].bedtime)
    }
    
    @Test
    fun `uses custom cycle duration from user profile`() = runTest {
        fakePreferencesRepo.setProfile(
            UserProfile(userId = "test", optimalCycleMinutes = 105, sleepLatencyMinutes = 15)
        )
        
        val result = useCase(LocalTime.of(7, 0))
        val recommendations = (result as Result.Success).data
        
        // 07:00 - (6 × 105 + 15) = 19:15
        assertEquals(LocalTime.of(19, 15), recommendations[0].bedtime)
        assertEquals(105, recommendations[0].cycleDurationMinutes)
    }
}
```

---

## Definition of Done

- [ ] Use case compiles without errors
- [ ] All unit tests pass
- [ ] 80%+ test coverage achieved
- [ ] No Android dependencies detected
- [ ] Algorithm verified with manual calculations
- [ ] Edge cases tested (midnight, custom durations)

---

## References

- `DOMAIN_LAYER_SPEC.md` - Complete algorithm specification
- `PROJECT_OVERVIEW.md` - Sleep calculation feature

---

# EPIC 2: UI Design Implementation

**Epic Goal:** Implement complete UI design system and all screens according to UI specifications

**Priority:** P0 (Must have)  
**Estimated Time:** 5-6 days  
**Dependencies:** EPIC-0 (Project Setup)

---

## Success Criteria

- [ ] Design system implemented (colors, typography, spacing, shapes)
- [ ] Component library created (buttons, cards, switches)
- [ ] All 4 main screens implemented
- [ ] Navigation graph configured
- [ ] Accessibility guidelines followed (touch targets, contrast)
- [ ] Responsive design for different screen sizes
- [ ] Dark mode optimized (Deep Blue theme)

---

## Stories

### Design Foundation
- [UI-1] Implement Design System (Colors, Typography, Spacing)
- [UI-2] Create Component Library
- [UI-3] Setup Navigation Graph

### Screens
- [UI-4] Implement Home Screen (Sleep Calculator)
- [UI-5] Implement History Screen
- [UI-6] Implement Settings Screen
- [UI-7] Implement Onboarding Flow (3 screens)

### Polish
- [UI-8] Implement Animations and Transitions
- [UI-9] Accessibility Improvements
- [UI-10] Responsive Layout Handling

---

## Design Assets

All screens follow **Deep Blue** design language:
- **Primary:** Navy Blue (#0B1120), Cyan Blue (#00D9FF)
- **Font:** Urbanist
- **Dark Mode:** Default theme

---

## Related Documentation

- `UI_SPECIFICATIONS.md` - Complete screen designs
- `ARCHITECTURE_GUARDRAILS.md` - Compose best practices

---

# UI-1: Implement Design System (Colors, Typography, Spacing)

**Story:** As a developer, I need a centralized design system so that all UI components have consistent styling.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P0  
**Estimated Time:** 3 hours  
**Type:** Task  
**Dependencies:** SETUP-1

---

## Acceptance Criteria

- [ ] `SleepColors` object created with all color definitions
- [ ] `SleepTypography` object created with all text styles
- [ ] `SleepSpacing` object created with spacing values
- [ ] `SleepShapes` object created with border radius values
- [ ] Material3 theme configured with custom colors
- [ ] Theme applied to app in `MainActivity`
- [ ] Preview functions created to visualize theme

---

## Implementation Steps

1. Create `presentation/theme/Color.kt`:
   ```kotlin
   package com.sleepoptimizer.presentation.theme
   
   import androidx.compose.ui.graphics.Color
   
   object SleepColors {
       val NavyBlue = Color(0xFF0B1120)        // Main background
       val CyanBlue = Color(0xFF00D9FF)        // Accent/CTAs
       val MidnightBlue = Color(0xFF1A2332)    // Card backgrounds
       val SteelBlue = Color(0xFF4A5568)       // Secondary text
       val White = Color(0xFFFFFFFF)           // Primary text
       val Gray300 = Color(0xFFD1D5DB)         // Disabled states
       val ErrorRed = Color(0xFFEF4444)        // Warnings
       val SuccessGreen = Color(0xFF10B981)    // Confirmations
   }
   ```

2. Create `presentation/theme/Type.kt`:
   ```kotlin
   package com.sleepoptimizer.presentation.theme
   
   import androidx.compose.material3.Typography
   import androidx.compose.ui.text.TextStyle
   import androidx.compose.ui.text.font.Font
   import androidx.compose.ui.text.font.FontFamily
   import androidx.compose.ui.text.font.FontWeight
   import androidx.compose.ui.unit.sp
   
   val Urbanist = FontFamily(
       Font(R.font.urbanist_regular, FontWeight.Normal),
       Font(R.font.urbanist_medium, FontWeight.Medium),
       Font(R.font.urbanist_semibold, FontWeight.SemiBold),
       Font(R.font.urbanist_bold, FontWeight.Bold)
   )
   
   val SleepTypography = Typography(
       displayLarge = TextStyle(
           fontFamily = Urbanist,
           fontWeight = FontWeight.Bold,
           fontSize = 96.sp,
           letterSpacing = (-1.5).sp
       ),
       headlineLarge = TextStyle(
           fontFamily = Urbanist,
           fontWeight = FontWeight.SemiBold,
           fontSize = 32.sp,
           letterSpacing = 0.sp
       ),
       bodyLarge = TextStyle(
           fontFamily = Urbanist,
           fontWeight = FontWeight.Normal,
           fontSize = 16.sp,
           lineHeight = 24.sp
       ),
       labelMedium = TextStyle(
           fontFamily = Urbanist,
           fontWeight = FontWeight.Medium,
           fontSize = 14.sp,
           letterSpacing = 0.5.sp
       )
   )
   ```

3. Create `presentation/theme/Spacing.kt`:
   ```kotlin
   package com.sleepoptimizer.presentation.theme
   
   import androidx.compose.ui.unit.dp
   
   object SleepSpacing {
       val XXS = 4.dp
       val XS = 8.dp
       val Small = 12.dp
       val Medium = 16.dp
       val Large = 24.dp
       val XL = 32.dp
       val XXL = 48.dp
   }
   ```

4. Create `presentation/theme/Shape.kt`:
   ```kotlin
   package com.sleepoptimizer.presentation.theme
   
   import androidx.compose.foundation.shape.CircleShape
   import androidx.compose.foundation.shape.RoundedCornerShape
   import androidx.compose.ui.unit.dp
   
   object SleepShapes {
       val Small = RoundedCornerShape(8.dp)
       val Medium = RoundedCornerShape(12.dp)
       val Large = RoundedCornerShape(16.dp)
       val ExtraLarge = RoundedCornerShape(24.dp)
       val Circle = CircleShape
   }
   ```

5. Create `presentation/theme/Theme.kt`:
   ```kotlin
   package com.sleepoptimizer.presentation.theme
   
   import androidx.compose.material3.MaterialTheme
   import androidx.compose.material3.darkColorScheme
   import androidx.compose.runtime.Composable
   
   private val DarkColorScheme = darkColorScheme(
       primary = SleepColors.CyanBlue,
       onPrimary = SleepColors.NavyBlue,
       background = SleepColors.NavyBlue,
       onBackground = SleepColors.White,
       surface = SleepColors.MidnightBlue,
       onSurface = SleepColors.White,
       error = SleepColors.ErrorRed
   )
   
   @Composable
   fun SleepOptimizerTheme(content: @Composable () -> Unit) {
       MaterialTheme(
           colorScheme = DarkColorScheme,
           typography = SleepTypography,
           content = content
       )
   }
   ```

6. Download Urbanist font from Google Fonts
   - Place in `presentation/res/font/` directory
   - Files needed: `urbanist_regular.ttf`, `urbanist_medium.ttf`, `urbanist_semibold.ttf`, `urbanist_bold.ttf`

7. Apply theme in `MainActivity`:
   ```kotlin
   class MainActivity : ComponentActivity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           setContent {
               SleepOptimizerTheme {
                   // App content
               }
           }
       }
   }
   ```

---

## Definition of Done

- [ ] All theme files compile without errors
- [ ] Theme applied globally in app
- [ ] Urbanist font loads correctly
- [ ] Preview functions show correct colors and typography
- [ ] No hardcoded colors/sizes in any composables (all use theme)

---

## Design Reference

See `UI_SPECIFICATIONS.md` - Design System section for exact values.

---

## Testing

Create preview functions to verify theme:
```kotlin
@Preview
@Composable
fun ThemePreview() {
    SleepOptimizerTheme {
        Column {
            Text("Display Large", style = MaterialTheme.typography.displayLarge)
            Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
            Text("Body Large", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
```

---

# UI-2: Create Component Library

**Story:** As a developer, I need reusable UI components so that the app has a consistent look and feel.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P0  
**Estimated Time:** 4 hours  
**Type:** Task  
**Dependencies:** UI-1

---

## Acceptance Criteria

- [ ] SleepPrimaryButton component created
- [ ] SleepSecondaryButton component created
- [ ] SleepInfoCard component created
- [ ] SwitchWithLabel component created
- [ ] BedtimeRecommendationCard component created
- [ ] TimeSpinner component created
- [ ] All components follow design system
- [ ] Preview functions created for each component
- [ ] Accessibility content descriptions added

---

## Components to Implement

### 1. SleepPrimaryButton
```kotlin
@Composable
fun SleepPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```
**Specs:**
- Height: 56dp
- Color: CyanBlue background, NavyBlue text
- Shape: Medium (12dp radius)
- Full width by default

### 2. SleepSecondaryButton
```kotlin
@Composable
fun SleepSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```
**Specs:**
- Outlined button
- Border: 1dp CyanBlue
- Content color: CyanBlue

### 3. SleepInfoCard
```kotlin
@Composable
fun SleepInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
)
```
**Specs:**
- Background: MidnightBlue
- Icon size: 48dp
- Padding: Large (24dp)
- Shape: Large (16dp radius)

### 4. SwitchWithLabel
```kotlin
@Composable
fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
)
```
**Specs:**
- Checked thumb: CyanBlue
- Checked track: CyanBlue @ 50% opacity
- Label + switch in Row

### 5. BedtimeRecommendationCard
```kotlin
@Composable
fun BedtimeRecommendationCard(
    bedtime: LocalTime,
    cycleCount: Int,
    isOptimal: Boolean,
    isPassed: Boolean,
    onClick: () -> Unit
)
```
**Specs:**
- Background: CyanBlue @ 15% if optimal, MidnightBlue otherwise
- Icons: Check (optimal), Alarm (normal), Close (passed)
- Strikethrough text if passed
- Padding: Medium (16dp)

### 6. TimeSpinner
```kotlin
@Composable
fun TimeSpinner(
    selectedTime: LocalTime,
    isActive: Boolean,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
)
```
**Specs:**
- Height: 200dp
- Large time display (96sp)
- Background: CyanBlue @ 10% if active
- Clickable to open time picker

---

## Implementation Location

`presentation/components/`

---

## Definition of Done

- [ ] All components compile without errors
- [ ] Preview functions show correctly in Android Studio
- [ ] Components match UI_SPECIFICATIONS.md exactly
- [ ] Accessibility labels added
- [ ] No hardcoded values (all use theme)

---

## References

- `UI_SPECIFICATIONS.md` - Component Library section

---

# UI-4: Implement Home Screen (Sleep Calculator)

**Story:** As a user, I want to see bedtime recommendations based on my wake-up time.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P0  
**Estimated Time:** 6 hours  
**Type:** Feature  
**Dependencies:** UI-1, UI-2, UI-3

---

## Acceptance Criteria

- [ ] TimeSpinner displayed at top with selected wake time
- [ ] Two switches (Aktiverad, Daglig) below time spinner
- [ ] ScrollView with bedtime recommendations visible after 18:00
- [ ] Each recommendation shows time, cycle count, and icon
- [ ] Optimal bedtime highlighted with green checkmark
- [ ] Passed bedtimes shown with strikethrough
- [ ] FAB for settings navigation
- [ ] Empty state shown before 18:00
- [ ] Loading state while calculating

---

## Screen Layout

```
TopAppBar: "Sleep Cycle Optimizer"
├─ TimeSpinner (07:00)
├─ Row [Switch: Aktiverad] [Switch: Daglig]
├─ Spacer
└─ LazyColumn (Bedtime Recommendations)
   ├─ BedtimeRecommendationCard (21:45, 6 cycles, optimal)
   ├─ BedtimeRecommendationCard (23:15, 5 cycles)
   └─ BedtimeRecommendationCard (00:45, 4 cycles)

FloatingActionButton (Settings icon)
```

---

## Implementation Steps

1. Create `SleepCalculatorScreen.kt`
2. Create `SleepCalculatorViewModel.kt` (stub - will connect later)
3. Create UI state data class
4. Handle time picker dialog
5. Implement recommendation list
6. Add loading and empty states
7. Add FAB navigation to settings

---

## UI State

```kotlin
data class SleepCalculatorUiState(
    val selectedTime: LocalTime = LocalTime.of(7, 0),
    val isActive: Boolean = false,
    val isDaily: Boolean = false,
    val bedtimeRecommendations: List<BedtimeRecommendation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

---

## Empty State (Before 18:00)

Show message: "Notifikation kommer 18:00 - Planera din sömn då"

---

## Definition of Done

- [ ] Screen matches UI_SPECIFICATIONS.md exactly
- [ ] Time picker opens on TimeSpinner click
- [ ] Recommendations scroll smoothly
- [ ] Switches toggle correctly
- [ ] FAB navigates to settings
- [ ] Screen responsive on different sizes

---

## References

- `UI_SPECIFICATIONS.md` - Home Screen section

---

# UI-5: Implement History Screen

**Story:** As a user, I want to see my sleep history and quality trends over time.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P1  
**Estimated Time:** 5 hours  
**Type:** Feature  
**Dependencies:** UI-1, UI-2

---

## Acceptance Criteria

- [ ] TopAppBar with "Sömnhistorik" title
- [ ] Quality overview card showing percentage and average
- [ ] Sleep quality chart (line chart) showing last 7 days
- [ ] LazyColumn with daily sleep logs
- [ ] Each log card shows date, sleep times, rating (stars)
- [ ] Empty state for no logs
- [ ] Smooth scrolling performance

---

## Screen Layout

```
TopAppBar: "Sömnhistorik"
├─ SleepQualityOverview Card
│  ├─ CircularProgressIndicator (80%)
│  ├─ "Sleep Quality"
│  └─ Stats: Avg duration, Best day
├─ SleepQualityChart (Line chart)
└─ LazyColumn (Daily Logs)
   ├─ SleepLogCard (Torsdag 8 Maj)
   ├─ SleepLogCard (Onsdag 7 Maj)
   └─ ...
```

---

## Components Needed

### SleepQualityOverview
- Circular progress with percentage
- Text: "Sleep Quality"
- Stats row: Average duration | Best day

### SleepQualityChart
- Simple line chart (can use Canvas or library)
- X-axis: Days of week
- Y-axis: Rating 1-5
- Points connected with line

### SleepLogCard
```kotlin
@Composable
fun SleepLogCard(
    date: String,
    bedtime: LocalTime,
    wakeTime: LocalTime,
    rating: Int,
    cycleCount: Int
)
```

---

## Empty State

Show message: "Ingen sömnhistorik än. Börja logga din sömn!"

---

## Definition of Done

- [ ] Screen matches UI_SPECIFICATIONS.md
- [ ] Chart displays correctly for 7 days
- [ ] Logs scroll smoothly (>100 items)
- [ ] Empty state shown when no logs
- [ ] Card taps open detail view (future)

---

## References

- `UI_SPECIFICATIONS.md` - History Screen section

---

# UI-6: Implement Settings Screen

**Story:** As a user, I want to configure my sleep preferences and start Discovery Phase.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P1  
**Estimated Time:** 4 hours  
**Type:** Feature  
**Dependencies:** UI-1, UI-2

---

## Acceptance Criteria

- [ ] TopAppBar with "Inställningar" title
- [ ] Sleep Preferences section with 3 dropdowns
- [ ] Discovery Phase section with switch and info
- [ ] Sync & Backup section with switch
- [ ] About section with version and links
- [ ] All dropdowns functional
- [ ] Info dialogs explain features

---

## Screen Layout

```
TopAppBar: "Inställningar"
├─ Section: SLEEP PREFERENCES
│  ├─ Dropdown: Cykel-längd [90 min]
│  ├─ Dropdown: Insomning [15 min]
│  └─ Dropdown: Påminnelse [18:00]
├─ Section: DISCOVERY PHASE
│  ├─ Switch: Aktivera Discovery Phase
│  ├─ Status: Inaktiv
│  ├─ Info text
│  └─ Button: "Starta Discovery Phase"
├─ Section: SYNC & BACKUP
│  ├─ Switch: Firebase Synk
│  ├─ Text: Senast synkad
│  └─ Button: "Exportera data"
└─ Section: ABOUT
   ├─ Text: Version 1.0.0 (MVP)
   ├─ Link: Privacy Policy
   └─ Link: GitHub Repository
```

---

## Dropdowns

### Cykel-längd
Options: 70, 75, 80, 85, 90, 95, 100, 105, 110 min

### Insomning
Options: 5, 10, 15, 20, 25, 30, 35, 40, 45 min

### Påminnelse
Options: 17:00, 17:30, 18:00, 18:30, 19:00

---

## Discovery Phase Info

```
ℹ️ Discovery Phase justerar automatiskt din 
   sömncykel baserat på feedback.
   
   Duration: 21 dagar
   Requirement: 7+ rated sleep logs
```

---

## Definition of Done

- [ ] Screen matches UI_SPECIFICATIONS.md
- [ ] All dropdowns save preferences
- [ ] Discovery Phase button enables/disables correctly
- [ ] Sync switch works
- [ ] Links open correctly
- [ ] Info dialogs explain features clearly

---

## References

- `UI_SPECIFICATIONS.md` - Settings Screen section

---

# UI-7: Implement Onboarding Flow (3 Screens)

**Story:** As a new user, I want a smooth onboarding experience that explains the app and requests necessary permissions.

**Epic:** EPIC-2 UI Design Implementation  
**Priority:** P0  
**Estimated Time:** 4 hours  
**Type:** Feature  
**Dependencies:** UI-1, UI-2, UI-3

---

## Acceptance Criteria

- [ ] 3 onboarding screens created
- [ ] Navigation between screens with buttons
- [ ] Permission requests triggered at correct times
- [ ] OnboardingViewModel tracks completion
- [ ] Skip onboarding if already completed
- [ ] Navigate to main screen after completion

---

## Screen 1: Welcome

**Layout:**
```
┌─────────────────────────────────────┐
│                                     │
│              🌙                     │
│                                     │
│       Välkommen till                │
│    Sleep Cycle Optimizer            │
│                                     │
│   Optimera din sömn baserat på      │
│    naturliga 90-minuters cykler     │
│                                     │
│                                     │
│         [Kom igång]                 │
│                                     │
└─────────────────────────────────────┘
```

**Button:** "Kom igång" → Navigate to Screen 2

---

## Screen 2: Permissions

**Layout:**
```
┌─────────────────────────────────────┐
│          Behörigheter               │
│                                     │
│  🔔 Notifikationer                  │
│     För dagliga påminnelser kl      │
│     18:00                           │
│     [Tillåt]                        │
│                                     │
│  ⏰ Alarm-läsning                    │
│     För att föreslå sömntider       │
│     baserat på dina larm            │
│     [Tillåt] [Hoppa över]           │
│                                     │
│                                     │
│         [Fortsätt]                  │
│                                     │
└─────────────────────────────────────┘
```

**Permissions:**
1. POST_NOTIFICATIONS (required)
2. READ_ALARM (optional - can skip)

**Buttons:**
- "Tillåt" → Request permission
- "Hoppa över" (for alarm) → Continue without
- "Fortsätt" → Navigate to Screen 3

---

## Screen 3: Set First Wake Time

**Layout:**
```
┌─────────────────────────────────────┐
│    När vaknar du vanligtvis?        │
│                                     │
│         ┌───────────┐               │
│         │   07:00   │               │
│         └───────────┘               │
│                                     │
│   Vi använder detta för att         │
│   beräkna optimala sänggångstider   │
│   åt dig                            │
│                                     │
│                                     │
│         [Fortsätt]                  │
│                                     │
└─────────────────────────────────────┘
```

**Button:** "Fortsätt" → Save wake time, mark onboarding complete, navigate to main screen

---

## Implementation Steps

1. Create `OnboardingScreen.kt` with 3 sub-screens:
   - `WelcomeScreen()`
   - `PermissionsScreen()`
   - `SetWakeTimeScreen()`

2. Create `OnboardingViewModel.kt`:
   ```kotlin
   data class OnboardingUiState(
       val currentScreen: Int = 0,
       val notificationPermissionGranted: Boolean = false,
       val alarmPermissionGranted: Boolean = false,
       val selectedWakeTime: LocalTime = LocalTime.of(7, 0)
   )
   ```

3. Add navigation logic:
   ```kotlin
   fun nextScreen() {
       if (currentScreen < 2) {
           currentScreen++
       } else {
           markOnboardingComplete()
           navigateToMain()
       }
   }
   ```

4. Request permissions using `rememberPermissionState`

5. Save onboarding completion to DataStore

6. Add check in MainActivity to skip onboarding if completed

---

## Permission Handling

```kotlin
// Request notification permission
val notificationPermissionState = rememberPermissionState(
    permission = Manifest.permission.POST_NOTIFICATIONS
)

LaunchedEffect(Unit) {
    if (!notificationPermissionState.status.isGranted) {
        notificationPermissionState.launchPermissionRequest()
    }
}

// Request alarm permission (optional)
val alarmPermissionState = rememberPermissionState(
    permission = Manifest.permission.READ_ALARM
)
```

---

## Definition of Done

- [ ] All 3 screens render correctly
- [ ] Navigation works smoothly
- [ ] Permissions requested at correct times
- [ ] Wake time saved to preferences
- [ ] Onboarding skipped on second launch
- [ ] Matches UI_SPECIFICATIONS.md

---

## References

- `UI_SPECIFICATIONS.md` - Onboarding Flow section

---

# EPIC 3: Data Layer Implementation

**Epic Goal:** Implement offline-first data persistence with Room and DataStore

**Priority:** P0 (Must have)  
**Estimated Time:** 4-5 days  
**Dependencies:** EPIC-1 (Domain Layer)

---

## Success Criteria

- [ ] Room database configured with all entities
- [ ] All DAOs implemented with queries
- [ ] DataStore preferences configured
- [ ] Repository implementations with offline-first strategy
- [ ] Entity mappers (Domain ↔ Room)
- [ ] Migration strategy defined
- [ ] 70%+ integration test coverage
- [ ] In-memory database tests passing

---

## Stories

- [DATA-1] Setup Room Database
- [DATA-2] Implement SleepLogDao
- [DATA-3] Implement UserProfileDao
- [DATA-4] Setup DataStore Preferences
- [DATA-5] Implement Entity Mappers
- [DATA-6] Implement SleepRepository
- [DATA-7] Implement UserPreferencesRepository
- [DATA-8] Write Integration Tests

---

## Technical Notes

**Room Version:** 2.6.0  
**DataStore Version:** 1.0.0

**Key Entities:**
- SleepLogEntity (with indices on wake_time, rating, sync_status)
- UserProfileEntity (single row, discovery phase as JSON)

**Testing:**
- Use in-memory database for tests
- Test all DAO queries
- Test repository offline-first behavior

---

## Related Documentation

- `DATA_LAYER_SPEC.md` - Complete specification
- `ARCHITECTURE_GUARDRAILS.md` - Offline-first rules
- `TESTING_STRATEGY.md` - Integration test examples

---

# EPIC 4: Notification System

**Epic Goal:** Implement all three notification types with precise scheduling

**Priority:** P0 (Must have)  
**Estimated Time:** 3-4 days  
**Dependencies:** EPIC-3 (Data Layer)

---

## Success Criteria

- [ ] Three notification channels configured
- [ ] Daily check-in notification (18:00) working
- [ ] Bedtime reminder notification (dynamic) working
- [ ] Morning feedback notification (wake + 15min) working
- [ ] BroadcastReceivers handling all notifications
- [ ] BootReceiver rescheduling after restart
- [ ] Alarm integration detecting system alarms
- [ ] Interactive notification actions working
- [ ] Notification permissions requested

---

## Stories

- [NOTIF-1] Setup Notification Channels
- [NOTIF-2] Implement Daily Check-in (18:00)
- [NOTIF-3] Implement Bedtime Reminder (Dynamic)
- [NOTIF-4] Implement Morning Feedback
- [NOTIF-5] Implement Alarm Integration
- [NOTIF-6] Implement BootReceiver
- [NOTIF-7] Request Notification Permissions
- [NOTIF-8] Test Notification Timing

---

## Technical Notes

**Permissions Needed:**
- POST_NOTIFICATIONS (Android 13+)
- SCHEDULE_EXACT_ALARM (Android 12+)
- READ_ALARM (Android 12+)

**Scheduling:**
- Use AlarmManager.setExactAndAllowWhileIdle()
- Survives Doze mode
- Precise timing even in background

**Channels:**
1. Daily Check-in (Default priority, soft chime)
2. Bedtime Reminder (High priority, gentle bell)
3. Morning Feedback (Low priority, silent)

---

## Related Documentation

- `NOTIFICATION_STRATEGY.md` - Complete flow
- `ARCHITECTURE_GUARDRAILS.md` - Error handling

---

# EPIC 5: Firebase Synchronization

**Epic Goal:** Implement background cloud sync with conflict resolution

**Priority:** P1 (Should have)  
**Estimated Time:** 3-4 days  
**Dependencies:** EPIC-3 (Data Layer)

---

## Success Criteria

- [ ] Firebase Authentication setup
- [ ] Firestore schema implemented
- [ ] WorkManager sync worker created
- [ ] Last-write-wins conflict resolution
- [ ] Background sync every 6 hours
- [ ] Manual sync trigger
- [ ] Network error handling
- [ ] Offline-first verified (local writes never blocked)

---

## Stories

- [FIREBASE-1] Setup Firebase Project
- [FIREBASE-2] Implement Firebase Authentication
- [FIREBASE-3] Create Firestore Schema
- [FIREBASE-4] Implement SleepLog Sync
- [FIREBASE-5] Implement UserProfile Sync
- [FIREBASE-6] Create WorkManager Sync Worker
- [FIREBASE-7] Implement Conflict Resolution
- [FIREBASE-8] Add Security Rules

---

## Technical Notes

**Firestore Structure:**
```
users/{userId}/
├─ profile (document)
└─ sleep_logs/ (collection)
   ├─ {logId} (document)
   └─ ...
```

**Sync Strategy:**
- Write to Room first (always succeeds)
- Queue WorkManager background sync
- Never block on Firebase operations

**Conflict Resolution:**
- Compare `updated_at` timestamps
- Last write wins
- Log conflicts for debugging

---

## Related Documentation

- `FIREBASE_SCHEMA.md` - Complete sync strategy
- `ARCHITECTURE_GUARDRAILS.md` - Offline-first rules

---

# EPIC 6: Discovery Phase

**Epic Goal:** Implement adaptive learning feature to personalize sleep parameters

**Priority:** P2 (Nice to have)  
**Estimated Time:** 3-4 days  
**Dependencies:** EPIC-4 (Notifications), EPIC-3 (Data Layer)

---

## Success Criteria

- [ ] Discovery Phase can be started from settings
- [ ] 21-day phase with 3 weekly shifts implemented
- [ ] Morning feedback ratings collected
- [ ] Analysis algorithm calculates best parameters
- [ ] Recommendations applied to user profile
- [ ] UI shows phase progress
- [ ] Can be canceled mid-phase

---

## Stories

- [DISCOVERY-1] Implement Start Discovery Phase Use Case
- [DISCOVERY-2] Implement Analysis Use Case
- [DISCOVERY-3] Add Discovery Phase UI in Settings
- [DISCOVERY-4] Implement Weekly Shift Rotation
- [DISCOVERY-5] Create Analysis Results Screen
- [DISCOVERY-6] Add Progress Tracking

---

## Technical Notes

**Phase Structure:**
- Week 1 (Days 1-7): Test LongerLatency (30min)
- Week 2 (Days 8-14): Test LongerCycles (105min)
- Week 3 (Days 15-21): Test FewerCycles (5 cycles)

**Analysis:**
- Compare average ratings per shift
- Select shift with highest average rating
- Apply winning parameters to profile

**Requirements:**
- User must have 7+ rated logs before starting
- Cannot start if already active
- All shifts must complete for valid analysis

---

## Related Documentation

- `DOMAIN_LAYER_SPEC.md` - DiscoveryPhase entity
- `PROJECT_OVERVIEW.md` - Discovery Phase feature

---

# EPIC 7: ViewModels & Integration

**Epic Goal:** Connect UI to domain/data layers via ViewModels

**Priority:** P0 (Must have)  
**Estimated Time:** 3-4 days  
**Dependencies:** EPIC-1 (Domain), EPIC-2 (UI), EPIC-3 (Data)

---

## Success Criteria

- [ ] All ViewModels implemented with Hilt injection
- [ ] UI state flows working correctly
- [ ] UseCases called from ViewModels
- [ ] Error handling with user-friendly messages
- [ ] Loading states shown during operations
- [ ] Navigation working between all screens
- [ ] End-to-end user flows functional

---

## Stories

- [VM-1] Implement SleepCalculatorViewModel
- [VM-2] Implement HistoryViewModel
- [VM-3] Implement SettingsViewModel
- [VM-4] Implement OnboardingViewModel
- [VM-5] Connect ViewModels to UI
- [VM-6] Implement Navigation Graph
- [VM-7] Add Error Handling
- [VM-8] Test Complete User Flows

---

## ViewModels to Implement

### SleepCalculatorViewModel
- Manages wake time selection
- Calls CalculateOptimalBedtimeUseCase
- Handles bedtime selection
- Schedules notifications

### HistoryViewModel
- Fetches sleep logs via GetSleepHistoryUseCase
- Calculates statistics
- Handles date range filtering

### SettingsViewModel
- Manages user preferences
- Starts/stops Discovery Phase
- Handles Firebase sync toggle
- Exports data

### OnboardingViewModel
- Manages onboarding flow
- Requests permissions
- Saves initial preferences

---

## Related Documentation

- `ARCHITECTURE_GUARDRAILS.md` - ViewModel rules
- `TESTING_STRATEGY.md` - ViewModel testing

---

# EPIC 8: Comprehensive Testing

**Epic Goal:** Achieve coverage targets and ensure production quality

**Priority:** P0 (Must have)  
**Estimated Time:** 4-5 days  
**Dependencies:** All other EPICs

---

## Success Criteria

- [ ] Domain layer: 80%+ unit test coverage
- [ ] Data layer: 70%+ integration test coverage
- [ ] Presentation: 60%+ ViewModel test coverage
- [ ] UI: 40%+ critical path E2E tests
- [ ] All tests passing in CI
- [ ] No flaky tests
- [ ] Test documentation complete

---

## Stories

- [TEST-1] Write Domain Layer Unit Tests
- [TEST-2] Write Data Layer Integration Tests
- [TEST-3] Write ViewModel Tests
- [TEST-4] Write E2E UI Tests
- [TEST-5] Setup CI Pipeline (GitHub Actions)
- [TEST-6] Configure Coverage Reporting
- [TEST-7] Fix Flaky Tests
- [TEST-8] Document Test Strategy

---

## Coverage Targets

| Layer | Current | Target | Status |
|-------|---------|--------|--------|
| Domain | 0% | 80% | ❌ |
| Data | 0% | 70% | ❌ |
| Presentation | 0% | 60% | ❌ |
| UI | 0% | 40% | ❌ |

---

## Critical Test Cases

### Domain Tests
- Sleep calculation with midnight crossing
- Discovery Phase with insufficient data
- Validation edge cases

### Data Tests
- DAO queries with in-memory database
- Repository offline-first behavior
- Migration tests

### ViewModel Tests
- UI state updates
- Error handling
- Loading states

### E2E Tests
- Onboarding flow
- Calculate bedtime → schedule reminder
- Rate sleep → update history

---

## Related Documentation

- `TESTING_STRATEGY.md` - Complete testing guide
- `ARCHITECTURE_GUARDRAILS.md` - Coverage requirements

---

# EPIC 9: Polish & MVP Release

**Epic Goal:** Final polish, bug fixes, and prepare for MVP release

**Priority:** P0 (Must have)  
**Estimated Time:** 3-4 days  
**Dependencies:** All other EPICs

---

## Success Criteria

- [ ] All critical bugs fixed
- [ ] Performance optimized (60 FPS maintained)
- [ ] Animations smooth and polished
- [ ] Error messages user-friendly
- [ ] Loading states appropriate
- [ ] Edge cases handled
- [ ] App icon and splash screen created
- [ ] README updated with screenshots
- [ ] Demo video recorded

---

## Stories

- [POLISH-1] Fix Critical Bugs
- [POLISH-2] Optimize Performance
- [POLISH-3] Polish Animations
- [POLISH-4] Improve Error Messages
- [POLISH-5] Handle Edge Cases
- [POLISH-6] Create App Icon
- [POLISH-7] Update Documentation
- [POLISH-8] Record Demo Video

---

## Performance Targets

- Main screen: 60 FPS, no dropped frames
- History scroll: 60 FPS with 100+ items
- Settings: <16ms UI updates
- Database queries: <50ms for 30-day range

---

## Edge Cases to Handle

- Midnight crossing in calculations
- Wake time before bedtime
- Discovery Phase with timezone changes
- Notification scheduling during DST
- Device restart mid-phase
- Low storage space
- No internet connection

---

## Documentation Checklist

- [ ] README.md updated with:
  - Project description
  - Screenshots of all screens
  - Architecture diagram
  - Setup instructions
  - Testing instructions
- [ ] LICENSE file added
- [ ] CONTRIBUTING.md created
- [ ] Code comments added

---

## Demo Video Contents

1. Onboarding flow (30s)
2. Calculate bedtime (15s)
3. Notification workflow (30s)
4. History screen (15s)
5. Settings & Discovery Phase (30s)

Total: ~2 minutes

---

## Related Documentation

- `PROJECT_OVERVIEW.md` - Portfolio highlights
- `ARCHITECTURE_GUARDRAILS.md` - Performance budgets

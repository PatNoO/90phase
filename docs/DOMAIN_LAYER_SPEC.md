# Domain Layer Specification

> Complete specification of all entities, use cases, and business logic for the Sleep Cycle Optimizer domain layer

---

## Table of Contents
1. [Overview](#overview)
2. [Entities](#entities)
3. [Use Cases](#use-cases)
4. [Repository Interfaces](#repository-interfaces)
5. [Common Types](#common-types)
6. [Business Logic Rules](#business-logic-rules)
7. [Validation Rules](#validation-rules)

---

## Overview

### Domain Layer Principles

**CRITICAL:** The domain layer must remain **pure Kotlin** with NO Android or Firebase dependencies.

**Allowed dependencies:**
```gradle
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core) // NOT coroutines-android
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

**Package Structure:**
```
domain/
├── entities/
│   ├── UserProfile.kt
│   ├── SleepLog.kt
│   ├── BedtimeRecommendation.kt
│   ├── DiscoveryPhase.kt
│   └── SystemAlarm.kt
├── usecases/
│   ├── CalculateOptimalBedtimeUseCase.kt
│   ├── LogSleepSessionUseCase.kt
│   ├── GetSleepHistoryUseCase.kt
│   ├── StartDiscoveryPhaseUseCase.kt
│   ├── AnalyzeDiscoveryPhaseUseCase.kt
│   └── FetchSystemAlarmsUseCase.kt
├── repositories/
│   ├── SleepRepository.kt
│   ├── UserPreferencesRepository.kt
│   └── AlarmRepository.kt
└── common/
    ├── Result.kt
    └── DomainError.kt
```

---

## Entities

### 1. UserProfile

**Purpose:** Represents user's sleep preferences and Discovery Phase state.

```kotlin
package com.sleepoptimizer.domain.entities

import java.time.LocalDate

data class UserProfile(
    val userId: String,
    val email: String? = null,
    val displayName: String? = null,
    val optimalCycleMinutes: Int = 90,
    val sleepLatencyMinutes: Int = 15,
    val reminderTime: String = "18:00", // HH:mm format
    val notificationsEnabled: Boolean = true,
    val discoveryPhase: DiscoveryPhase? = null
) {
    companion object {
        const val MIN_CYCLE_DURATION = 70
        const val MAX_CYCLE_DURATION = 110
        const val MIN_SLEEP_LATENCY = 5
        const val MAX_SLEEP_LATENCY = 45
    }
    
    fun isDiscoveryPhaseActive(): Boolean {
        return discoveryPhase?.isActive == true
    }
}
```

**Field Constraints:**
- `optimalCycleMinutes`: 70-110 (default: 90)
- `sleepLatencyMinutes`: 5-45 (default: 15)
- `reminderTime`: Valid 24-hour time string (HH:mm)
- `userId`: Non-empty string (Firebase UID or local UUID)

---

### 2. SleepLog

**Purpose:** Records a single sleep session with optional feedback.

```kotlin
package com.sleepoptimizer.domain.entities

import java.time.Instant

data class SleepLog(
    val id: String,
    val targetWakeTime: Instant,
    val recommendedBedtime: Instant? = null,
    val actualBedtime: Instant? = null,
    val actualWakeTime: Instant? = null,
    val wakeUpRating: Int? = null, // 1-5 stars
    val cycleCount: Int,
    val cycleDurationUsed: Int,
    val sleepLatencyUsed: Int,
    val notes: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
) {
    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val MIN_CYCLE_COUNT = 3
        const val MAX_CYCLE_COUNT = 8
    }
    
    fun hasBeenRated(): Boolean = wakeUpRating != null
    
    fun getTotalSleepDuration(): Long? {
        return if (actualBedtime != null && actualWakeTime != null) {
            java.time.Duration.between(actualBedtime, actualWakeTime).toMinutes()
        } else null
    }
}

enum class SyncStatus {
    SYNCED,           // Local == Remote
    PENDING_UPLOAD,   // Needs to be sent to Firebase
    PENDING_DOWNLOAD, // Needs to be fetched from Firebase
    CONFLICT          // Both modified since last sync
}
```

**Field Constraints:**
- `wakeUpRating`: null OR 1-5
- `cycleCount`: 3-8 (typical: 4-6)
- `cycleDurationUsed`: 70-110 minutes
- `sleepLatencyUsed`: 5-45 minutes

---

### 3. BedtimeRecommendation

**Purpose:** A single calculated bedtime option with metadata.

```kotlin
package com.sleepoptimizer.domain.entities

import java.time.LocalTime

data class BedtimeRecommendation(
    val bedtime: LocalTime,
    val cycleCount: Int,
    val cycleDurationMinutes: Int,
    val sleepLatencyMinutes: Int,
    val isOptimal: Boolean, // True for the recommended option (most cycles)
    val isPassed: Boolean = false, // True if bedtime is in the past
    val totalSleepMinutes: Int
) {
    fun formatBedtime(): String {
        return bedtime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    }
    
    fun getTotalSleepHours(): Double {
        return totalSleepMinutes / 60.0
    }
}
```

**Calculation Rules:**
- `totalSleepMinutes` = (cycleCount × cycleDurationMinutes) + sleepLatencyMinutes
- `isOptimal` = true for the option with the highest cycleCount
- `isPassed` = true if bedtime < current time (for UI strikethrough)

---

### 4. DiscoveryPhase

**Purpose:** Tracks the adaptive learning phase where the app tests different parameters.

```kotlin
package com.sleepoptimizer.domain.entities

import java.time.LocalDate

data class DiscoveryPhase(
    val isActive: Boolean,
    val currentShift: ShiftType,
    val startDate: LocalDate,
    val endDate: LocalDate, // 21 days after startDate
    val ratings: List<DailyRating> = emptyList()
) {
    companion object {
        const val DISCOVERY_DURATION_DAYS = 21
        const val MIN_RATINGS_REQUIRED = 7 // Need at least 7 days of data
    }
    
    fun getDaysRemaining(): Int {
        val now = LocalDate.now()
        return if (now.isBefore(endDate)) {
            java.time.Period.between(now, endDate).days
        } else 0
    }
    
    fun hasEnoughData(): Boolean {
        return ratings.size >= MIN_RATINGS_REQUIRED
    }
    
    fun getAverageRating(): Double? {
        return if (ratings.isNotEmpty()) {
            ratings.mapNotNull { it.rating }.average()
        } else null
    }
}

sealed class ShiftType(val displayName: String) {
    object LongerLatency : ShiftType("Längre insomning (30 min)")
    object LongerCycles : ShiftType("Längre cykler (105 min)")
    object FewerCycles : ShiftType("Färre cykler (5 st)")
    
    fun getCycleDuration(): Int = when (this) {
        is LongerCycles -> 105
        else -> 90
    }
    
    fun getSleepLatency(): Int = when (this) {
        is LongerLatency -> 30
        else -> 15
    }
    
    fun getCycleCount(): Int = when (this) {
        is FewerCycles -> 5
        else -> 6
    }
}

data class DailyRating(
    val date: LocalDate,
    val rating: Int?, // 1-5, null if not rated
    val shiftType: ShiftType,
    val actualWakeUpTime: Instant?
)
```

**Discovery Phase Flow:**
1. **Week 1 (Days 1-7):** Test LongerLatency (30 min)
2. **Week 2 (Days 8-14):** Test LongerCycles (105 min)
3. **Week 3 (Days 15-21):** Test FewerCycles (5 cycles)
4. **Analysis:** Compare average ratings for each shift vs baseline

---

### 5. SystemAlarm

**Purpose:** Represents an alarm from the Android system alarm app.

```kotlin
package com.sleepoptimizer.domain.entities

import java.time.Instant

data class SystemAlarm(
    val time: Instant,
    val label: String? = null,
    val isEnabled: Boolean = true
)
```

---

## Use Cases

### 1. CalculateOptimalBedtimeUseCase

**Purpose:** Calculate 3 bedtime recommendations based on target wake time.

**Signature:**
```kotlin
class CalculateOptimalBedtimeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        wakeUpTime: LocalTime,
        currentDate: LocalDate = LocalDate.now()
    ): Result<List<BedtimeRecommendation>>
}
```

**Algorithm:**
```kotlin
// Pseudocode
fun calculateBedtimes(wakeUpTime: LocalTime, profile: UserProfile): List<BedtimeRecommendation> {
    val cycleDuration = profile.optimalCycleMinutes
    val sleepLatency = profile.sleepLatencyMinutes
    val now = LocalTime.now()
    
    val recommendations = listOf(
        // 6 cycles (optimal)
        calculateForCycles(6, wakeUpTime, cycleDuration, sleepLatency),
        // 5 cycles
        calculateForCycles(5, wakeUpTime, cycleDuration, sleepLatency),
        // 4 cycles
        calculateForCycles(4, wakeUpTime, cycleDuration, sleepLatency)
    )
    
    return recommendations.map { rec ->
        rec.copy(
            isOptimal = rec.cycleCount == 6,
            isPassed = rec.bedtime.isBefore(now)
        )
    }
}

fun calculateForCycles(
    cycles: Int,
    wakeTime: LocalTime,
    cycleDuration: Int,
    sleepLatency: Int
): BedtimeRecommendation {
    val totalSleepMinutes = (cycles * cycleDuration) + sleepLatency
    val bedtime = wakeTime.minusMinutes(totalSleepMinutes.toLong())
    
    return BedtimeRecommendation(
        bedtime = bedtime,
        cycleCount = cycles,
        cycleDurationMinutes = cycleDuration,
        sleepLatencyMinutes = sleepLatency,
        isOptimal = false, // Set later
        totalSleepMinutes = totalSleepMinutes
    )
}
```

**Example:**
```
Input: wakeUpTime = 07:00, cycleDuration = 90, sleepLatency = 15
Process:
  6 cycles: 07:00 - (6 × 90 + 15) = 07:00 - 555 min = 21:45
  5 cycles: 07:00 - (5 × 90 + 15) = 07:00 - 465 min = 23:15
  4 cycles: 07:00 - (4 × 90 + 15) = 07:00 - 375 min = 00:45
Output: [
  BedtimeRecommendation(21:45, 6, optimal=true),
  BedtimeRecommendation(23:15, 5, optimal=false),
  BedtimeRecommendation(00:45, 4, optimal=false)
]
```

**Error Cases:**
- `InvalidWakeTime` - if wakeUpTime is invalid
- `UserProfileNotFound` - if no profile exists

---

### 2. LogSleepSessionUseCase

**Purpose:** Create or update a sleep log entry.

**Signature:**
```kotlin
class LogSleepSessionUseCase @Inject constructor(
    private val sleepRepository: SleepRepository
) {
    suspend fun createLog(
        targetWakeTime: Instant,
        recommendedBedtime: Instant,
        cycleCount: Int,
        cycleDuration: Int,
        sleepLatency: Int
    ): Result<SleepLog>
    
    suspend fun updateRating(
        logId: String,
        rating: Int
    ): Result<SleepLog>
    
    suspend fun updateActualTimes(
        logId: String,
        actualBedtime: Instant?,
        actualWakeTime: Instant?
    ): Result<SleepLog>
}
```

**Business Rules:**
- Rating must be 1-5
- `actualWakeTime` must be after `actualBedtime`
- Cannot update rating on already-rated log (prevents accidental overwrites)

---

### 3. GetSleepHistoryUseCase

**Purpose:** Retrieve sleep logs with filtering and sorting.

**Signature:**
```kotlin
class GetSleepHistoryUseCase @Inject constructor(
    private val sleepRepository: SleepRepository
) {
    fun getLogs(
        limit: Int = 30,
        dateRange: ClosedRange<LocalDate>? = null
    ): Flow<List<SleepLog>>
    
    fun getLogById(id: String): Flow<SleepLog?>
    
    suspend fun getAverageRating(
        dateRange: ClosedRange<LocalDate>
    ): Result<Double?>
}
```

**Example:**
```kotlin
// Get last 30 days of logs
val logs = getSleepHistoryUseCase.getLogs(
    limit = 30,
    dateRange = LocalDate.now().minusDays(30)..LocalDate.now()
).first()
```

---

### 4. StartDiscoveryPhaseUseCase

**Purpose:** Initialize a new Discovery Phase.

**Signature:**
```kotlin
class StartDiscoveryPhaseUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(): Result<DiscoveryPhase> {
        // 1. Check if user has enough baseline data (7+ rated logs)
        // 2. Create DiscoveryPhase starting with LongerLatency shift
        // 3. Set endDate = startDate + 21 days
        // 4. Save to UserProfile
    }
}
```

**Validation:**
- User must have at least 7 rated sleep logs (baseline data)
- Cannot start if a Discovery Phase is already active
- Returns `Result.Error(InsufficientData)` if <7 logs

---

### 5. AnalyzeDiscoveryPhaseUseCase

**Purpose:** Analyze Discovery Phase results and recommend optimal parameters.

**Signature:**
```kotlin
class AnalyzeDiscoveryPhaseUseCase @Inject constructor(
    private val sleepRepository: SleepRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        discoveryPhase: DiscoveryPhase
    ): Result<DiscoveryPhaseResult>
}

data class DiscoveryPhaseResult(
    val recommendedCycleDuration: Int,
    val recommendedSleepLatency: Int,
    val recommendedCycleCount: Int,
    val analysis: String // Human-readable summary
)
```

**Algorithm:**
```kotlin
fun analyzePhase(phase: DiscoveryPhase): DiscoveryPhaseResult {
    // 1. Group ratings by ShiftType
    val ratingsByShift = phase.ratings.groupBy { it.shiftType }
    
    // 2. Calculate average rating per shift
    val averages = ratingsByShift.mapValues { (_, ratings) ->
        ratings.mapNotNull { it.rating }.average()
    }
    
    // 3. Find best performing shift
    val bestShift = averages.maxByOrNull { it.value }?.key
    
    // 4. Return recommended parameters
    return DiscoveryPhaseResult(
        recommendedCycleDuration = bestShift?.getCycleDuration() ?: 90,
        recommendedSleepLatency = bestShift?.getSleepLatency() ?: 15,
        recommendedCycleCount = bestShift?.getCycleCount() ?: 6,
        analysis = generateAnalysis(averages)
    )
}
```

---

### 6. FetchSystemAlarmsUseCase

**Purpose:** Retrieve alarms from Android AlarmManager.

**Signature:**
```kotlin
class FetchSystemAlarmsUseCase @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(): Result<List<SystemAlarm>>
}
```

**Note:** Requires `READ_ALARM` permission (Android 12+)

---

## Repository Interfaces

### 1. SleepRepository

```kotlin
package com.sleepoptimizer.domain.repositories

interface SleepRepository {
    // Create
    suspend fun saveSleepLog(log: SleepLog): Result<Unit>
    
    // Read
    fun getAllSleepLogs(): Flow<List<SleepLog>>
    fun getSleepLog(id: String): Flow<SleepLog?>
    fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<SleepLog>>
    
    // Update
    suspend fun updateSleepLog(log: SleepLog): Result<Unit>
    suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit>
    
    // Delete
    suspend fun deleteSleepLog(id: String): Result<Unit>
    
    // Sync
    suspend fun getPendingUploadLogs(): List<SleepLog>
    suspend fun getLastSyncTimestamp(): Instant
    suspend fun updateLastSyncTimestamp(timestamp: Instant)
}
```

---

### 2. UserPreferencesRepository

```kotlin
package com.sleepoptimizer.domain.repositories

interface UserPreferencesRepository {
    // Profile
    suspend fun getUserProfile(): UserProfile
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    
    // Preferences
    suspend fun setCycleDuration(minutes: Int): Result<Unit>
    suspend fun setSleepLatency(minutes: Int): Result<Unit>
    suspend fun setReminderTime(time: String): Result<Unit>
    suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit>
    
    // Discovery Phase
    suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit>
    suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit>
    suspend fun endDiscoveryPhase(): Result<Unit>
    
    // Observe changes
    fun observeUserProfile(): Flow<UserProfile>
}
```

---

### 3. AlarmRepository

```kotlin
package com.sleepoptimizer.domain.repositories

interface AlarmRepository {
    suspend fun getNextAlarm(): SystemAlarm?
    suspend fun getAllAlarms(): Result<List<SystemAlarm>>
}
```

---

## Common Types

### Result Type

```kotlin
package com.sleepoptimizer.domain.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: DomainError) : Result<Nothing>()
    data object Loading : Result<Nothing>()
    
    fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }
    
    fun getOrNull(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }
}
```

---

### DomainError Type

```kotlin
package com.sleepoptimizer.domain.common

sealed class DomainError(open val message: String?) {
    // Network
    data class NetworkError(override val message: String?) : DomainError(message)
    
    // Database
    data class DatabaseError(override val message: String?) : DomainError(message)
    data class NotFound(override val message: String?) : DomainError(message)
    
    // Validation
    data class ValidationError(
        val field: String,
        val reason: String
    ) : DomainError("Invalid $field: $reason")
    
    // Calculation
    data class CalculationFailed(override val message: String?) : DomainError(message)
    
    // Discovery Phase
    data class InsufficientData(override val message: String?) : DomainError(message)
    data class DiscoveryPhaseAlreadyActive(override val message: String?) : DomainError(message)
    
    // Permissions
    data class PermissionDenied(val permission: String) : DomainError("Permission denied: $permission")
    
    // Auth
    data class AuthFailed(override val message: String?) : DomainError(message)
    data class UserNotAuthenticated(override val message: String? = "User not signed in") : DomainError(message)
}
```

---

## Business Logic Rules

### 1. Sleep Cycle Calculation

**Rule:** 1 sleep cycle = 90 minutes (default, adjustable 70-110)

**Formula:**
```
Bedtime = WakeTime - (CycleCount × CycleDuration) - SleepLatency

Example:
WakeTime = 07:00
CycleCount = 6
CycleDuration = 90 minutes
SleepLatency = 15 minutes

Bedtime = 07:00 - (6 × 90) - 15
        = 07:00 - 540 - 15
        = 07:00 - 555 minutes
        = 21:45
```

---

### 2. Optimal Bedtime Selection

**Rule:** The bedtime with the **highest cycle count** is marked as optimal.

**Rationale:** More complete sleep cycles = better sleep quality.

---

### 3. Discovery Phase Progression

**Rule:** 3 weeks, 1 shift type per week

| Week | Days  | Shift Type      | Parameters                        |
|------|-------|-----------------|-----------------------------------|
| 1    | 1-7   | LongerLatency   | 90 min cycles, 30 min latency    |
| 2    | 8-14  | LongerCycles    | 105 min cycles, 15 min latency   |
| 3    | 15-21 | FewerCycles     | 90 min cycles, 5 cycles total    |

**Analysis:** Compare average ratings per week against baseline (user's normal settings).

---

### 4. Rating Validation

**Rule:** Ratings must be 1-5 integers.

**Semantic Meaning:**
- 1 = Mycket trött (Very tired)
- 2 = Trött (Tired)
- 3 = Okej (Okay)
- 4 = Utvilad (Rested)
- 5 = Mycket utvilad (Very rested)

---

## Validation Rules

### UserProfile Validation

```kotlin
fun UserProfile.validate(): Result<Unit> {
    return when {
        optimalCycleMinutes !in MIN_CYCLE_DURATION..MAX_CYCLE_DURATION -> 
            Result.Error(ValidationError("optimalCycleMinutes", "Must be between $MIN_CYCLE_DURATION-$MAX_CYCLE_DURATION"))
        
        sleepLatencyMinutes !in MIN_SLEEP_LATENCY..MAX_SLEEP_LATENCY ->
            Result.Error(ValidationError("sleepLatencyMinutes", "Must be between $MIN_SLEEP_LATENCY-$MAX_SLEEP_LATENCY"))
        
        !isValidTimeFormat(reminderTime) ->
            Result.Error(ValidationError("reminderTime", "Must be in HH:mm format"))
        
        else -> Result.Success(Unit)
    }
}

fun isValidTimeFormat(time: String): Boolean {
    return time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
}
```

---

### SleepLog Validation

```kotlin
fun SleepLog.validate(): Result<Unit> {
    return when {
        wakeUpRating != null && wakeUpRating !in MIN_RATING..MAX_RATING ->
            Result.Error(ValidationError("wakeUpRating", "Must be between $MIN_RATING-$MAX_RATING"))
        
        cycleCount !in MIN_CYCLE_COUNT..MAX_CYCLE_COUNT ->
            Result.Error(ValidationError("cycleCount", "Must be between $MIN_CYCLE_COUNT-$MAX_CYCLE_COUNT"))
        
        actualBedtime != null && actualWakeTime != null && actualWakeTime.isBefore(actualBedtime) ->
            Result.Error(ValidationError("actualWakeTime", "Must be after actualBedtime"))
        
        else -> Result.Success(Unit)
    }
}
```

---

## Testing Considerations

### Example Unit Test

```kotlin
class CalculateOptimalBedtimeUseCaseTest {
    
    private lateinit var useCase: CalculateOptimalBedtimeUseCase
    private val fakePreferencesRepository = FakeUserPreferencesRepository()
    
    @Before
    fun setup() {
        fakePreferencesRepository.setProfile(
            UserProfile(
                userId = "test",
                optimalCycleMinutes = 90,
                sleepLatencyMinutes = 15
            )
        )
        useCase = CalculateOptimalBedtimeUseCase(fakePreferencesRepository)
    }
    
    @Test
    fun `should calculate 3 bedtime options for 7am wake time`() = runTest {
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        assertThat(result).isInstanceOf<Result.Success>()
        val recommendations = (result as Result.Success).data
        
        assertThat(recommendations).hasSize(3)
        assertThat(recommendations[0].bedtime).isEqualTo(LocalTime.of(21, 45)) // 6 cycles
        assertThat(recommendations[0].isOptimal).isTrue()
        assertThat(recommendations[1].bedtime).isEqualTo(LocalTime.of(23, 15)) // 5 cycles
        assertThat(recommendations[2].bedtime).isEqualTo(LocalTime.of(0, 45))  // 4 cycles
    }
    
    @Test
    fun `should mark bedtimes in the past as passed`() = runTest {
        // Given: Current time is 22:00
        val now = LocalTime.of(22, 0)
        
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        val recommendations = (result as Result.Success).data
        assertThat(recommendations[0].isPassed).isTrue()  // 21:45 is in the past
        assertThat(recommendations[1].isPassed).isFalse() // 23:15 is in the future
    }
}
```

---

## Implementation Checklist

Before starting implementation, ensure:

- [ ] All entities are defined with exact field types
- [ ] All use case signatures are documented
- [ ] All repository interfaces are specified
- [ ] Result/Error types cover all failure scenarios
- [ ] Business logic formulas are validated
- [ ] Validation rules are exhaustive
- [ ] Test scenarios are outlined

---

*This specification serves as the single source of truth for the domain layer. All implementation must match this spec exactly.*

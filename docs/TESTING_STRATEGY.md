# Testing Strategy

> Comprehensive testing approach ensuring 80% domain coverage and production-ready code quality

---

## Table of Contents
1. [Overview](#overview)
2. [Test Pyramid](#test-pyramid)
3. [Domain Layer Testing](#domain-layer-testing)
4. [Data Layer Testing](#data-layer-testing)
5. [Presentation Layer Testing](#presentation-layer-testing)
6. [End-to-End Testing](#end-to-end-testing)
7. [Test Doubles Strategy](#test-doubles-strategy)
8. [Critical Test Scenarios](#critical-test-scenarios)
9. [Flaky Test Prevention](#flaky-test-prevention)
10. [CI/CD Integration](#cicd-integration)

---

## Overview

### Testing Philosophy

**Goals:**
1. **Confidence:** Tests should catch bugs before they reach users
2. **Speed:** Fast feedback loop for developers
3. **Maintainability:** Tests should be easy to understand and update
4. **Coverage:** Meet minimum coverage requirements per layer

**Coverage Requirements:**

| Layer | Minimum Coverage | Target Coverage |
|-------|------------------|-----------------|
| Domain | 80% | 90%+ |
| Data (Repository) | 70% | 80% |
| Presentation (ViewModel) | 60% | 70% |
| UI (Compose) | 40% | 50% |

---

## Test Pyramid

```
                   ╱╲
                  ╱  ╲
                 ╱ E2E╲           ~5% (UI Tests)
                ╱──────╲
               ╱        ╲
              ╱Integration╲       ~15% (Repository, ViewModel)
             ╱────────────╲
            ╱              ╲
           ╱  Unit  Tests  ╲     ~80% (Domain, Pure Logic)
          ╱────────────────╲
```

### Distribution

- **Unit Tests (80%):** Domain logic, use cases, calculations
- **Integration Tests (15%):** Room, Repository, ViewModel
- **UI Tests (5%):** Critical user flows only

---

## Domain Layer Testing

### Strategy: Pure Unit Tests (No Mocks)

**Why no mocks?**
- Domain layer is pure Kotlin - easy to test without mocks
- Fakes are more reliable than mocks
- Tests run faster

### Example: CalculateOptimalBedtimeUseCase

```kotlin
package com.sleepoptimizer.domain.usecases

import com.sleepoptimizer.domain.entities.UserProfile
import com.sleepoptimizer.domain.repositories.fakes.FakeUserPreferencesRepository
import com.sleepoptimizer.domain.common.Result
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateOptimalBedtimeUseCaseTest {
    
    private lateinit var useCase: CalculateOptimalBedtimeUseCase
    private lateinit var fakePreferencesRepo: FakeUserPreferencesRepository
    
    @Before
    fun setup() {
        fakePreferencesRepo = FakeUserPreferencesRepository()
        useCase = CalculateOptimalBedtimeUseCase(fakePreferencesRepo)
    }
    
    @Test
    fun `should return 3 bedtime options for 7am wake time`() = runTest {
        // Given
        fakePreferencesRepo.setProfile(
            UserProfile(
                userId = "test",
                optimalCycleMinutes = 90,
                sleepLatencyMinutes = 15
            )
        )
        
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        assertTrue(result is Result.Success)
        val recommendations = (result as Result.Success).data
        assertEquals(3, recommendations.size)
        
        // 6 cycles: 07:00 - (6 × 90 + 15) = 21:45
        assertEquals(LocalTime.of(21, 45), recommendations[0].bedtime)
        assertTrue(recommendations[0].isOptimal)
        
        // 5 cycles: 07:00 - (5 × 90 + 15) = 23:15
        assertEquals(LocalTime.of(23, 15), recommendations[1].bedtime)
        
        // 4 cycles: 07:00 - (4 × 90 + 15) = 00:45
        assertEquals(LocalTime.of(0, 45), recommendations[2].bedtime)
    }
    
    @Test
    fun `should mark bedtimes in the past as passed`() = runTest {
        // Given: Current time is 22:00
        // 21:45 should be marked as passed, but 23:15 should not
        fakePreferencesRepo.setProfile(
            UserProfile(userId = "test", optimalCycleMinutes = 90, sleepLatencyMinutes = 15)
        )
        
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        val recommendations = (result as Result.Success).data
        // Note: This test is time-dependent - should be refactored to inject Clock
        // For now, we document the expected behavior
    }
    
    @Test
    fun `should use custom cycle duration from user profile`() = runTest {
        // Given: User has 105-minute cycles
        fakePreferencesRepo.setProfile(
            UserProfile(userId = "test", optimalCycleMinutes = 105, sleepLatencyMinutes = 15)
        )
        
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        val recommendations = (result as Result.Success).data
        // 6 cycles: 07:00 - (6 × 105 + 15) = 19:15
        assertEquals(LocalTime.of(19, 15), recommendations[0].bedtime)
        assertEquals(105, recommendations[0].cycleDurationMinutes)
    }
    
    @Test
    fun `should handle midnight crossing correctly`() = runTest {
        // Given
        fakePreferencesRepo.setProfile(
            UserProfile(userId = "test", optimalCycleMinutes = 90, sleepLatencyMinutes = 15)
        )
        
        // When: Wake time is 01:00 AM
        val result = useCase(LocalTime.of(1, 0))
        
        // Then: Bedtimes should wrap around midnight
        val recommendations = (result as Result.Success).data
        // 6 cycles: 01:00 - (6 × 90 + 15) = 15:45 (previous day)
        assertEquals(LocalTime.of(15, 45), recommendations[0].bedtime)
    }
}
```

---

### Critical Domain Tests

**Must-have test cases:**

1. **Sleep Calculation Tests**
   - ✅ Standard 90-minute cycles
   - ✅ Custom cycle durations (70-110 min)
   - ✅ Custom sleep latency (5-45 min)
   - ✅ Midnight crossing (bedtime before/after midnight)
   - ✅ Edge cases (very early/late wake times)

2. **Discovery Phase Tests**
   - ✅ Start phase with sufficient data (7+ logs)
   - ✅ Reject start with insufficient data
   - ✅ Reject start if already active
   - ✅ Analyze phase results correctly
   - ✅ Compare average ratings per shift type

3. **Validation Tests**
   - ✅ Rating validation (1-5 only)
   - ✅ Cycle count validation (3-8)
   - ✅ Time format validation (HH:mm)
   - ✅ actualWakeTime after actualBedtime

---

## Data Layer Testing

### Strategy: Integration Tests with In-Memory Database

**Why integration tests?**
- Room queries need to be tested against real SQLite
- In-memory database is fast (~10ms per test)
- Catches SQL errors that mocks wouldn't

### Example: SleepLogDao Test

```kotlin
package com.sleepoptimizer.data.local.room.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sleepoptimizer.data.local.room.SleepDatabase
import com.sleepoptimizer.data.local.room.entities.SleepLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class SleepLogDaoTest {
    
    private lateinit var database: SleepDatabase
    private lateinit var dao: SleepLogDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
            .allowMainThreadQueries() // Only for testing
            .build()
        dao = database.sleepLogDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndRetrieveSleepLog() = runTest {
        // Given
        val log = createTestLog(id = "test-1", rating = 4)
        
        // When
        dao.insert(log)
        val retrieved = dao.getSleepLogById("test-1").first()
        
        // Then
        assertNotNull(retrieved)
        assertEquals(log.id, retrieved.id)
        assertEquals(log.wakeUpRating, retrieved.wakeUpRating)
    }
    
    @Test
    fun getRatedLogsReturnsOnlyLogsWithRatings() = runTest {
        // Given
        dao.insert(createTestLog(id = "rated-1", rating = 4))
        dao.insert(createTestLog(id = "unrated", rating = null))
        dao.insert(createTestLog(id = "rated-2", rating = 5))
        
        // When
        val ratedLogs = dao.getRatedLogs(limit = 10)
        
        // Then
        assertEquals(2, ratedLogs.size)
        assertTrue(ratedLogs.all { it.wakeUpRating != null })
    }
    
    @Test
    fun getLogsInDateRangeWorksCorrectly() = runTest {
        // Given: Logs from different dates
        val now = Instant.now()
        dao.insert(createTestLog(id = "old", targetWakeTime = now.minusSeconds(86400 * 10)))
        dao.insert(createTestLog(id = "recent", targetWakeTime = now.minusSeconds(86400 * 2)))
        dao.insert(createTestLog(id = "today", targetWakeTime = now))
        
        // When: Query last 7 days
        val startTime = now.minusSeconds(86400 * 7).toEpochMilli()
        val endTime = now.toEpochMilli()
        val logs = dao.getSleepLogsByDateRange(startTime, endTime).first()
        
        // Then: Should only get logs from last 7 days
        assertEquals(2, logs.size)
        assertTrue(logs.none { it.id == "old" })
    }
    
    @Test
    fun updateRatingWorksCorrectly() = runTest {
        // Given
        val log = createTestLog(id = "test", rating = null)
        dao.insert(log)
        
        // When
        dao.updateRating("test", rating = 5, updatedAt = Instant.now().toEpochMilli())
        
        // Then
        val updated = dao.getSleepLogById("test").first()
        assertEquals(5, updated?.wakeUpRating)
    }
    
    @Test
    fun getPendingUploadLogsReturnsOnlyUnsyncedLogs() = runTest {
        // Given
        dao.insert(createTestLog(id = "synced", syncStatus = "SYNCED"))
        dao.insert(createTestLog(id = "pending", syncStatus = "PENDING_UPLOAD"))
        
        // When
        val pending = dao.getPendingUploadLogs()
        
        // Then
        assertEquals(1, pending.size)
        assertEquals("pending", pending[0].id)
    }
    
    // Helper function
    private fun createTestLog(
        id: String,
        rating: Int? = null,
        targetWakeTime: Instant = Instant.now(),
        syncStatus: String = "PENDING_UPLOAD"
    ): SleepLogEntity {
        return SleepLogEntity(
            id = id,
            targetWakeTime = targetWakeTime.toEpochMilli(),
            recommendedBedtime = null,
            actualBedtime = null,
            actualWakeTime = null,
            wakeUpRating = rating,
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            notes = null,
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli(),
            syncStatus = syncStatus
        )
    }
}
```

---

### Repository Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class SleepRepositoryImplTest {
    
    private lateinit var database: SleepDatabase
    private lateinit var dao: SleepLogDao
    private lateinit var repository: SleepRepositoryImpl
    private lateinit var workManager: WorkManager
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java).build()
        dao = database.sleepLogDao()
        workManager = WorkManager.getInstance(context)
        repository = SleepRepositoryImpl(dao, workManager)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun saveSleepLogWritesToDatabaseImmediately() = runTest {
        // Given
        val log = SleepLog(
            id = "test",
            targetWakeTime = Instant.now(),
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        
        // When
        val result = repository.saveSleepLog(log)
        
        // Then: Should succeed immediately (not wait for Firebase)
        assertTrue(result is Result.Success)
        
        // Verify written to Room
        val retrieved = repository.getSleepLog("test").first()
        assertNotNull(retrieved)
        assertEquals(log.id, retrieved.id)
    }
}
```

---

## Presentation Layer Testing

### Strategy: ViewModel Tests with Fake UseCases

**Why fake use cases?**
- Fast tests (no Room or Firebase)
- Easy to simulate different scenarios
- Tests ViewModel logic, not domain logic

### Example: SleepCalculatorViewModel Test

```kotlin
class SleepCalculatorViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var viewModel: SleepCalculatorViewModel
    private lateinit var fakeCalculateBedtimeUseCase: FakeCalculateOptimalBedtimeUseCase
    private lateinit var fakeLogSleepSessionUseCase: FakeLogSleepSessionUseCase
    
    @Before
    fun setup() {
        fakeCalculateBedtimeUseCase = FakeCalculateOptimalBedtimeUseCase()
        fakeLogSleepSessionUseCase = FakeLogSleepSessionUseCase()
        viewModel = SleepCalculatorViewModel(
            calculateBedtimeUseCase = fakeCalculateBedtimeUseCase,
            logSleepSessionUseCase = fakeLogSleepSessionUseCase
        )
    }
    
    @Test
    fun `calculateBedtimes updates UI state with recommendations`() = runTest {
        // Given: Use case will return 3 recommendations
        val recommendations = listOf(
            BedtimeRecommendation(LocalTime.of(21, 45), 6, 90, 15, true, false, 555),
            BedtimeRecommendation(LocalTime.of(23, 15), 5, 90, 15, false, false, 465),
            BedtimeRecommendation(LocalTime.of(0, 45), 4, 90, 15, false, false, 375)
        )
        fakeCalculateBedtimeUseCase.setResult(Result.Success(recommendations))
        
        // When
        viewModel.calculateBedtimes(LocalTime.of(7, 0))
        advanceUntilIdle() // Wait for coroutines
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.bedtimeRecommendations.size)
        assertNull(state.errorMessage)
    }
    
    @Test
    fun `calculateBedtimes shows error when use case fails`() = runTest {
        // Given
        fakeCalculateBedtimeUseCase.setResult(
            Result.Error(DomainError.CalculationFailed("Test error"))
        )
        
        // When
        viewModel.calculateBedtimes(LocalTime.of(7, 0))
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.bedtimeRecommendations.isEmpty())
        assertNotNull(state.errorMessage)
    }
    
    @Test
    fun `selectBedtime schedules reminder and logs session`() = runTest {
        // Given
        val bedtime = LocalTime.of(21, 45)
        
        // When
        viewModel.selectBedtime(bedtime)
        advanceUntilIdle()
        
        // Then
        assertTrue(fakeLogSleepSessionUseCase.wasCreateLogCalled)
        // Would also verify reminder scheduled (needs BedtimeReminderScheduler fake)
    }
}

// Fake Use Case
class FakeCalculateOptimalBedtimeUseCase : CalculateOptimalBedtimeUseCase {
    private var result: Result<List<BedtimeRecommendation>> = Result.Loading
    
    fun setResult(newResult: Result<List<BedtimeRecommendation>>) {
        result = newResult
    }
    
    override suspend fun invoke(wakeUpTime: LocalTime): Result<List<BedtimeRecommendation>> {
        return result
    }
}
```

---

## End-to-End Testing

### Strategy: Critical User Flows Only

**Why limited E2E tests?**
- Slow (1-2 seconds per test)
- Flaky (UI timing issues)
- Should only cover integration points

### Example: Onboarding Flow Test

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class OnboardingFlowTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun userCanCompleteOnboardingFlow() {
        // Welcome Screen
        composeTestRule.onNodeWithText("Välkommen till Sleep Cycle Optimizer")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Kom igång")
            .performClick()
        
        // Permissions Screen
        composeTestRule.onNodeWithText("Notifikationer")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Tillåt")
            .performClick()
        
        composeTestRule.onNodeWithText("Fortsätt")
            .performClick()
        
        // Set Wake Time Screen
        composeTestRule.onNodeWithText("När vaknar du vanligtvis?")
            .assertIsDisplayed()
        
        // Set time to 07:00
        composeTestRule.onNodeWithTag("time_picker")
            .performClick()
        
        // (Time picker interaction - simplified for example)
        
        composeTestRule.onNodeWithText("Fortsätt")
            .performClick()
        
        // Should navigate to main calculator screen
        composeTestRule.onNodeWithText("Sleep Cycle Optimizer")
            .assertIsDisplayed()
    }
}
```

---

## Test Doubles Strategy

### Fake > Mock

**✅ PREFER Fakes:**
```kotlin
class FakeSleepRepository : SleepRepository {
    private val logs = mutableListOf<SleepLog>()
    
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        logs.add(log)
        return Result.Success(Unit)
    }
    
    override fun getAllSleepLogs(): Flow<List<SleepLog>> = flowOf(logs)
    
    // Test helpers
    fun getLogs() = logs.toList()
    fun clear() = logs.clear()
}
```

**❌ AVOID Mocks (unless unavoidable):**
```kotlin
@MockK
lateinit var mockRepository: SleepRepository

every { mockRepository.saveSleepLog(any()) } returns Result.Success(Unit)
```

**Why fakes are better:**
- More realistic behavior
- No need to mock every method
- Self-documenting (fake acts like real thing)
- No Mockk dependency

---

## Critical Test Scenarios

### 1. Sleep Calculation Edge Cases

```kotlin
@Test
fun `bedtime at exactly midnight`() = runTest {
    // Wake time: 07:30, 6 cycles = 00:00 bedtime
    val result = useCase(LocalTime.of(7, 30))
    val recommendations = (result as Result.Success).data
    assertEquals(LocalTime.MIDNIGHT, recommendations[0].bedtime)
}

@Test
fun `very short sleep (3 cycles)`() = runTest {
    // Test minimum cycle count
    fakePreferencesRepo.setProfile(
        UserProfile(userId = "test", optimalCycleMinutes = 90, sleepLatencyMinutes = 15)
    )
    
    val result = useCase(LocalTime.of(5, 0)) // Early wake time
    val recommendations = (result as Result.Success).data
    assertTrue(recommendations.isNotEmpty())
}
```

---

### 2. Discovery Phase Edge Cases

```kotlin
@Test
fun `cannot start discovery phase with less than 7 rated logs`() = runTest {
    // Given: Only 5 rated logs
    fakeSleepRepo.setLogs(createLogsWithRatings(count = 5))
    
    // When
    val result = startDiscoveryPhaseUseCase()
    
    // Then
    assertTrue(result is Result.Error)
    val error = (result as Result.Error).error
    assertTrue(error is DomainError.InsufficientData)
}

@Test
fun `discovery phase analysis selects best performing shift`() = runTest {
    // Given: Week 1 (LongerLatency) avg rating: 3.0
    //        Week 2 (LongerCycles) avg rating: 4.5
    //        Week 3 (FewerCycles) avg rating: 3.5
    val phase = DiscoveryPhase(
        isActive = false,
        currentShift = ShiftType.FewerCycles,
        startDate = LocalDate.now().minusDays(21),
        endDate = LocalDate.now(),
        ratings = createRatingsForPhase()
    )
    
    // When
    val result = analyzeDiscoveryPhaseUseCase(phase)
    
    // Then
    assertTrue(result is Result.Success)
    val analysis = (result as Result.Success).data
    assertEquals(105, analysis.recommendedCycleDuration) // LongerCycles won
}
```

---

### 3. Notification Timing

```kotlin
@Test
fun `daily check-in scheduled for tomorrow if already past 18_00`() {
    // Given: Current time is 20:00
    val scheduler = DailyCheckInScheduler(alarmManager, context, fakePrefsRepo)
    val now = LocalDateTime.of(2024, 5, 10, 20, 0)
    
    // When
    scheduler.scheduleCheckIn()
    
    // Then
    // Should schedule for next day at 18:00
    val expectedTime = LocalDateTime.of(2024, 5, 11, 18, 0)
    verify(alarmManager).setExactAndAllowWhileIdle(
        eq(AlarmManager.RTC_WAKEUP),
        eq(expectedTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
        any()
    )
}
```

---

## Flaky Test Prevention

### 1. Avoid Time-Dependent Tests

**❌ BAD: Uses real time**
```kotlin
@Test
fun `bedtime is in the past`() {
    val result = useCase(LocalTime.of(7, 0))
    // FLAKY: Depends on when test runs
    assertTrue(recommendations[0].isPassed)
}
```

**✅ GOOD: Inject Clock**
```kotlin
@Test
fun `bedtime is in the past when current time is after it`() {
    // Inject fixed Clock
    val fixedClock = Clock.fixed(
        LocalTime.of(22, 0).atDate(LocalDate.now()).toInstant(ZoneOffset.UTC),
        ZoneOffset.UTC
    )
    val useCase = CalculateOptimalBedtimeUseCase(fakePrefsRepo, fixedClock)
    
    val result = useCase(LocalTime.of(7, 0))
    assertTrue(recommendations[0].isPassed) // 21:45 is before 22:00
}
```

---

### 2. Use Test Dispatchers

```kotlin
@Test
fun `viewmodel updates state correctly`() = runTest {
    // Using runTest from kotlinx-coroutines-test
    viewModel.calculateBedtimes(LocalTime.of(7, 0))
    advanceUntilIdle() // Wait for all coroutines
    
    assertEquals(3, viewModel.uiState.value.bedtimeRecommendations.size)
}
```

---

### 3. Idempotent Database Tests

```kotlin
@Before
fun setup() {
    database = Room.inMemoryDatabaseBuilder(...).build()
    // Fresh database per test - no leftover state
}

@After
fun teardown() {
    database.close()
}
```

---

## CI/CD Integration

### GitHub Actions Workflow

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run Unit Tests
        run: ./gradlew test
      
      - name: Generate Coverage Report
        run: ./gradlew koverHtmlReport
      
      - name: Check Coverage Thresholds
        run: |
          ./gradlew koverVerify
          # Fails if domain < 80%, data < 70%, presentation < 60%
  
  integration-tests:
    runs-on: macos-latest # Needed for Android Emulator
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Integration Tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 34
          script: ./gradlew connectedCheck
```

---

### Coverage Thresholds (build.gradle.kts)

```kotlin
koverReport {
    verify {
        rule {
            name = "Domain Layer Coverage"
            bound {
                minValue = 80
                metric = MetricType.LINE
                aggregation = AggregationType.COVERED_PERCENTAGE
            }
            filters {
                includes {
                    classes("com.sleepoptimizer.domain.*")
                }
            }
        }
        
        rule {
            name = "Data Layer Coverage"
            bound {
                minValue = 70
            }
            filters {
                includes {
                    classes("com.sleepoptimizer.data.repositories.*")
                }
            }
        }
    }
}
```

---

## Test Organization

### File Structure

```
domain/
└── src/
    ├── main/kotlin/
    └── test/kotlin/
        ├── usecases/
        │   ├── CalculateOptimalBedtimeUseCaseTest.kt
        │   └── StartDiscoveryPhaseUseCaseTest.kt
        ├── entities/
        │   └── DiscoveryPhaseTest.kt
        └── fakes/
            ├── FakeSleepRepository.kt
            └── FakeUserPreferencesRepository.kt

data/
└── src/
    └── androidTest/kotlin/
        ├── dao/
        │   ├── SleepLogDaoTest.kt
        │   └── UserProfileDaoTest.kt
        └── repositories/
            └── SleepRepositoryImplTest.kt

presentation/
└── src/
    └── test/kotlin/
        └── viewmodels/
            ├── SleepCalculatorViewModelTest.kt
            └── HistoryViewModelTest.kt
```

---

## Test Checklist

Before merging a PR:

- [ ] All domain use cases have >80% coverage
- [ ] All repository implementations have integration tests
- [ ] ViewModels have tests with fake use cases
- [ ] Critical user flows have E2E tests
- [ ] No flaky tests (run suite 3x to verify)
- [ ] All tests pass in CI
- [ ] Coverage thresholds met

---

*This testing strategy ensures high-quality, maintainable code with fast feedback loops and minimal flakiness.*

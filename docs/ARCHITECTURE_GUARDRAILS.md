# Architecture Guardrails

> **Immutable rules for maintaining Clean Architecture integrity throughout the Sleep Cycle Optimizer project**

This document defines **non-negotiable architectural constraints** that must be followed during development. These rules exist to prevent technical debt, ensure testability, and maintain the project's portfolio quality.

---

## Table of Contents
1. [Core Principles](#core-principles)
2. [Layer Dependency Rules](#layer-dependency-rules)
3. [Module Structure Rules](#module-structure-rules)
4. [Data Flow Contracts](#data-flow-contracts)
5. [Testing Requirements](#testing-requirements)
6. [Code Quality Standards](#code-quality-standards)
7. [Error Handling Strategy](#error-handling-strategy)
8. [Performance Budgets](#performance-budgets)
9. [Firebase Integration Rules](#firebase-integration-rules)
10. [Violation Detection](#violation-detection)

---

## Core Principles

### The Three Immutable Laws

1. **Domain Independence**  
   The `:domain` module SHALL NOT depend on Android framework, Firebase, or any external library except Kotlin stdlib and coroutines.

2. **Single Source of Truth**  
   Room database IS the source of truth. Firebase is a secondary sync mechanism, NOT the primary data store.

3. **Unidirectional Data Flow**  
   Data flows DOWN the architecture (Presentation → Domain → Data). The Data layer NEVER directly updates UI state.

---

## Layer Dependency Rules

### Allowed Dependencies

```
┌─────────────────────────────────────────────────────────────────┐
│ PRESENTATION LAYER (:presentation, :app)                         │
│                                                                  │
│ ✅ CAN depend on:                                                │
│    - :domain (UseCases, Entities, Repository Interfaces)        │
│    - Jetpack Compose, ViewModel, Hilt                           │
│    - Coroutines, Flow                                            │
│                                                                  │
│ ❌ CANNOT depend on:                                             │
│    - :data (Repository implementations)                          │
│    - Room, DataStore, Firebase (use :domain interfaces only)    │
│    - Direct database access                                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ DOMAIN LAYER (:domain)                                           │
│                                                                  │
│ ✅ CAN depend on:                                                │
│    - Kotlin stdlib ONLY                                          │
│    - Coroutines (kotlinx-coroutines-core, NOT -android)         │
│    - Nothing else                                                │
│                                                                  │
│ ❌ CANNOT depend on:                                             │
│    - android.*                                                   │
│    - androidx.*                                                  │
│    - com.google.firebase.*                                       │
│    - :presentation, :data                                        │
│    - Any third-party library                                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ DATA LAYER (:data)                                               │
│                                                                  │
│ ✅ CAN depend on:                                                │
│    - :domain (Repository Interfaces, Entities)                  │
│    - Room, DataStore, Firebase                                   │
│    - WorkManager, Retrofit (if needed)                           │
│                                                                  │
│ ❌ CANNOT depend on:                                             │
│    - :presentation                                               │
│    - ViewModel, Compose                                          │
│    - Direct UI components                                        │
└─────────────────────────────────────────────────────────────────┘
```

### Enforcement

**Gradle Configuration (build.gradle.kts):**

```kotlin
// domain/build.gradle.kts
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core) // NOT coroutines-android
    
    // NO android.*, androidx.*, firebase.* dependencies allowed
    // CI will fail if Android dependencies are detected
}

// Dependency guard check
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("androidx") || 
            requested.group.startsWith("com.google.android") ||
            requested.group.startsWith("com.google.firebase")) {
            throw GradleException(
                "Domain layer cannot depend on Android/Firebase libraries: ${requested.group}"
            )
        }
    }
}
```

---

## Module Structure Rules

### File Organization

**MANDATORY naming conventions:**

| Type | Naming Rule | Example |
|------|-------------|---------|
| **UseCases** | Must end with `UseCase` | `CalculateOptimalBedtimeUseCase` |
| **Repositories (Interface)** | Must end with `Repository` | `SleepRepository` |
| **Repositories (Impl)** | Must end with `RepositoryImpl` | `SleepRepositoryImpl` |
| **ViewModels** | Must end with `ViewModel` | `SleepCalculatorViewModel` |
| **Entities (Domain)** | Plain nouns | `UserProfile`, `SleepLog` |
| **Entities (Room)** | Must end with `Entity` | `SleepLogEntity` |
| **DTOs (Firebase)** | Must end with `Dto` | `SleepLogDto` |

### Package Structure

```
domain/
├── entities/          # Domain models (plain Kotlin data classes)
├── usecases/          # Business logic (one public function per UseCase)
├── repositories/      # Repository INTERFACES only
└── common/            # Shared domain types (Result, DomainError)

data/
├── local/
│   ├── room/
│   │   ├── entities/  # Room-specific entities (separate from domain)
│   │   └── dao/       # Data Access Objects
│   └── datastore/     # Preferences management
├── remote/
│   ├── firebase/
│   │   └── models/    # Firestore DTOs (separate from domain)
│   └── sync/          # WorkManager sync tasks
└── repositories/      # Repository IMPLEMENTATIONS
    └── mappers/       # Entity ↔ DTO converters
```

### Entity Mapping Rules

**NEVER use domain entities directly in Room or Firebase.**

**Rule:** Domain entities are pure business models. Persistence layers (Room, Firestore) have their own representation.

**Example:**

```kotlin
// ✅ CORRECT: Separate entities

// domain/entities/SleepLog.kt
data class SleepLog(
    val id: String,
    val timestamp: Instant,        // Kotlin Instant
    val wakeUpRating: Int?,
    val cycleCount: Int
)

// data/local/room/entities/SleepLogEntity.kt
@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,           // Room stores as Long
    val wakeUpRating: Int?,
    val cycleCount: Int
)

// data/remote/firebase/models/SleepLogDto.kt
data class SleepLogDto(
    val id: String? = null,
    val timestamp: Long? = null,   // Firestore uses nullable Long
    val wakeUpRating: Int? = null,
    val cycleCount: Int? = null
)

// ❌ WRONG: Using domain entity in Room
@Entity(tableName = "sleep_logs")
data class SleepLog(...) // Domain entity should NOT have @Entity annotation
```

**Mappers are mandatory:**

```kotlin
// data/repositories/mappers/SleepLogMapper.kt
fun SleepLogEntity.toDomain(): SleepLog = SleepLog(
    id = id,
    timestamp = Instant.ofEpochMilli(timestamp),
    wakeUpRating = wakeUpRating,
    cycleCount = cycleCount
)

fun SleepLog.toEntity(): SleepLogEntity = SleepLogEntity(
    id = id,
    timestamp = timestamp.toEpochMilli(),
    wakeUpRating = wakeUpRating,
    cycleCount = cycleCount
)
```

---

## Data Flow Contracts

### ViewModel → UseCase → Repository

**Rule:** ViewModels MUST interact with domain layer through UseCases only. No direct repository calls.

```kotlin
// ✅ CORRECT
class SleepCalculatorViewModel @Inject constructor(
    private val calculateBedtimeUseCase: CalculateOptimalBedtimeUseCase
) : ViewModel() {
    
    fun calculateBedtimes(wakeUpTime: LocalTime) {
        viewModelScope.launch {
            val result = calculateBedtimeUseCase(wakeUpTime)
            _uiState.value = result.toUiState()
        }
    }
}

// ❌ WRONG: Direct repository access
class SleepCalculatorViewModel @Inject constructor(
    private val sleepRepository: SleepRepository // ❌ ViewModels should not inject repositories
) : ViewModel() { ... }
```

### UseCase Signature Rules

**All UseCases MUST:**
1. Be suspending functions OR return Flow
2. Accept simple input parameters (domain entities, primitives)
3. Return `Result<T>` for operations that can fail
4. Have a single public `invoke` or `execute` function

```kotlin
// ✅ CORRECT: Simple, testable UseCase
class CalculateOptimalBedtimeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(wakeUpTime: LocalTime): Result<List<BedtimeRecommendation>> {
        return try {
            val profile = userPreferencesRepository.getUserProfile()
            val bedtimes = calculateBedtimes(wakeUpTime, profile)
            Result.Success(bedtimes)
        } catch (e: Exception) {
            Result.Error(DomainError.CalculationFailed(e.message))
        }
    }
    
    private fun calculateBedtimes(...): List<BedtimeRecommendation> {
        // Pure calculation logic
    }
}

// ❌ WRONG: Multiple public functions, no error handling
class CalculateOptimalBedtimeUseCase {
    fun calculateFor6Cycles(...) { }
    fun calculateFor5Cycles(...) { }
    fun calculateFor7Cycles(...) { }
    // ❌ Fragmented responsibilities
}
```

---

## Testing Requirements

### Coverage Requirements

| Layer | Minimum Coverage | Test Type |
|-------|------------------|-----------|
| **Domain** | 80% | Unit tests (fast, no mocks) |
| **Data (Repository)** | 70% | Integration tests (Fake implementations) |
| **Presentation (ViewModel)** | 60% | Unit tests (Fake UseCases) |
| **UI (Compose)** | 40% | UI tests (critical paths only) |

### Testing Rules

**Domain Layer Testing:**

```kotlin
// ✅ CORRECT: Pure unit test with no mocks
class CalculateOptimalBedtimeUseCaseTest {
    
    private lateinit var useCase: CalculateOptimalBedtimeUseCase
    private val fakePreferencesRepository = FakeUserPreferencesRepository()
    
    @Test
    fun `should return 3 bedtime options for 7am wake time`() = runTest {
        // Given
        fakePreferencesRepository.setProfile(
            UserProfile(cycleDuration = 90, sleepLatency = 15)
        )
        
        // When
        val result = useCase(LocalTime.of(7, 0))
        
        // Then
        assertThat(result).isInstanceOf<Result.Success>()
        val bedtimes = (result as Result.Success).data
        assertThat(bedtimes).hasSize(3)
        assertThat(bedtimes[0].bedtime).isEqualTo(LocalTime.of(21, 45)) // 6 cycles
    }
}

// ❌ WRONG: Using mockk when a fake would suffice
class CalculateOptimalBedtimeUseCaseTest {
    @MockK lateinit var repository: UserPreferencesRepository
    // Mocks couple tests to implementation details
}
```

**Repository Testing:**

```kotlin
// ✅ CORRECT: Integration test with real Room in-memory database
@RunWith(AndroidJUnit4::class)
class SleepRepositoryImplTest {
    
    private lateinit var database: SleepDatabase
    private lateinit var repository: SleepRepositoryImpl
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SleepRepositoryImpl(database.sleepLogDao())
    }
    
    @Test
    fun `should persist and retrieve sleep log`() = runTest {
        // Given
        val log = SleepLog(id = "test", ...)
        
        // When
        repository.saveSleepLog(log)
        val retrieved = repository.getSleepLog("test")
        
        // Then
        assertThat(retrieved).isEqualTo(log)
    }
}
```

### Test File Organization

```
domain/
└── src/
    ├── main/kotlin/
    └── test/kotlin/          # Unit tests mirror main structure
        ├── usecases/
        ├── entities/
        └── fakes/            # Fake repository implementations
            └── FakeUserPreferencesRepository.kt

data/
└── src/
    └── androidTest/kotlin/   # Integration tests for Room/DataStore
        └── repositories/
```

---

## Code Quality Standards

### Static Analysis Configuration

**Detekt Rules (detekt.yml):**

```yaml
complexity:
  CyclomaticComplexMethod:
    active: true
    threshold: 10  # Maximum function complexity

naming:
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
  
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'

style:
  MagicNumber:
    active: true
    ignoreNumbers: ['-1', '0', '1', '2', '90', '15'] # Sleep constants allowed
  
  MaxLineLength:
    active: true
    maxLineLength: 120

coroutines:
  GlobalCoroutineUsage:
    active: true  # Fail build if GlobalScope is used

# Custom rules for Clean Architecture
custom-rules:
  - DomainLayerAndroidDependency:
      active: true
      message: "Domain layer cannot import android.* or androidx.*"
```

**ktlint Configuration (.editorconfig):**

```ini
[*.kt]
max_line_length = 120
insert_final_newline = true
indent_size = 4
ij_kotlin_imports_layout = *,java.**,javax.**,kotlin.**,^

# Enforce trailing commas (easier diffs)
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
```

### Code Review Checklist

Before merging any PR, verify:

- [ ] No `!!` (non-null assertion) operator in production code
- [ ] No `TODO` comments without linked issue
- [ ] No `Thread.sleep()` or `runBlocking` in production code
- [ ] All public functions have KDoc comments
- [ ] No hardcoded strings (use string resources)
- [ ] No `lateinit var` in domain layer (use constructor injection)
- [ ] ViewModels use `viewModelScope`, not `GlobalScope`
- [ ] Database operations never run on main thread
- [ ] Compose functions follow naming convention (PascalCase)

---

## Error Handling Strategy

### Result Type

**Use sealed class, not exceptions, for expected failures:**

```kotlin
// domain/common/Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: DomainError) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

sealed class DomainError {
    data class NetworkError(val message: String?) : DomainError()
    data class DatabaseError(val message: String?) : DomainError()
    data class ValidationError(val field: String, val reason: String) : DomainError()
    data class CalculationFailed(val message: String?) : DomainError()
}
```

**ViewModel mapping to UI state:**

```kotlin
// presentation/viewmodels/SleepCalculatorViewModel.kt
data class SleepCalculatorUiState(
    val isLoading: Boolean = false,
    val bedtimeRecommendations: List<BedtimeRecommendation> = emptyList(),
    val errorMessage: String? = null
)

fun Result<List<BedtimeRecommendation>>.toUiState(): SleepCalculatorUiState {
    return when (this) {
        is Result.Loading -> SleepCalculatorUiState(isLoading = true)
        is Result.Success -> SleepCalculatorUiState(bedtimeRecommendations = data)
        is Result.Error -> SleepCalculatorUiState(
            errorMessage = error.toUserFriendlyMessage()
        )
    }
}
```

### Exception Handling Rules

| Scenario | Strategy |
|----------|----------|
| **Network failures** | Return `Result.Error(NetworkError)`, NOT throw |
| **Database corruption** | Catch, log, return `Result.Error(DatabaseError)` |
| **Null from DB** | Return `Result.Error(NotFoundError)`, NOT crash |
| **Invalid input** | Return `Result.Error(ValidationError)` early |
| **Programming errors** | Let crash (IndexOutOfBounds, NPE in dev) |

```kotlin
// ✅ CORRECT: Expected failures return Result
suspend fun getSleepLog(id: String): Result<SleepLog> {
    return try {
        val entity = dao.getSleepLogById(id)
            ?: return Result.Error(DomainError.NotFound("Sleep log $id not found"))
        Result.Success(entity.toDomain())
    } catch (e: Exception) {
        Result.Error(DomainError.DatabaseError(e.message))
    }
}

// ❌ WRONG: Throwing exceptions for expected failures
suspend fun getSleepLog(id: String): SleepLog {
    val entity = dao.getSleepLogById(id)
        ?: throw NotFoundException("Sleep log not found")
    return entity.toDomain()
}
```

---

## Performance Budgets

### Compose Performance

**Recomposition limits:**

- Main screen (Calculator): Max 60 FPS, no dropped frames during input
- History screen: Lazy list should scroll at 60 FPS with 100+ items
- Settings screen: Instant preference updates (<16ms)

**Rules to prevent unnecessary recompositions:**

```kotlin
// ✅ CORRECT: Immutable state, derivedStateOf for calculations
@Composable
fun SleepCalculatorScreen(
    uiState: SleepCalculatorUiState,
    onWakeTimeChanged: (LocalTime) -> Unit
) {
    val formattedBedtimes by remember(uiState.bedtimeRecommendations) {
        derivedStateOf {
            uiState.bedtimeRecommendations.map { it.format() }
        }
    }
    
    // UI code...
}

// ❌ WRONG: Mutable state, inline calculations
@Composable
fun SleepCalculatorScreen() {
    var bedtimes by remember { mutableStateOf(listOf<String>()) }
    
    // Calculation runs every recomposition
    val formatted = bedtimes.map { /* expensive formatting */ }
}
```

### Database Query Limits

- Single log retrieval: <5ms
- History query (30 days): <50ms
- Full sync operation: <500ms for 100 logs

**Indexing requirements:**

```kotlin
@Entity(
    tableName = "sleep_logs",
    indices = [
        Index(value = ["user_id", "timestamp"]),  // Frequent query pattern
        Index(value = ["timestamp"])               // For date range queries
    ]
)
data class SleepLogEntity(...)
```

---

## Firebase Integration Rules

### Offline-First Strategy

**CRITICAL: Room is the source of truth. Firebase is for sync ONLY.**

```kotlin
// ✅ CORRECT: Write to Room first, sync in background
class SleepRepositoryImpl @Inject constructor(
    private val localDao: SleepLogDao,
    private val firestore: FirebaseFirestore,
    private val workManager: WorkManager
) : SleepRepository {
    
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        return try {
            // 1. Write to local database immediately
            localDao.insert(log.toEntity())
            
            // 2. Queue background sync (non-blocking)
            workManager.enqueueUniqueWork(
                "sync_sleep_log_${log.id}",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SleepLogSyncWorker>()
                    .setInputData(workDataOf("log_id" to log.id))
                    .build()
            )
            
            // 3. Return success immediately (sync happens in background)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.DatabaseError(e.message))
        }
    }
    
    override fun getSleepLogs(): Flow<List<SleepLog>> {
        // ONLY read from Room, never from Firestore directly
        return localDao.getAllSleepLogs()
            .map { entities -> entities.map { it.toDomain() } }
    }
}

// ❌ WRONG: Reading from Firebase first
override fun getSleepLogs(): Flow<List<SleepLog>> {
    return flow {
        val firestoreLogs = firestore.collection("sleep_logs").get().await()
        emit(firestoreLogs.toSleepLogs())
    }
}
```

### Sync Conflict Resolution

**Strategy: Last-Write-Wins with Timestamp**

```kotlin
data class SyncableSleepLog(
    val log: SleepLog,
    val lastModified: Instant,
    val syncStatus: SyncStatus
)

enum class SyncStatus {
    SYNCED,         // Local == Remote
    PENDING_UPLOAD, // Local newer than remote
    PENDING_DOWNLOAD, // Remote newer than local
    CONFLICT        // Both modified since last sync
}

// Conflict resolution logic
fun resolveConflict(local: SyncableSleepLog, remote: SyncableSleepLog): SyncableSleepLog {
    return if (local.lastModified > remote.lastModified) {
        local.copy(syncStatus = PENDING_UPLOAD)
    } else {
        remote.copy(syncStatus = PENDING_DOWNLOAD)
    }
}
```

### Network Error Handling

```kotlin
class SleepLogSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val logId = inputData.getString("log_id") ?: return Result.failure()
            syncSleepLog(logId)
            Result.success()
        } catch (e: FirebaseNetworkException) {
            // Retry with exponential backoff
            Result.retry()
        } catch (e: Exception) {
            // Log error, but don't fail (local data is safe)
            Timber.e(e, "Sync failed for log, will retry later")
            Result.failure()
        }
    }
}
```

---

## Violation Detection

### Pre-commit Hooks

```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running architecture checks..."

# Check for Android imports in domain layer
if grep -r "import android\." domain/src/main/kotlin/; then
    echo "❌ VIOLATION: Domain layer contains Android imports"
    exit 1
fi

if grep -r "import androidx\." domain/src/main/kotlin/; then
    echo "❌ VIOLATION: Domain layer contains AndroidX imports"
    exit 1
fi

# Check for direct repository usage in ViewModels
if grep -r "private.*Repository" presentation/src/main/kotlin/.*ViewModel.kt; then
    echo "❌ VIOLATION: ViewModel directly injects Repository (use UseCase instead)"
    exit 1
fi

# Check for !! operator in production code
if grep -r "!!" --include="*.kt" --exclude-dir="test" .; then
    echo "⚠️ WARNING: Non-null assertion (!!) found in production code"
fi

echo "✅ Architecture checks passed"
exit 0
```

### CI Pipeline Checks (GitHub Actions)

```yaml
# .github/workflows/architecture-check.yml
name: Architecture Validation

on: [pull_request]

jobs:
  architecture-lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Check Domain Layer Purity
        run: |
          ./gradlew :domain:dependencies | grep -E "androidx|android\." && exit 1 || echo "✅ Domain layer is pure"
      
      - name: Run Detekt
        run: ./gradlew detekt
      
      - name: Check Test Coverage
        run: |
          ./gradlew koverHtmlReport
          # Fail if domain coverage < 80%
          
      - name: Verify No TODOs without Issues
        run: |
          grep -r "TODO" --include="*.kt" | grep -v "TODO(#[0-9]" && exit 1 || echo "✅ All TODOs are linked"
```

---

## Summary: The 10 Commandments

1. **Domain SHALL NOT depend on Android** - Pure Kotlin only
2. **Room is the source of truth** - Firebase is secondary sync
3. **ViewModels use UseCases only** - No direct repository access
4. **Entities are separate per layer** - Domain, Room, and Firestore entities are distinct
5. **Use Result types, not exceptions** - For expected failures
6. **Test coverage minimums** - 80% domain, 70% data, 60% presentation
7. **No magic numbers** - Extract to constants or domain entities
8. **Offline-first always** - Write local first, sync in background
9. **No nullable late-init** - Use constructor injection
10. **Performance budgets enforced** - 60 FPS, max query times defined

---

**These rules are non-negotiable. Technical debt starts with "just this once."**

---

*Last updated: 2024-01-XX*  
*Enforced by: CI pipeline + pre-commit hooks + code review*

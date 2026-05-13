# Dependency Injection Specification

> Complete Hilt configuration for all layers with proper scoping and test doubles

---

## Table of Contents
1. [Overview](#overview)
2. [Application Setup](#application-setup)
3. [Domain Module](#domain-module)
4. [Data Module](#data-module)
5. [Presentation Module](#presentation-module)
6. [Testing Modules](#testing-modules)
7. [Best Practices](#best-practices)

---

## Overview

### Dependency Injection Strategy

**Framework:** Hilt (built on top of Dagger)

**Key Principles:**
1. **Constructor Injection:** Prefer constructor injection over field injection
2. **Interface Binding:** Inject repository interfaces, not implementations
3. **Singleton Scope:** Use sparingly (only for truly global objects)
4. **ViewModelScope:** Use `@HiltViewModel` for all ViewModels

**Module Organization:**
```
app/
└── di/
    ├── AppModule.kt           # Application-level dependencies
    ├── DatabaseModule.kt      # Room, DataStore
    ├── RepositoryModule.kt    # Repository implementations
    ├── UseCaseModule.kt       # Use cases (if needed)
    └── NetworkModule.kt       # Firebase, WorkManager
```

---

## Application Setup

### SleepOptimizerApplication

```kotlin
package com.sleepoptimizer

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class SleepOptimizerApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Initialize notification channels
        NotificationChannels.createChannels(this)
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
```

---

## Domain Module

### UseCases (No Module Needed)

**Note:** UseCases with constructor injection don't need a module - Hilt can provide them automatically.

```kotlin
// domain/usecases/CalculateOptimalBedtimeUseCase.kt
class CalculateOptimalBedtimeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(wakeUpTime: LocalTime): Result<List<BedtimeRecommendation>> {
        // Implementation...
    }
}

// No module needed! Hilt automatically provides this.
```

**Why no module?** Because:
1. Constructor has `@Inject`
2. All parameters are interfaces that Hilt knows how to provide
3. No special scoping needed

---

## Data Module

### 1. DatabaseModule

```kotlin
package com.sleepoptimizer.di

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.sleepoptimizer.data.local.room.SleepDatabase
import com.sleepoptimizer.data.local.room.dao.SleepLogDao
import com.sleepoptimizer.data.local.room.dao.UserProfileDao
import com.sleepoptimizer.data.local.datastore.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideSleepDatabase(
        @ApplicationContext context: Context
    ): SleepDatabase {
        return Room.databaseBuilder(
            context,
            SleepDatabase::class.java,
            SleepDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // TODO: Replace with proper migrations in production
            .build()
    }
    
    @Provides
    fun provideSleepLogDao(database: SleepDatabase): SleepLogDao {
        return database.sleepLogDao()
    }
    
    @Provides
    fun provideUserProfileDao(database: SleepDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    // DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences"
    )
    
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        dataStore: DataStore<Preferences>
    ): UserPreferencesDataStore {
        return UserPreferencesDataStore(dataStore)
    }
}
```

---

### 2. RepositoryModule

```kotlin
package com.sleepoptimizer.di

import com.sleepoptimizer.data.repositories.SleepRepositoryImpl
import com.sleepoptimizer.data.repositories.UserPreferencesRepositoryImpl
import com.sleepoptimizer.data.repositories.AlarmRepositoryImpl
import com.sleepoptimizer.domain.repositories.SleepRepository
import com.sleepoptimizer.domain.repositories.UserPreferencesRepository
import com.sleepoptimizer.domain.repositories.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSleepRepository(
        impl: SleepRepositoryImpl
    ): SleepRepository
    
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
    
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl
    ): AlarmRepository
}
```

**Why `@Binds` instead of `@Provides`?**
- `@Binds` is more efficient (generates less code)
- Use for simple interface → implementation mappings
- Use `@Provides` when you need custom instantiation logic

---

### 3. NetworkModule (Firebase & WorkManager)

```kotlin
package com.sleepoptimizer.di

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        
        // Configure Firestore settings
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Enable offline persistence
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        
        firestore.firestoreSettings = settings
        return firestore
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
```

---

### 4. NotificationModule

```kotlin
package com.sleepoptimizer.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    fun provideAlarmManager(
        @ApplicationContext context: Context
    ): AlarmManager {
        return context.getSystemService()
            ?: throw IllegalStateException("AlarmManager not available")
    }
    
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService()
            ?: throw IllegalStateException("NotificationManager not available")
    }
}
```

---

## Presentation Module

### ViewModels (No Module Needed)

**ViewModels use `@HiltViewModel` annotation:**

```kotlin
package com.sleepoptimizer.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepoptimizer.domain.usecases.CalculateOptimalBedtimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepCalculatorViewModel @Inject constructor(
    private val calculateBedtimeUseCase: CalculateOptimalBedtimeUseCase,
    private val logSleepSessionUseCase: LogSleepSessionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SleepCalculatorUiState())
    val uiState: StateFlow<SleepCalculatorUiState> = _uiState
    
    fun calculateBedtimes(wakeUpTime: LocalTime) {
        viewModelScope.launch {
            val result = calculateBedtimeUseCase(wakeUpTime)
            _uiState.value = result.toUiState()
        }
    }
}
```

**In Activity/Fragment:**
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: SleepCalculatorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewModel automatically injected
    }
}
```

---

### WorkManager Integration

```kotlin
package com.sleepoptimizer.data.remote.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sleepoptimizer.domain.repositories.SleepRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SleepDataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sleepRepository: SleepRepository // Injected by Hilt
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Sync logic...
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

**Note:** Use `@HiltWorker` + `@AssistedInject` for Workers.

---

## Testing Modules

### 1. Test Fakes (No Hilt Needed)

**Fake repositories for unit tests:**

```kotlin
package com.sleepoptimizer.domain.repositories.fakes

class FakeSleepRepository : SleepRepository {
    private val logs = mutableListOf<SleepLog>()
    
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        logs.add(log)
        return Result.Success(Unit)
    }
    
    override fun getAllSleepLogs(): Flow<List<SleepLog>> {
        return flowOf(logs)
    }
    
    // ... other methods
    
    // Test helpers
    fun setLogs(newLogs: List<SleepLog>) {
        logs.clear()
        logs.addAll(newLogs)
    }
}
```

**Usage in tests:**
```kotlin
class CalculateOptimalBedtimeUseCaseTest {
    
    private lateinit var useCase: CalculateOptimalBedtimeUseCase
    private val fakePreferencesRepo = FakeUserPreferencesRepository()
    
    @Before
    fun setup() {
        // No Hilt needed - manually construct
        useCase = CalculateOptimalBedtimeUseCase(fakePreferencesRepo)
    }
    
    @Test
    fun `test bedtime calculation`() = runTest {
        // Test...
    }
}
```

---

### 2. Integration Tests with Hilt

**For testing ViewModels and repositories together:**

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SleepCalculatorViewModelTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var calculateBedtimeUseCase: CalculateOptimalBedtimeUseCase
    
    private lateinit var viewModel: SleepCalculatorViewModel
    
    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = SleepCalculatorViewModel(calculateBedtimeUseCase, ...)
    }
    
    @Test
    fun `ViewModel calculates bedtimes correctly`() = runTest {
        // Test...
    }
}
```

---

### 3. Replacing Modules for Tests

**Custom test modules:**

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object FakeNetworkModule {
    
    @Provides
    @Singleton
    fun provideFakeFirebaseAuth(): FirebaseAuth {
        return mockk<FirebaseAuth>()
    }
    
    @Provides
    @Singleton
    fun provideFakeFirestore(): FirebaseFirestore {
        return mockk<FirebaseFirestore>()
    }
}
```

---

## Best Practices

### 1. Scope Guidelines

| Scope | Use For | Lifespan |
|-------|---------|----------|
| `@Singleton` | Database, DataStore, Firebase | App lifetime |
| `@ViewModelScoped` | ViewModels | ViewModel lifetime |
| `@ActivityScoped` | Per-activity dependencies | Activity lifetime |
| Unscoped (default) | Use cases, short-lived objects | Per-injection |

---

### 2. Constructor Injection vs Field Injection

**✅ PREFER Constructor Injection:**
```kotlin
class CalculateOptimalBedtimeUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    // ...
}
```

**❌ AVOID Field Injection:**
```kotlin
class BadExample {
    @Inject lateinit var repository: SleepRepository // Hard to test
}
```

**Why?**
- Constructor injection makes dependencies explicit
- Easier to test (no Hilt needed in unit tests)
- Compile-time safety (no lateinit)

---

### 3. Interface Injection

**✅ CORRECT: Inject interfaces:**
```kotlin
class SleepCalculatorViewModel @Inject constructor(
    private val sleepRepository: SleepRepository // Interface
) : ViewModel()
```

**❌ WRONG: Inject implementations:**
```kotlin
class BadViewModel @Inject constructor(
    private val sleepRepository: SleepRepositoryImpl // Implementation
) : ViewModel()
```

---

### 4. Avoid Circular Dependencies

**❌ BAD:**
```kotlin
class A @Inject constructor(private val b: B)
class B @Inject constructor(private val a: A) // Circular!
```

**✅ GOOD: Extract shared dependency:**
```kotlin
class A @Inject constructor(private val shared: SharedDependency)
class B @Inject constructor(private val shared: SharedDependency)
```

---

### 5. Use Qualifiers When Needed

**When you need multiple instances of the same type:**

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
    
    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher {
        return Dispatchers.Main
    }
}

// Usage
class SomeRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchData() = withContext(ioDispatcher) {
        // ...
    }
}
```

---

## Common Issues & Solutions

### Issue 1: "Cannot find symbol" errors

**Cause:** Missing `kapt` plugin or annotation processor

**Solution:**
```gradle
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}

dependencies {
    ksp("com.google.dagger:hilt-compiler:2.48")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
}
```

---

### Issue 2: Workers not injected

**Cause:** Missing HiltWorkerFactory configuration

**Solution:**
```kotlin
@HiltAndroidApp
class SleepOptimizerApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
```

---

### Issue 3: ViewModel not found

**Cause:** Missing `@HiltViewModel` or `@AndroidEntryPoint`

**Solution:**
```kotlin
@HiltViewModel // Must have this
class MyViewModel @Inject constructor(...) : ViewModel()

@AndroidEntryPoint // Must have this
class MyActivity : ComponentActivity()
```

---

## Dependency Graph Overview

```
Application
    ├─ SleepDatabase (@Singleton)
    ├─ DataStore (@Singleton)
    ├─ FirebaseAuth (@Singleton)
    ├─ FirebaseFirestore (@Singleton)
    ├─ WorkManager (@Singleton)
    │
    ├─ Repositories (@Singleton)
    │   ├─ SleepRepositoryImpl
    │   ├─ UserPreferencesRepositoryImpl
    │   └─ AlarmRepositoryImpl
    │
    ├─ UseCases (Unscoped - created per injection)
    │   ├─ CalculateOptimalBedtimeUseCase
    │   ├─ LogSleepSessionUseCase
    │   └─ StartDiscoveryPhaseUseCase
    │
    └─ ViewModels (@ViewModelScoped)
        ├─ SleepCalculatorViewModel
        ├─ HistoryViewModel
        └─ SettingsViewModel
```

---

## Build.gradle Setup

```gradle
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    
    // Hilt for WorkManager
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    
    // Hilt for ViewModel
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Testing
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspTest("com.google.dagger:hilt-compiler:2.48")
    
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.48")
}
```

---

*This specification ensures clean, testable dependency injection throughout the app using Hilt best practices.*

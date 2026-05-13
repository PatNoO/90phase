# Data Layer Specification

> Complete specification of Room database schema, DAOs, DataStore preferences, and entity mappers

---

## Table of Contents
1. [Overview](#overview)
2. [Room Database](#room-database)
3. [Data Access Objects (DAOs)](#data-access-objects-daos)
4. [DataStore Preferences](#datastore-preferences)
5. [Entity Mappers](#entity-mappers)
6. [Migration Strategy](#migration-strategy)
7. [Testing Strategy](#testing-strategy)

---

## Overview

### Data Layer Principles

**CRITICAL:** Room is the **source of truth**. Firebase is a secondary sync mechanism.

**Read Path:** ALWAYS read from Room first (offline-first)  
**Write Path:** Write to Room immediately, queue Firebase sync in background

**Package Structure:**
```
data/
├── local/
│   ├── room/
│   │   ├── SleepDatabase.kt
│   │   ├── entities/
│   │   │   ├── SleepLogEntity.kt
│   │   │   └── UserProfileEntity.kt
│   │   └── dao/
│   │       ├── SleepLogDao.kt
│   │       └── UserProfileDao.kt
│   └── datastore/
│       └── UserPreferencesDataStore.kt
├── remote/
│   ├── firebase/
│   │   ├── FirestoreSyncManager.kt
│   │   └── models/
│   │       ├── SleepLogDto.kt
│   │       └── UserProfileDto.kt
│   └── sync/
│       └── SleepDataSyncWorker.kt
└── repositories/
    ├── SleepRepositoryImpl.kt
    ├── UserPreferencesRepositoryImpl.kt
    ├── AlarmRepositoryImpl.kt
    └── mappers/
        ├── SleepLogMapper.kt
        └── UserProfileMapper.kt
```

---

## Room Database

### Database Configuration

```kotlin
package com.sleepoptimizer.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        SleepLogEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        const val DATABASE_NAME = "sleep_optimizer.db"
    }
}
```

### Type Converters

```kotlin
package com.sleepoptimizer.data.local.room

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }
    
    @TypeConverter
    fun toTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
    
    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun toLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
}
```

---

## Room Entities

### 1. SleepLogEntity

```kotlin
package com.sleepoptimizer.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "sleep_logs",
    indices = [
        Index(value = ["target_wake_time"], name = "idx_target_wake_time"),
        Index(value = ["created_at"], name = "idx_created_at"),
        Index(value = ["wake_up_rating"], name = "idx_wake_up_rating"),
        Index(value = ["sync_status"], name = "idx_sync_status")
    ]
)
data class SleepLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "target_wake_time")
    val targetWakeTime: Long, // Instant as epoch millis
    
    @ColumnInfo(name = "recommended_bedtime")
    val recommendedBedtime: Long?, // Instant as epoch millis
    
    @ColumnInfo(name = "actual_bedtime")
    val actualBedtime: Long?, // Instant as epoch millis
    
    @ColumnInfo(name = "actual_wake_time")
    val actualWakeTime: Long?, // Instant as epoch millis
    
    @ColumnInfo(name = "wake_up_rating")
    val wakeUpRating: Int?, // 1-5, nullable
    
    @ColumnInfo(name = "cycle_count")
    val cycleCount: Int,
    
    @ColumnInfo(name = "cycle_duration_used")
    val cycleDurationUsed: Int,
    
    @ColumnInfo(name = "sleep_latency_used")
    val sleepLatencyUsed: Int,
    
    @ColumnInfo(name = "notes")
    val notes: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Instant as epoch millis
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long, // Instant as epoch millis
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String // "SYNCED", "PENDING_UPLOAD", etc.
)
```

**SQL Schema (Generated):**
```sql
CREATE TABLE sleep_logs (
    id TEXT PRIMARY KEY NOT NULL,
    target_wake_time INTEGER NOT NULL,
    recommended_bedtime INTEGER,
    actual_bedtime INTEGER,
    actual_wake_time INTEGER,
    wake_up_rating INTEGER CHECK(wake_up_rating IS NULL OR (wake_up_rating >= 1 AND wake_up_rating <= 5)),
    cycle_count INTEGER NOT NULL,
    cycle_duration_used INTEGER NOT NULL,
    sleep_latency_used INTEGER NOT NULL,
    notes TEXT,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    sync_status TEXT NOT NULL DEFAULT 'PENDING_UPLOAD'
);

CREATE INDEX idx_target_wake_time ON sleep_logs(target_wake_time DESC);
CREATE INDEX idx_created_at ON sleep_logs(created_at DESC);
CREATE INDEX idx_wake_up_rating ON sleep_logs(wake_up_rating);
CREATE INDEX idx_sync_status ON sleep_logs(sync_status);
```

---

### 2. UserProfileEntity

```kotlin
package com.sleepoptimizer.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "email")
    val email: String?,
    
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    
    @ColumnInfo(name = "optimal_cycle_minutes")
    val optimalCycleMinutes: Int,
    
    @ColumnInfo(name = "sleep_latency_minutes")
    val sleepLatencyMinutes: Int,
    
    @ColumnInfo(name = "reminder_time")
    val reminderTime: String, // HH:mm format
    
    @ColumnInfo(name = "notifications_enabled")
    val notificationsEnabled: Boolean,
    
    // Discovery Phase (stored as JSON string)
    @ColumnInfo(name = "discovery_phase_json")
    val discoveryPhaseJson: String? // Serialized DiscoveryPhase
)
```

**SQL Schema (Generated):**
```sql
CREATE TABLE user_profile (
    user_id TEXT PRIMARY KEY NOT NULL,
    email TEXT,
    display_name TEXT,
    optimal_cycle_minutes INTEGER NOT NULL DEFAULT 90,
    sleep_latency_minutes INTEGER NOT NULL DEFAULT 15,
    reminder_time TEXT NOT NULL DEFAULT '18:00',
    notifications_enabled INTEGER NOT NULL DEFAULT 1,
    discovery_phase_json TEXT
);
```

---

## Data Access Objects (DAOs)

### 1. SleepLogDao

```kotlin
package com.sleepoptimizer.data.local.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {
    
    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: SleepLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<SleepLogEntity>)
    
    // READ
    @Query("SELECT * FROM sleep_logs ORDER BY created_at DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>
    
    @Query("SELECT * FROM sleep_logs WHERE id = :id")
    fun getSleepLogById(id: String): Flow<SleepLogEntity?>
    
    @Query("""
        SELECT * FROM sleep_logs 
        WHERE target_wake_time >= :startTime 
        AND target_wake_time <= :endTime 
        ORDER BY target_wake_time DESC
    """)
    fun getSleepLogsByDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<SleepLogEntity>>
    
    @Query("""
        SELECT * FROM sleep_logs 
        WHERE wake_up_rating IS NOT NULL 
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    suspend fun getRatedLogs(limit: Int = 30): List<SleepLogEntity>
    
    @Query("""
        SELECT * FROM sleep_logs 
        WHERE sync_status = 'PENDING_UPLOAD' 
        ORDER BY updated_at ASC
    """)
    suspend fun getPendingUploadLogs(): List<SleepLogEntity>
    
    @Query("""
        SELECT AVG(wake_up_rating) FROM sleep_logs 
        WHERE wake_up_rating IS NOT NULL 
        AND created_at >= :startTime 
        AND created_at <= :endTime
    """)
    suspend fun getAverageRating(startTime: Long, endTime: Long): Double?
    
    @Query("SELECT COUNT(*) FROM sleep_logs WHERE wake_up_rating IS NOT NULL")
    suspend fun getRatedLogCount(): Int
    
    // UPDATE
    @Update
    suspend fun update(log: SleepLogEntity)
    
    @Query("UPDATE sleep_logs SET wake_up_rating = :rating, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateRating(id: String, rating: Int, updatedAt: Long)
    
    @Query("UPDATE sleep_logs SET sync_status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, updatedAt: Long)
    
    @Query("""
        UPDATE sleep_logs 
        SET actual_bedtime = :actualBedtime, 
            actual_wake_time = :actualWakeTime, 
            updated_at = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateActualTimes(
        id: String,
        actualBedtime: Long?,
        actualWakeTime: Long?,
        updatedAt: Long
    )
    
    // DELETE
    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM sleep_logs")
    suspend fun deleteAll()
    
    // SYNC HELPERS
    @Query("SELECT MAX(updated_at) FROM sleep_logs")
    suspend fun getLastUpdatedTimestamp(): Long?
}
```

**Key Query Patterns:**

1. **Recent Logs with Ratings** (for Discovery Phase analysis):
```kotlin
@Query("""
    SELECT * FROM sleep_logs 
    WHERE wake_up_rating IS NOT NULL 
    AND created_at >= :startDate 
    ORDER BY created_at DESC
""")
suspend fun getLogsForAnalysis(startDate: Long): List<SleepLogEntity>
```

2. **Logs Needing Sync**:
```kotlin
@Query("SELECT * FROM sleep_logs WHERE sync_status != 'SYNCED'")
suspend fun getUnsyncedLogs(): List<SleepLogEntity>
```

---

### 2. UserProfileDao

```kotlin
package com.sleepoptimizer.data.local.room.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    // CREATE/UPDATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)
    
    @Update
    suspend fun update(profile: UserProfileEntity)
    
    // READ
    @Query("SELECT * FROM user_profile WHERE user_id = :userId")
    fun getProfile(userId: String): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profile WHERE user_id = :userId")
    suspend fun getProfileOnce(userId: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getAnyProfile(): UserProfileEntity?
    
    // UPDATE SPECIFIC FIELDS
    @Query("UPDATE user_profile SET optimal_cycle_minutes = :minutes WHERE user_id = :userId")
    suspend fun updateCycleDuration(userId: String, minutes: Int)
    
    @Query("UPDATE user_profile SET sleep_latency_minutes = :minutes WHERE user_id = :userId")
    suspend fun updateSleepLatency(userId: String, minutes: Int)
    
    @Query("UPDATE user_profile SET reminder_time = :time WHERE user_id = :userId")
    suspend fun updateReminderTime(userId: String, time: String)
    
    @Query("UPDATE user_profile SET notifications_enabled = :enabled WHERE user_id = :userId")
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean)
    
    @Query("UPDATE user_profile SET discovery_phase_json = :json WHERE user_id = :userId")
    suspend fun updateDiscoveryPhase(userId: String, json: String?)
    
    // DELETE
    @Query("DELETE FROM user_profile WHERE user_id = :userId")
    suspend fun delete(userId: String)
}
```

---

## DataStore Preferences

### UserPreferencesDataStore

**Purpose:** Store small, frequently accessed preferences using Proto DataStore.

```kotlin
package com.sleepoptimizer.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIREBASE_SYNC_ENABLED = booleanPreferencesKey("firebase_sync_enabled")
    }
    
    // User ID
    suspend fun setUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }
    
    fun getUserId(): Flow<String?> {
        return dataStore.data.map { prefs ->
            prefs[USER_ID]
        }
    }
    
    suspend fun getUserIdOnce(): String? {
        return dataStore.data.map { it[USER_ID] }.first()
    }
    
    // Sync Timestamp
    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
    
    fun getLastSyncTimestamp(): Flow<Long> {
        return dataStore.data.map { prefs ->
            prefs[LAST_SYNC_TIMESTAMP] ?: 0L
        }
    }
    
    // Onboarding
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }
    
    fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[ONBOARDING_COMPLETED] ?: false
        }
    }
    
    // Firebase Sync
    suspend fun setFirebaseSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[FIREBASE_SYNC_ENABLED] = enabled
        }
    }
    
    fun isFirebaseSyncEnabled(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[FIREBASE_SYNC_ENABLED] ?: false
        }
    }
    
    // Clear all
    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
```

**Initialization (in Hilt module):**
```kotlin
@Singleton
@Provides
fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        produceFile = { context.dataStoreFile("user_preferences.preferences_pb") }
    )
}
```

---

## Entity Mappers

### 1. SleepLogMapper

```kotlin
package com.sleepoptimizer.data.repositories.mappers

import com.sleepoptimizer.data.local.room.entities.SleepLogEntity
import com.sleepoptimizer.domain.entities.SleepLog
import com.sleepoptimizer.domain.entities.SyncStatus
import java.time.Instant

// Entity -> Domain
fun SleepLogEntity.toDomain(): SleepLog {
    return SleepLog(
        id = id,
        targetWakeTime = Instant.ofEpochMilli(targetWakeTime),
        recommendedBedtime = recommendedBedtime?.let { Instant.ofEpochMilli(it) },
        actualBedtime = actualBedtime?.let { Instant.ofEpochMilli(it) },
        actualWakeTime = actualWakeTime?.let { Instant.ofEpochMilli(it) },
        wakeUpRating = wakeUpRating,
        cycleCount = cycleCount,
        cycleDurationUsed = cycleDurationUsed,
        sleepLatencyUsed = sleepLatencyUsed,
        notes = notes,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(updatedAt),
        syncStatus = SyncStatus.valueOf(syncStatus)
    )
}

// Domain -> Entity
fun SleepLog.toEntity(): SleepLogEntity {
    return SleepLogEntity(
        id = id,
        targetWakeTime = targetWakeTime.toEpochMilli(),
        recommendedBedtime = recommendedBedtime?.toEpochMilli(),
        actualBedtime = actualBedtime?.toEpochMilli(),
        actualWakeTime = actualWakeTime?.toEpochMilli(),
        wakeUpRating = wakeUpRating,
        cycleCount = cycleCount,
        cycleDurationUsed = cycleDurationUsed,
        sleepLatencyUsed = sleepLatencyUsed,
        notes = notes,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
        syncStatus = syncStatus.name
    )
}

// List extensions
fun List<SleepLogEntity>.toDomain(): List<SleepLog> {
    return map { it.toDomain() }
}

fun List<SleepLog>.toEntity(): List<SleepLogEntity> {
    return map { it.toEntity() }
}
```

---

### 2. UserProfileMapper

```kotlin
package com.sleepoptimizer.data.repositories.mappers

import com.sleepoptimizer.data.local.room.entities.UserProfileEntity
import com.sleepoptimizer.domain.entities.UserProfile
import com.sleepoptimizer.domain.entities.DiscoveryPhase
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// Entity -> Domain
fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        userId = userId,
        email = email,
        displayName = displayName,
        optimalCycleMinutes = optimalCycleMinutes,
        sleepLatencyMinutes = sleepLatencyMinutes,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        discoveryPhase = discoveryPhaseJson?.let { 
            Json.decodeFromString<DiscoveryPhase>(it) 
        }
    )
}

// Domain -> Entity
fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = userId,
        email = email,
        displayName = displayName,
        optimalCycleMinutes = optimalCycleMinutes,
        sleepLatencyMinutes = sleepLatencyMinutes,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        discoveryPhaseJson = discoveryPhase?.let { 
            Json.encodeToString(it) 
        }
    )
}
```

**Serialization Dependencies:**
```gradle
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

---

## Repository Implementation Example

### SleepRepositoryImpl

```kotlin
package com.sleepoptimizer.data.repositories

import com.sleepoptimizer.data.local.room.dao.SleepLogDao
import com.sleepoptimizer.data.repositories.mappers.*
import com.sleepoptimizer.domain.entities.SleepLog
import com.sleepoptimizer.domain.repositories.SleepRepository
import com.sleepoptimizer.domain.common.Result
import com.sleepoptimizer.domain.common.DomainError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SleepRepositoryImpl @Inject constructor(
    private val sleepLogDao: SleepLogDao,
    private val workManager: WorkManager
) : SleepRepository {
    
    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
        return try {
            // 1. Write to Room immediately
            sleepLogDao.insert(log.toEntity())
            
            // 2. Queue background sync (non-blocking)
            workManager.enqueueUniqueWork(
                "sync_sleep_log_${log.id}",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SleepLogSyncWorker>()
                    .setInputData(workDataOf("log_id" to log.id))
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            )
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.DatabaseError(e.message))
        }
    }
    
    override fun getAllSleepLogs(): Flow<List<SleepLog>> {
        return sleepLogDao.getAllSleepLogs()
            .map { entities -> entities.toDomain() }
    }
    
    override fun getSleepLog(id: String): Flow<SleepLog?> {
        return sleepLogDao.getSleepLogById(id)
            .map { entity -> entity?.toDomain() }
    }
    
    override suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus
    ): Result<Unit> {
        return try {
            sleepLogDao.updateSyncStatus(
                id = id,
                status = status.name,
                updatedAt = Instant.now().toEpochMilli()
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DomainError.DatabaseError(e.message))
        }
    }
    
    override suspend fun getPendingUploadLogs(): List<SleepLog> {
        return sleepLogDao.getPendingUploadLogs().toDomain()
    }
}
```

---

## Migration Strategy

### Version 1 → Version 2 (Example)

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add a new column
        database.execSQL(
            "ALTER TABLE sleep_logs ADD COLUMN user_id TEXT DEFAULT 'local'"
        )
        
        // Create a new index
        database.execSQL(
            "CREATE INDEX idx_user_id ON sleep_logs(user_id)"
        )
    }
}

// In Database builder
Room.databaseBuilder(context, SleepDatabase::class.java, DATABASE_NAME)
    .addMigrations(MIGRATION_1_2)
    .build()
```

### Migration Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SleepDatabase::class.java
    )
    
    @Test
    fun migrate1To2() {
        // Create database v1
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("INSERT INTO sleep_logs (id, target_wake_time, ...) VALUES (...)")
            close()
        }
        
        // Migrate to v2
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        
        // Verify migration
        helper.openDatabase(TEST_DB).use { db ->
            val cursor = db.query("SELECT user_id FROM sleep_logs")
            cursor.moveToFirst()
            assertThat(cursor.getString(0)).isEqualTo("local")
        }
    }
}
```

---

## Testing Strategy

### In-Memory Database Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class SleepLogDaoTest {
    
    private lateinit var database: SleepDatabase
    private lateinit var dao: SleepLogDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SleepDatabase::class.java)
            .allowMainThreadQueries()
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
        val log = SleepLogEntity(
            id = "test-1",
            targetWakeTime = Instant.now().toEpochMilli(),
            cycleCount = 6,
            cycleDurationUsed = 90,
            sleepLatencyUsed = 15,
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli(),
            syncStatus = "PENDING_UPLOAD",
            recommendedBedtime = null,
            actualBedtime = null,
            actualWakeTime = null,
            wakeUpRating = null,
            notes = null
        )
        
        // When
        dao.insert(log)
        val retrieved = dao.getSleepLogById("test-1").first()
        
        // Then
        assertThat(retrieved).isEqualTo(log)
    }
    
    @Test
    fun getRatedLogsReturnsOnlyLogsWithRatings() = runTest {
        // Given
        dao.insert(createLogWithRating(4))
        dao.insert(createLogWithRating(null))
        dao.insert(createLogWithRating(5))
        
        // When
        val ratedLogs = dao.getRatedLogs(limit = 10)
        
        // Then
        assertThat(ratedLogs).hasSize(2)
        assertThat(ratedLogs.all { it.wakeUpRating != null }).isTrue()
    }
}
```

---

## Performance Considerations

### Index Strategy

**MUST have indices on:**
- `target_wake_time` (DESC) - For chronological queries
- `created_at` (DESC) - For recent logs
- `wake_up_rating` - For Discovery Phase analysis
- `sync_status` - For finding unsynced logs

**Optional indices:**
- `user_id` (if multi-user support added later)
- Composite index on `(wake_up_rating, created_at)` if frequent analysis queries

### Query Optimization

```kotlin
// ❌ BAD: Fetching all logs then filtering in memory
val allLogs = dao.getAllSleepLogs().first()
val ratedLogs = allLogs.filter { it.wakeUpRating != null }

// ✅ GOOD: Let SQLite filter
val ratedLogs = dao.getRatedLogs(limit = 30)
```

---

## Implementation Checklist

Before starting implementation:

- [ ] All Room entities defined with proper indices
- [ ] All DAO queries tested with in-memory database
- [ ] DataStore keys documented
- [ ] Mappers tested bidirectionally (Entity ↔ Domain)
- [ ] Migration strategy planned for schema changes
- [ ] Repository tests use Fakes, not Mocks

---

*This specification ensures the data layer provides fast, reliable offline-first storage with seamless background synchronization.*

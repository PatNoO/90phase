# Firebase Integration & Data Schema

> Complete specification for Firebase Authentication, Firestore data structure, and background sync strategy

---

## Table of Contents
1. [Firebase Authentication](#firebase-authentication)
2. [Firestore Schema](#firestore-schema)
3. [Sync Strategy](#sync-strategy)
4. [Conflict Resolution](#conflict-resolution)
5. [Security Rules](#security-rules)
6. [Offline Behavior](#offline-behavior)

---

## Firebase Authentication

### Setup

**Authentication Method:** Email/Password (primary), Google Sign-In (optional for Phase 2)

```kotlin
// domain/entities/User.kt
data class User(
    val userId: String,
    val email: String,
    val displayName: String?,
    val createdAt: Instant,
    val lastSyncedAt: Instant?
)
```

### Sign-up Flow
```
[User opens app for first time]
           ↓
    [Optional: Sign in]
           ↓
    ┌──────────────────────────────────┐
    │ Vill du synka din data till      │
    │ molnet? (Valfritt)               │
    │                                  │
    │ ✓ Backup av sömndata             │
    │ ✓ Synk mellan enheter            │
    │                                  │
    │ [Skapa konto]  [Fortsätt lokalt] │
    └──────────────────────────────────┘
           ↓
    [If "Skapa konto"]
           ↓
    [Firebase createUserWithEmailAndPassword]
           ↓
    [Store userId in DataStore]
           ↓
    [Trigger initial sync]
```

**Implementation:**
```kotlin
// data/remote/firebase/FirebaseAuthManager.kt
class FirebaseAuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.Error(DomainError.AuthFailed)
            
            Result.Success(
                User(
                    userId = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    createdAt = Instant.now(),
                    lastSyncedAt = null
                )
            )
        } catch (e: FirebaseAuthException) {
            Result.Error(DomainError.AuthFailed(e.message))
        }
    }
    
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            createdAt = Instant.now(), // Would be fetched from Firestore in real impl
            lastSyncedAt = null
        )
    }
}
```

---

## Firestore Schema

### Collection Structure

```
users/
  {userId}/
    profile/
      - userId: String
      - email: String
      - displayName: String?
      - preferences: Map<String, Any>
          - cycleDurationMinutes: Int (default: 90)
          - sleepLatencyMinutes: Int (default: 15)
          - reminderTime: String (default: "18:00")
          - notificationsEnabled: Boolean
      - discoveryPhase: Map<String, Any>?
          - isActive: Boolean
          - currentShift: String ("LONGER_LATENCY" | "LONGER_CYCLES" | "FEWER_CYCLES")
          - startDate: Timestamp
          - endDate: Timestamp
      - createdAt: Timestamp
      - updatedAt: Timestamp
    
    sleep_logs/
      {logId}/
        - id: String (UUID)
        - targetWakeTime: Timestamp
        - recommendedBedtime: Timestamp
        - actualBedtime: Timestamp?
        - actualWakeTime: Timestamp?
        - wakeUpRating: Int? (1-5)
        - cycleCount: Int
        - cycleDurationUsed: Int
        - sleepLatencyUsed: Int
        - notes: String?
        - createdAt: Timestamp
        - updatedAt: Timestamp
        - syncStatus: String ("SYNCED" | "PENDING_UPLOAD" | "CONFLICT")
```

### Data Transfer Objects (DTOs)

```kotlin
// data/remote/firebase/models/UserProfileDto.kt
data class UserProfileDto(
    val userId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val preferences: Map<String, Any>? = null,
    val discoveryPhase: Map<String, Any>? = null,
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null
) {
    companion object {
        fun from(profile: UserProfile, userId: String): UserProfileDto {
            return UserProfileDto(
                userId = userId,
                email = profile.email,
                displayName = profile.displayName,
                preferences = mapOf(
                    "cycleDurationMinutes" to profile.optimalCycleMinutes,
                    "sleepLatencyMinutes" to profile.sleepLatencyMinutes,
                    "reminderTime" to profile.reminderTime,
                    "notificationsEnabled" to profile.notificationsEnabled
                ),
                discoveryPhase = profile.discoveryPhase?.let {
                    mapOf(
                        "isActive" to it.isActive,
                        "currentShift" to it.currentShift.name,
                        "startDate" to com.google.firebase.Timestamp(it.startDate.atStartOfDay().toEpochSecond(), 0),
                        "endDate" to com.google.firebase.Timestamp(it.endDate.atStartOfDay().toEpochSecond(), 0)
                    )
                },
                createdAt = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )
        }
    }
    
    fun toDomain(): UserProfile {
        val prefs = preferences ?: emptyMap()
        return UserProfile(
            userId = userId ?: "",
            email = email ?: "",
            displayName = displayName,
            optimalCycleMinutes = (prefs["cycleDurationMinutes"] as? Long)?.toInt() ?: 90,
            sleepLatencyMinutes = (prefs["sleepLatencyMinutes"] as? Long)?.toInt() ?: 15,
            reminderTime = prefs["reminderTime"] as? String ?: "18:00",
            notificationsEnabled = prefs["notificationsEnabled"] as? Boolean ?: true,
            discoveryPhase = discoveryPhase?.let { parseDiscoveryPhase(it) }
        )
    }
}

// data/remote/firebase/models/SleepLogDto.kt
data class SleepLogDto(
    val id: String? = null,
    val targetWakeTime: com.google.firebase.Timestamp? = null,
    val recommendedBedtime: com.google.firebase.Timestamp? = null,
    val actualBedtime: com.google.firebase.Timestamp? = null,
    val actualWakeTime: com.google.firebase.Timestamp? = null,
    val wakeUpRating: Int? = null,
    val cycleCount: Int? = null,
    val cycleDurationUsed: Int? = null,
    val sleepLatencyUsed: Int? = null,
    val notes: String? = null,
    val createdAt: com.google.firebase.Timestamp? = null,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val syncStatus: String? = null
) {
    companion object {
        fun from(log: SleepLog): SleepLogDto {
            return SleepLogDto(
                id = log.id,
                targetWakeTime = log.targetWakeTime.toFirebaseTimestamp(),
                recommendedBedtime = log.recommendedBedtime?.toFirebaseTimestamp(),
                actualBedtime = log.actualBedtime?.toFirebaseTimestamp(),
                actualWakeTime = log.actualWakeTime?.toFirebaseTimestamp(),
                wakeUpRating = log.wakeUpRating,
                cycleCount = log.cycleCount,
                cycleDurationUsed = log.cycleDurationUsed,
                sleepLatencyUsed = log.sleepLatencyUsed,
                notes = log.notes,
                createdAt = log.createdAt.toFirebaseTimestamp(),
                updatedAt = log.updatedAt.toFirebaseTimestamp(),
                syncStatus = log.syncStatus.name
            )
        }
    }
    
    fun toDomain(): SleepLog {
        return SleepLog(
            id = id ?: UUID.randomUUID().toString(),
            targetWakeTime = targetWakeTime?.toInstant() ?: Instant.now(),
            recommendedBedtime = recommendedBedtime?.toInstant(),
            actualBedtime = actualBedtime?.toInstant(),
            actualWakeTime = actualWakeTime?.toInstant(),
            wakeUpRating = wakeUpRating,
            cycleCount = cycleCount ?: 6,
            cycleDurationUsed = cycleDurationUsed ?: 90,
            sleepLatencyUsed = sleepLatencyUsed ?: 15,
            notes = notes,
            createdAt = createdAt?.toInstant() ?: Instant.now(),
            updatedAt = updatedAt?.toInstant() ?: Instant.now(),
            syncStatus = SyncStatus.valueOf(syncStatus ?: "PENDING_UPLOAD")
        )
    }
}
```

---

## Sync Strategy

### Background Sync with WorkManager

**Sync Triggers:**
1. **Periodic Sync:** Every 6 hours (when network available)
2. **Immediate Sync:** After user creates/updates sleep log
3. **Manual Sync:** User pulls to refresh in History screen

```kotlin
// data/remote/sync/SleepDataSyncWorker.kt
class SleepDataSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val sleepRepository: SleepRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()
        
        return try {
            // 1. Upload pending local changes
            uploadPendingLogs(userId)
            
            // 2. Download remote changes
            downloadRemoteLogs(userId)
            
            // 3. Sync user profile/preferences
            syncUserProfile(userId)
            
            Result.success()
        } catch (e: FirebaseNetworkException) {
            // Retry with exponential backoff
            Result.retry()
        } catch (e: Exception) {
            Timber.e(e, "Sync failed, will retry later")
            Result.failure()
        }
    }
    
    private suspend fun uploadPendingLogs(userId: String) {
        val pendingLogs = sleepRepository.getPendingUploadLogs()
        
        pendingLogs.forEach { log ->
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("sleep_logs")
                    .document(log.id)
                    .set(SleepLogDto.from(log))
                    .await()
                
                // Mark as synced in local database
                sleepRepository.updateSyncStatus(log.id, SyncStatus.SYNCED)
            } catch (e: Exception) {
                Timber.e(e, "Failed to upload log ${log.id}")
            }
        }
    }
    
    private suspend fun downloadRemoteLogs(userId: String) {
        val lastSyncTime = sleepRepository.getLastSyncTimestamp()
        
        val remoteLogs = firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .whereGreaterThan("updatedAt", lastSyncTime.toFirebaseTimestamp())
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(SleepLogDto::class.java)?.toDomain() }
        
        remoteLogs.forEach { remoteLog ->
            val localLog = sleepRepository.getSleepLog(remoteLog.id)
            
            if (localLog == null) {
                // New log from another device
                sleepRepository.saveSleepLog(remoteLog)
            } else {
                // Potential conflict - resolve
                val resolvedLog = resolveConflict(localLog, remoteLog)
                sleepRepository.saveSleepLog(resolvedLog)
            }
        }
        
        sleepRepository.updateLastSyncTimestamp(Instant.now())
    }
}
```

**Scheduling the Worker:**
```kotlin
// di/WorkManagerModule.kt
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    
    @Provides
    @Singleton
    fun provideSyncWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    fun schedulePeriodicSync(workManager: WorkManager) {
        val syncRequest = PeriodicWorkRequestBuilder<SleepDataSyncWorker>(
            repeatInterval = 6, // hours
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "sleep_data_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
```

---

## Conflict Resolution

### Last-Write-Wins Strategy

**Conflict Detection:**
```kotlin
data class SyncableEntity<T>(
    val data: T,
    val lastModified: Instant,
    val syncStatus: SyncStatus
)

enum class SyncStatus {
    SYNCED,           // Local == Remote
    PENDING_UPLOAD,   // Local newer than remote
    PENDING_DOWNLOAD, // Remote newer than local
    CONFLICT          // Both modified since last sync
}
```

**Resolution Logic:**
```kotlin
fun <T> resolveConflict(
    local: SyncableEntity<T>,
    remote: SyncableEntity<T>
): SyncableEntity<T> {
    return when {
        local.lastModified > remote.lastModified -> {
            // Local version is newer - upload
            local.copy(syncStatus = SyncStatus.PENDING_UPLOAD)
        }
        remote.lastModified > local.lastModified -> {
            // Remote version is newer - download
            remote.copy(syncStatus = SyncStatus.PENDING_DOWNLOAD)
        }
        else -> {
            // Same timestamp - already synced
            local.copy(syncStatus = SyncStatus.SYNCED)
        }
    }
}
```

**User-Facing Conflict Resolution (Future Enhancement):**
```kotlin
// For critical data where last-write-wins might lose important info
sealed class ConflictResolution {
    data class KeepLocal(val entity: SleepLog) : ConflictResolution()
    data class KeepRemote(val entity: SleepLog) : ConflictResolution()
    data class Merge(val local: SleepLog, val remote: SleepLog) : ConflictResolution()
}
```

---

## Security Rules

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isSignedIn() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isSignedIn() && request.auth.uid == userId;
    }
    
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if isOwner(userId);
      
      // User profile
      match /profile {
        allow read: if isOwner(userId);
        allow write: if isOwner(userId) 
                     && request.resource.data.userId == userId;
      }
      
      // Sleep logs
      match /sleep_logs/{logId} {
        allow read: if isOwner(userId);
        allow create: if isOwner(userId) 
                      && request.resource.data.id == logId;
        allow update: if isOwner(userId) 
                      && request.resource.data.id == logId
                      && resource.data.id == logId;
        allow delete: if isOwner(userId);
      }
    }
  }
}
```

**Testing Security Rules:**
```kotlin
@RunWith(AndroidJUnit4::class)
class FirestoreSecurityRulesTest {
    
    private lateinit var testEnv: FirebaseFirestoreTestEnvironment
    
    @Test
    fun userCannotAccessOtherUsersData() = runTest {
        val aliceDb = testEnv.authenticatedContext("alice").firestore
        val bobDb = testEnv.authenticatedContext("bob").firestore
        
        // Alice creates a sleep log
        aliceDb.collection("users/alice/sleep_logs")
            .add(mapOf("targetWakeTime" to Timestamp.now()))
            .await()
        
        // Bob tries to read Alice's data
        val exception = assertThrows<FirebaseFirestoreException> {
            bobDb.collection("users/alice/sleep_logs")
                .get()
                .await()
        }
        
        assertThat(exception.code).isEqualTo(FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
}
```

---

## Offline Behavior

### Read Operations

**Always read from local Room database first:**
```kotlin
override fun getSleepLogs(): Flow<List<SleepLog>> {
    // ONLY read from Room - never directly from Firestore
    return localDao.getAllSleepLogs()
        .map { entities -> entities.map { it.toDomain() } }
}
```

### Write Operations

**Write to Room immediately, queue Firestore sync:**
```kotlin
override suspend fun saveSleepLog(log: SleepLog): Result<Unit> {
    return try {
        // 1. Write to local database IMMEDIATELY
        localDao.insert(log.toEntity())
        
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
        
        // 3. Return success IMMEDIATELY (user sees instant update)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainError.DatabaseError(e.message))
    }
}
```

### Network State Handling

```kotlin
// data/remote/NetworkMonitor.kt
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val isConnected: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        
        connectivityManager?.registerDefaultNetworkCallback(callback)
        
        // Emit current state
        val currentState = connectivityManager?.activeNetwork != null
        trySend(currentState)
        
        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
```

**UI Feedback for Sync Status:**
```kotlin
@Composable
fun SyncStatusIndicator(
    isConnected: Boolean,
    lastSyncTime: Instant?
) {
    Row(
        modifier = Modifier.padding(SleepSpacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
            contentDescription = null,
            tint = if (isConnected) SleepColors.SuccessGreen else SleepColors.Gray300,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = when {
                !isConnected -> "Offline"
                lastSyncTime == null -> "Inte synkad"
                else -> "Synkad ${formatRelativeTime(lastSyncTime)}"
            },
            style = SleepTypography.LabelMedium,
            color = SleepColors.SteelBlue
        )
    }
}
```

---

## Data Migration Strategy

### Initial Data Seeding

**When user signs up and has existing local data:**
```kotlin
suspend fun migrateLocalDataToFirebase(userId: String) {
    val allLocalLogs = sleepRepository.getAllSleepLogs().first()
    
    allLocalLogs.forEach { log ->
        firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .document(log.id)
            .set(SleepLogDto.from(log))
            .await()
    }
    
    // Mark all as synced
    allLocalLogs.forEach { log ->
        sleepRepository.updateSyncStatus(log.id, SyncStatus.SYNCED)
    }
}
```

### Export/Import (Manual Backup)

```kotlin
// domain/usecases/ExportSleepDataUseCase.kt
class ExportSleepDataUseCase @Inject constructor(
    private val sleepRepository: SleepRepository
) {
    suspend operator fun invoke(): Result<File> {
        return try {
            val logs = sleepRepository.getAllSleepLogs().first()
            val json = Json.encodeToString(logs)
            
            val file = File(context.cacheDir, "sleep_data_export_${System.currentTimeMillis()}.json")
            file.writeText(json)
            
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(DomainError.ExportFailed(e.message))
        }
    }
}
```

---

## Performance Optimization

### Firestore Query Optimization

**Index Configuration (firestore.indexes.json):**
```json
{
  "indexes": [
    {
      "collectionGroup": "sleep_logs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "updatedAt", "order": "DESCENDING" },
        { "fieldPath": "targetWakeTime", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "sleep_logs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "wakeUpRating", "order": "DESCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

### Batch Operations

```kotlin
suspend fun batchUploadLogs(logs: List<SleepLog>, userId: String) {
    val batch = firestore.batch()
    
    logs.forEach { log ->
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .document(log.id)
        
        batch.set(docRef, SleepLogDto.from(log))
    }
    
    batch.commit().await()
}
```

---

*This document defines the complete Firebase integration strategy, ensuring offline-first functionality with robust background synchronization.*

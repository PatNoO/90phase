package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface SleepRepository {
    // Create
    suspend fun saveSleepLog(log: SleepLog): Result<Unit>

    // Read
    fun getAllSleepLogs(): Flow<List<SleepLog>>

    fun getSleepLog(id: String): Flow<SleepLog?>

    fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>>

    // Update
    suspend fun updateSleepLog(log: SleepLog): Result<Unit>

    suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
    ): Result<Unit>

    // Delete
    suspend fun deleteSleepLog(id: String): Result<Unit>

    // Sync support
    suspend fun getPendingUploadLogs(): Result<List<SleepLog>>

    suspend fun getLastSyncTimestamp(): Result<Instant>

    suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit>
}

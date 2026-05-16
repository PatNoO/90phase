package com.example.a90phase.data.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.room.dao.SleepLogDao
import com.example.a90phase.data.local.room.mapper.toDomain
import com.example.a90phase.data.local.room.mapper.toEntity
import com.example.a90phase.data.workers.SleepLogSyncWorker
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepositoryImpl @Inject constructor(
    private val sleepLogDao: SleepLogDao,
    private val dataStore: UserPreferencesDataStore,
    @ApplicationContext private val context: Context,
) : SleepRepository {

    override suspend fun saveSleepLog(log: SleepLog): Result<Unit> =
        runCatching {
            sleepLogDao.insertSleepLog(log.toEntity())
            enqueueSyncWork()
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override fun getAllSleepLogs(): Flow<List<SleepLog>> =
        sleepLogDao.getSleepLogsFlow().map { entities -> entities.map { it.toDomain() } }

    override fun getSleepLog(id: String): Flow<SleepLog?> =
        sleepLogDao.getSleepLogById(id).map { it?.toDomain() }

    override fun getSleepLogsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> =
        sleepLogDao.getSleepLogsByDateRange(startDate, endDate)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun updateSleepLog(log: SleepLog): Result<Unit> =
        runCatching {
            sleepLogDao.insertSleepLog(log.toEntity())
            enqueueSyncWork()
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun updateSyncStatus(id: String, status: SyncStatus): Result<Unit> =
        runCatching {
            sleepLogDao.updateSyncStatus(id, status)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun deleteSleepLog(id: String): Result<Unit> =
        runCatching {
            sleepLogDao.deleteSleepLog(id)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun getPendingUploadLogs(): Result<List<SleepLog>> =
        runCatching {
            val logs = sleepLogDao.getUnsyncedLogs().map { it.toDomain() }
            Result.Success(logs)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun getLastSyncTimestamp(): Result<Instant> =
        runCatching {
            val epochMillis = dataStore.observeLastSyncTimestamp().first()
            Result.Success(Instant.ofEpochMilli(epochMillis))
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun updateLastSyncTimestamp(timestamp: Instant): Result<Unit> =
        runCatching {
            dataStore.setLastSyncTimestamp(timestamp.toEpochMilli())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    private fun enqueueSyncWork() {
        val request = OneTimeWorkRequestBuilder<SleepLogSyncWorker>()
            .addTag(SleepLogSyncWorker.WORK_TAG)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SleepLogSyncWorker.WORK_TAG, ExistingWorkPolicy.KEEP, request)
    }
}

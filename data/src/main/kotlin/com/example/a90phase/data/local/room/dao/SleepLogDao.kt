package com.example.a90phase.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.a90phase.data.local.room.entity.SleepLogEntity
import com.example.a90phase.domain.entities.SyncStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(entity: SleepLogEntity)

    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getSleepLogsFlow(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs WHERE id = :id")
    fun getSleepLogById(id: String): Flow<SleepLogEntity?>

    @Query("SELECT * FROM sleep_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSleepLogsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs WHERE sync_status = 'PENDING_UPLOAD'")
    suspend fun getUnsyncedLogs(): List<SleepLogEntity>

    @Query("UPDATE sleep_logs SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE sleep_logs SET sync_status = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    @Query("DELETE FROM sleep_logs WHERE id = :id")
    suspend fun deleteSleepLog(id: String)
}

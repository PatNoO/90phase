package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.example.a90phase.domain.repositories.SleepRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class LogSleepSessionUseCase(
    private val sleepRepository: SleepRepository,
) {
    suspend fun createLog(
        date: LocalDate,
        wakeTime: Instant,
        bedtime: Instant? = null,
        cycleCount: Int,
        cycleDuration: Int,
        sleepLatency: Int,
    ): Result<SleepLog> {
        val log =
            SleepLog(
                id = UUID.randomUUID().toString(),
                date = date,
                wakeTime = wakeTime,
                bedtime = bedtime,
                cycleCount = cycleCount,
                cycleDurationUsed = cycleDuration,
                sleepLatencyUsed = sleepLatency,
                syncStatus = SyncStatus.PENDING_UPLOAD,
            )
        return sleepRepository.saveSleepLog(log).map { log }
    }

    suspend fun updateRating(
        log: SleepLog,
        rating: Int,
    ): Result<SleepLog> {
        if (rating !in MIN_RATING..MAX_RATING) {
            return Result.Error(
                DomainError.ValidationError("qualityRating", "Must be between $MIN_RATING and $MAX_RATING"),
            )
        }
        val updated = log.copy(qualityRating = rating, syncStatus = SyncStatus.PENDING_UPLOAD)
        return sleepRepository.updateSleepLog(updated).map { updated }
    }

    suspend fun updateBedtime(
        log: SleepLog,
        bedtime: Instant,
    ): Result<SleepLog> {
        val updated = log.copy(bedtime = bedtime, syncStatus = SyncStatus.PENDING_UPLOAD)
        return sleepRepository.updateSleepLog(updated).map { updated }
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}

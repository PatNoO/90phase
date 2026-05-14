package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.repositories.SleepRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class GetSleepHistoryUseCase(
    private val sleepRepository: SleepRepository,
) {
    fun allLogs(): Flow<List<SleepLog>> = sleepRepository.getAllSleepLogs()

    fun logsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<SleepLog>> = sleepRepository.getSleepLogsByDateRange(startDate, endDate)
}

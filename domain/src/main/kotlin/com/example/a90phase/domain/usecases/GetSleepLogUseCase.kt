package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.repositories.SleepRepository
import kotlinx.coroutines.flow.Flow

class GetSleepLogUseCase(
    private val sleepRepository: SleepRepository,
) {
    operator fun invoke(id: String): Flow<SleepLog?> = sleepRepository.getSleepLog(id)
}

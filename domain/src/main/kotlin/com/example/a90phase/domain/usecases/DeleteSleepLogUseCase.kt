package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.SleepRepository

class DeleteSleepLogUseCase(
    private val sleepRepository: SleepRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = sleepRepository.deleteSleepLog(id)
}

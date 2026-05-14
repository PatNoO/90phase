package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.repositories.OnboardingRepository

class SaveOnboardingStateUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    suspend operator fun invoke(state: UserOnboardingState): Result<Unit> = onboardingRepository.saveOnboardingState(state)

    suspend fun markCompleted(): Result<Unit> = onboardingRepository.markOnboardingCompleted()
}

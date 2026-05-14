package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.repositories.OnboardingRepository
import kotlinx.coroutines.flow.Flow

class GetOnboardingStateUseCase(
    private val onboardingRepository: OnboardingRepository,
) {
    operator fun invoke(): Flow<UserOnboardingState> = onboardingRepository.getOnboardingState()
}

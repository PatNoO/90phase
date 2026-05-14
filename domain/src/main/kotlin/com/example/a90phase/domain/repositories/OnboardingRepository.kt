package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserOnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun getOnboardingState(): Flow<UserOnboardingState>

    suspend fun saveOnboardingState(state: UserOnboardingState): Result<Unit>

    suspend fun markOnboardingCompleted(): Result<Unit>
}

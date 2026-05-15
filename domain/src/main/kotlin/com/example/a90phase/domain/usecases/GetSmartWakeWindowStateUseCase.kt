package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetSmartWakeWindowStateUseCase(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeSmartWakeWindowEnabled()
}

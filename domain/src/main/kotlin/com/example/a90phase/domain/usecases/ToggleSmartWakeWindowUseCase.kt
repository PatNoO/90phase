package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.UserPreferencesRepository

class ToggleSmartWakeWindowUseCase(
    private val repository: UserPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = repository.setSmartWakeWindowEnabled(enabled)
}

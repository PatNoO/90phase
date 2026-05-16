package com.example.a90phase.data.repositories

import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.datastore.toJson
import com.example.a90phase.data.local.datastore.toUserOnboardingState
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.repositories.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
) : OnboardingRepository {

    override fun getOnboardingState(): Flow<UserOnboardingState> =
        dataStore.observeOnboardingState().map { json ->
            json?.toUserOnboardingState() ?: UserOnboardingState()
        }

    override suspend fun saveOnboardingState(state: UserOnboardingState): Result<Unit> =
        runCatching {
            dataStore.setOnboardingState(state.toJson())
            dataStore.setOnboardingComplete(state.isCompleted)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun markOnboardingCompleted(): Result<Unit> =
        runCatching {
            val current = dataStore.observeOnboardingState().first()
                ?.toUserOnboardingState() ?: UserOnboardingState()
            dataStore.setOnboardingState(current.copy(isCompleted = true).toJson())
            dataStore.setOnboardingComplete(true)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }
}

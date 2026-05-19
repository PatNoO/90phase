package com.example.a90phase.data.repositories

import com.example.a90phase.data.local.datastore.PatternInsightsDataStore
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class PatternInsightsRepositoryImpl @Inject constructor(
    private val dataStore: PatternInsightsDataStore,
) : PatternInsightsRepository {

    override fun observePatternInsightsEnabled(): Flow<Boolean> =
        dataStore.observePatternInsightsEnabled()

    override suspend fun setPatternInsightsEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.setPatternInsightsEnabled(enabled)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override fun observeDismissedInsightIds(): Flow<Set<String>> =
        dataStore.observeDismissedInsightIds()

    override suspend fun dismissInsight(id: String): Result<Unit> =
        runCatching {
            dataStore.addDismissedInsightId(id)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }
}

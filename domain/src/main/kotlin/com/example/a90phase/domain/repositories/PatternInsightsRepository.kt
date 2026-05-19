package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import kotlinx.coroutines.flow.Flow

interface PatternInsightsRepository {
    fun observePatternInsightsEnabled(): Flow<Boolean>

    suspend fun setPatternInsightsEnabled(enabled: Boolean): Result<Unit>

    fun observeDismissedInsightIds(): Flow<Set<String>>

    suspend fun dismissInsight(id: String): Result<Unit>
}

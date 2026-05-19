package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.PatternInsightsRepository

class DismissPatternInsightUseCase(
    private val repository: PatternInsightsRepository,
) {
    suspend operator fun invoke(insightId: String): Result<Unit> = repository.dismissInsight(insightId)
}

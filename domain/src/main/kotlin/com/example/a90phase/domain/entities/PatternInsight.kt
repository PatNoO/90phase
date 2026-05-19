package com.example.a90phase.domain.entities

import java.time.LocalDate

data class PatternInsight(
    val id: String,
    val message: String,
    val type: InsightType,
    val createdAt: LocalDate,
    val isDismissed: Boolean = false,
)

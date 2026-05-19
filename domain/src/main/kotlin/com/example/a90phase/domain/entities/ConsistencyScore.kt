package com.example.a90phase.domain.entities

data class ConsistencyScore(
    val percentage: Int,
    val label: Label,
) {
    enum class Label { HIGH, MEDIUM, LOW }
}

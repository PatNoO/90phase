package com.example.a90phase.domain.entities

import java.time.Instant

data class SystemAlarm(
    val time: Instant,
    val label: String? = null,
    val isEnabled: Boolean = true,
)

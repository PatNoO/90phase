package com.example.a90phase.domain.entities

import java.time.Instant

data class SmartWakeWindow(
    val isEnabled: Boolean = false,
    val wakeTime: Instant,
    val windowStartMinutes: Int = 30,
    val windowEndMinutes: Int = 0,
    val lastDetectedMovement: Instant? = null,
)

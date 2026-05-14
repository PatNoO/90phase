package com.example.a90phase.domain.sensors

import kotlinx.coroutines.flow.Flow

interface AccelerometerDataSource {
    fun observeAcceleration(): Flow<AccelerometerReading>
}

data class AccelerometerReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestampMs: Long,
)

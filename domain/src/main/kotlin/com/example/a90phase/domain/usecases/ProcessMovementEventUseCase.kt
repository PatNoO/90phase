package com.example.a90phase.domain.usecases

import com.example.a90phase.domain.sensors.AccelerometerReading
import kotlin.math.sqrt

class ProcessMovementEventUseCase {
    private var lastReading: AccelerometerReading? = null
    private var significantMovementStartMs: Long? = null

    fun process(reading: AccelerometerReading): Boolean {
        val prev = lastReading
        lastReading = reading

        if (prev == null) return false

        val delta = magnitude(reading) - magnitude(prev)
        if (delta > MOVEMENT_THRESHOLD_MS2) {
            if (significantMovementStartMs == null) {
                significantMovementStartMs = reading.timestampMs
            }
            val durationMs = reading.timestampMs - significantMovementStartMs!!
            if (durationMs >= SUSTAINED_DURATION_MS) {
                return true
            }
        } else {
            significantMovementStartMs = null
        }
        return false
    }

    private fun magnitude(r: AccelerometerReading): Float = sqrt(r.x * r.x + r.y * r.y + r.z * r.z)

    companion object {
        const val MOVEMENT_THRESHOLD_MS2 = 2.0f
        const val SUSTAINED_DURATION_MS = 1_000L
    }
}

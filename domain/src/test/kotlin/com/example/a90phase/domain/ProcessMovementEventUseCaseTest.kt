package com.example.a90phase.domain

import com.example.a90phase.domain.sensors.AccelerometerReading
import com.example.a90phase.domain.usecases.ProcessMovementEventUseCase
import io.kotest.matchers.shouldBe
import org.junit.Test

class ProcessMovementEventUseCaseTest {
    private val useCase = ProcessMovementEventUseCase()

    private fun reading(
        x: Float = 0f,
        y: Float = 0f,
        z: Float = 9.8f,
        timestampMs: Long = 0L,
    ) = AccelerometerReading(x = x, y = y, z = z, timestampMs = timestampMs)

    // ── First reading ──────────────────────────────────────────────────────────

    @Test
    fun `first reading always returns false`() {
        val result = useCase.process(reading(timestampMs = 1_000L))

        result shouldBe false
    }

    // ── Below threshold ────────────────────────────────────────────────────────

    @Test
    fun `small delta below threshold returns false`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        val result = useCase.process(reading(z = 9.9f, timestampMs = 500L))

        result shouldBe false
    }

    // ── Sustained movement detection ───────────────────────────────────────────

    @Test
    fun `sustained movement above threshold for 1000ms returns true`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        useCase.process(reading(z = 15f, timestampMs = 100L)) // delta=5.2 → starts window at t=100
        useCase.process(reading(z = 22f, timestampMs = 500L)) // delta=7 → duration=400ms
        val result = useCase.process(reading(z = 30f, timestampMs = 1_100L)) // delta=8 → duration=1000ms

        result shouldBe true
    }

    @Test
    fun `movement not yet sustained for 1000ms returns false`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        useCase.process(reading(z = 15f, timestampMs = 100L)) // delta=5.2 → starts window at t=100
        val result = useCase.process(reading(z = 22f, timestampMs = 900L)) // duration=800ms < 1000ms

        result shouldBe false
    }

    // ── Reset on low movement ──────────────────────────────────────────────────

    @Test
    fun `drop below threshold resets sustained timer`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        useCase.process(reading(z = 15f, timestampMs = 100L)) // delta=5.2 → starts window at t=100
        useCase.process(reading(z = 14f, timestampMs = 600L)) // delta=-1 → resets
        useCase.process(reading(z = 20f, timestampMs = 700L)) // delta=6 → new window at t=700
        val result = useCase.process(reading(z = 27f, timestampMs = 1_500L)) // duration=800ms < 1000ms

        result shouldBe false
    }

    @Test
    fun `after reset sustained movement must reach threshold again`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        useCase.process(reading(z = 15f, timestampMs = 0L)) // delta=5.2 → starts window
        useCase.process(reading(z = 14f, timestampMs = 500L)) // delta=-1 → resets
        useCase.process(reading(z = 20f, timestampMs = 600L)) // delta=6 → new window at t=600
        val result = useCase.process(reading(z = 27f, timestampMs = 1_700L)) // duration=1100ms >= 1000ms

        result shouldBe true
    }

    // ── Exact boundary ─────────────────────────────────────────────────────────

    @Test
    fun `exactly 1000ms sustained movement returns true`() {
        useCase.process(reading(z = 9.8f, timestampMs = 0L))
        useCase.process(reading(z = 15f, timestampMs = 0L)) // delta=5.2 → starts window at t=0
        val result = useCase.process(reading(z = 22f, timestampMs = 1_000L)) // duration=1000ms >= 1000ms

        result shouldBe true
    }
}

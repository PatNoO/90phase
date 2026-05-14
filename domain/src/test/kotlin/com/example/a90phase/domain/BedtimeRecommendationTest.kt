package com.example.a90phase.domain

import com.example.a90phase.domain.entities.BedtimeQuality
import com.example.a90phase.domain.entities.BedtimeRecommendation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class BedtimeRecommendationTest {
    @Test
    fun `isOptimal is true only for OPTIMAL quality`() {
        val optimal = BedtimeRecommendation(LocalTime.of(21, 45), 6, BedtimeQuality.OPTIMAL, 555)
        val good = BedtimeRecommendation(LocalTime.of(23, 15), 5, BedtimeQuality.GOOD, 465)
        val passed = BedtimeRecommendation(LocalTime.of(20, 0), 6, BedtimeQuality.PASSED, 555)

        assertTrue(optimal.isOptimal)
        assertFalse(good.isOptimal)
        assertFalse(passed.isOptimal)
    }

    @Test
    fun `isPassed is true only for PASSED quality`() {
        val passed = BedtimeRecommendation(LocalTime.of(20, 0), 6, BedtimeQuality.PASSED, 555)
        val optimal = BedtimeRecommendation(LocalTime.of(21, 45), 6, BedtimeQuality.OPTIMAL, 555)

        assertTrue(passed.isPassed)
        assertFalse(optimal.isPassed)
    }

    @Test
    fun `formatBedtime returns HH-mm string`() {
        val rec = BedtimeRecommendation(LocalTime.of(21, 45), 6, BedtimeQuality.OPTIMAL, 555)
        assertEquals("21:45", rec.formatBedtime())
    }

    @Test
    fun `totalSleepHours converts minutes correctly`() {
        val rec = BedtimeRecommendation(LocalTime.of(21, 45), 6, BedtimeQuality.OPTIMAL, 555)
        assertEquals(9.25, rec.totalSleepHours(), 0.001)
    }
}

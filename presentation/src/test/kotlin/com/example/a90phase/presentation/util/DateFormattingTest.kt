package com.example.a90phase.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Locks in that the shared date patterns render correctly in both shipped locales.
 *
 * The composable helpers in `DateFormatting.kt` need a Compose runtime, so this covers the part
 * that actually decides the output: pattern plus locale.
 */
class DateFormattingTest {

    private val english = Locale.forLanguageTag("en")
    private val swedish = Locale.forLanguageTag("sv")
    private val thursday = LocalDate.of(2025, 5, 15)

    private fun format(pattern: String, locale: Locale) =
        thursday.format(DateTimeFormatter.ofPattern(pattern, locale))

    @Test
    fun `full date pattern renders English`() {
        assertEquals("Thursday 15 May 2025", format(FULL_DATE_PATTERN, english))
    }

    @Test
    fun `full date pattern renders Swedish`() {
        assertEquals("torsdag 15 maj 2025", format(FULL_DATE_PATTERN, swedish))
    }

    @Test
    fun `card date pattern renders English`() {
        assertEquals("Thursday  15 May", format(CARD_DATE_PATTERN, english))
    }

    @Test
    fun `card date pattern renders Swedish`() {
        assertEquals("torsdag  15 maj", format(CARD_DATE_PATTERN, swedish))
    }

    @Test
    fun `weekday name is localized rather than the enum name`() {
        val dayOfWeek = DayOfWeek.THURSDAY
        assertEquals("Thursday", dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, english))
        assertEquals("torsdag", dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, swedish))
        // The enum name is always English — the bug this replaced.
        assertEquals("THURSDAY", dayOfWeek.toString())
    }

    @Test
    fun `locale tag resource values map to real locales`() {
        assertEquals("en", english.toLanguageTag())
        assertEquals("sv", swedish.toLanguageTag())
    }
}

package com.example.a90phase.presentation.util

private const val MINUTES_PER_HOUR = 60

/**
 * Formats a sleep duration for display, e.g. `7h 30min` or `9h`.
 *
 * Shared by the History list card, the log detail screen and the alarm ring screen so the same
 * duration can never be rendered two different ways.
 */
fun formatSleepDuration(totalMinutes: Int): String {
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR
    return if (minutes == 0) "${hours}h" else "${hours}h ${minutes}min"
}

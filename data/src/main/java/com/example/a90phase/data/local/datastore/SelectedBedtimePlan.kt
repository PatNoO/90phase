package com.example.a90phase.data.local.datastore

/**
 * The bedtime the user selected on the calculator, as stored in preferences.
 *
 * [durationMinutes] is time *in bed* — it includes sleep latency, matching
 * `BedtimeRecommendation.durationMinutes` which produced it. That differs from
 * `SleepLog.sleepDurationMinutes`, which is time asleep and excludes latency.
 */
data class SelectedBedtimePlan(
    val cycleCount: Int,
    val durationMinutes: Int,
)

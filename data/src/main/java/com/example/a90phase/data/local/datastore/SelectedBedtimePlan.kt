package com.example.a90phase.data.local.datastore

import java.time.LocalTime

/**
 * The bedtime the user selected on the calculator, as stored in preferences.
 *
 * [durationMinutes] is time *in bed* — it includes sleep latency, matching
 * `BedtimeRecommendation.durationMinutes` which produced it. That differs from
 * `SleepLog.sleepDurationMinutes`, which is time asleep and excludes latency.
 */
data class SelectedBedtimePlan(
    val bedtime: LocalTime,
    val cycleCount: Int,
    val durationMinutes: Int,
) {
    /**
     * The wake time this plan was built for.
     *
     * Only one plan is ever stored, and nothing clears it when the user picks a different wake
     * time, so a stored plan can easily belong to a wake-up that is days old. Deriving its wake
     * time is what lets callers tell a current plan from a stale one — see [belongsTo].
     *
     * [LocalTime.plusMinutes] wraps past midnight, which is the normal case: a bedtime of 23:15
     * plus 465 minutes is 07:00 the next morning.
     */
    val wakeTime: LocalTime
        get() = bedtime.plusMinutes(durationMinutes.toLong())

    /** True when this plan was built for [candidate], and so describes the night just ended. */
    fun belongsTo(candidate: LocalTime): Boolean = wakeTime == candidate
}

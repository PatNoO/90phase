package com.example.a90phase.data.local.datastore

import com.example.a90phase.domain.entities.UserOnboardingState

internal fun UserOnboardingState.toJson(): String =
    "{\"isCompleted\":$isCompleted," +
        "\"dailyCheckInEnabled\":$dailyCheckInEnabled," +
        "\"bedtimeReminderEnabled\":$bedtimeReminderEnabled," +
        "\"morningRatingEnabled\":$morningRatingEnabled," +
        "\"morningBedtimeLogEnabled\":$morningBedtimeLogEnabled," +
        "\"smartWakeWindowEnabled\":$smartWakeWindowEnabled," +
        "\"discoveryPhaseInfoShown\":$discoveryPhaseInfoShown}"

internal fun String.toUserOnboardingState(): UserOnboardingState = runCatching {
    fun String.extractBool(key: String): Boolean =
        Regex(""""$key"\s*:\s*(true|false)""").find(this)?.groupValues?.get(1) == "true"
    UserOnboardingState(
        isCompleted = extractBool("isCompleted"),
        dailyCheckInEnabled = extractBool("dailyCheckInEnabled"),
        bedtimeReminderEnabled = extractBool("bedtimeReminderEnabled"),
        morningRatingEnabled = extractBool("morningRatingEnabled"),
        morningBedtimeLogEnabled = extractBool("morningBedtimeLogEnabled"),
        smartWakeWindowEnabled = extractBool("smartWakeWindowEnabled"),
        discoveryPhaseInfoShown = extractBool("discoveryPhaseInfoShown"),
    )
}.getOrDefault(UserOnboardingState())

package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import java.time.LocalTime

interface AlarmRepository {
    suspend fun getNextAlarm(): Result<SystemAlarm?>

    suspend fun getAllAlarms(): Result<List<SystemAlarm>>

    // Hands the wake time to the device's clock app so the OS rings a real alarm,
    // tagged with the app's label so it can be found and removed later.
    suspend fun setAlarm(wakeTime: LocalTime): Result<Unit>

    // Removes the app's labelled alarm regardless of its time (label search is more
    // reliable than time search across OEM clock apps).
    suspend fun dismissAlarm(): Result<Unit>
}

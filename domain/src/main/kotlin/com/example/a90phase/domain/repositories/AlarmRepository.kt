package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm

interface AlarmRepository {
    suspend fun getNextAlarm(): Result<SystemAlarm?>

    suspend fun getAllAlarms(): Result<List<SystemAlarm>>
}

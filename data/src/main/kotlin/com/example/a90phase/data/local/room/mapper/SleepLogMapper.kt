package com.example.a90phase.data.local.room.mapper

import com.example.a90phase.data.local.room.entity.SleepLogEntity
import com.example.a90phase.domain.entities.SleepLog

fun SleepLogEntity.toDomain(): SleepLog =
    SleepLog(
        id = id,
        date = date,
        bedtime = bedtime,
        wakeTime = wakeTime,
        qualityRating = qualityRating,
        cycleCount = cycleCount,
        cycleDurationUsed = cycleDurationUsed,
        sleepLatencyUsed = sleepLatencyUsed,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
    )

fun SleepLog.toEntity(): SleepLogEntity =
    SleepLogEntity(
        id = id,
        date = date,
        bedtime = bedtime,
        wakeTime = wakeTime,
        qualityRating = qualityRating,
        cycleCount = cycleCount,
        cycleDurationUsed = cycleDurationUsed,
        sleepLatencyUsed = sleepLatencyUsed,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
    )

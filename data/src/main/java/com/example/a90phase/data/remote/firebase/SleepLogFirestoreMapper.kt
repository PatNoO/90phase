package com.example.a90phase.data.remote.firebase

import com.example.a90phase.domain.entities.SleepLog
import com.example.a90phase.domain.entities.SyncStatus
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate

fun FirestoreSleepLogDocument.toInstant(timestamp: Timestamp): Instant =
    Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())

fun FirestoreSleepLogDocument.toDomainSleepLog(): SleepLog =
    SleepLog(
        id = id,
        date = LocalDate.parse(date),
        bedtime = bedtime?.let { Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong()) },
        wakeTime = Instant.ofEpochSecond(wakeTime.seconds, wakeTime.nanoseconds.toLong()),
        qualityRating = qualityRating?.toInt(),
        cycleCount = cycleCount.toInt(),
        cycleDurationUsed = cycleDurationUsed.toInt(),
        sleepLatencyUsed = sleepLatencyUsed.toInt(),
        notes = notes,
        createdAt = Instant.ofEpochSecond(createdAt.seconds, createdAt.nanoseconds.toLong()),
        updatedAt = Instant.ofEpochSecond(updatedAt.seconds, updatedAt.nanoseconds.toLong()),
        syncStatus = SyncStatus.SYNCED,
    )

fun SleepLog.toFirestoreDocument(): FirestoreSleepLogDocument =
    FirestoreSleepLogDocument(
        id = id,
        date = date.toString(),
        bedtime = bedtime?.let { Timestamp(it.epochSecond, it.nano) },
        wakeTime = Timestamp(wakeTime.epochSecond, wakeTime.nano),
        qualityRating = qualityRating?.toLong(),
        cycleCount = cycleCount.toLong(),
        cycleDurationUsed = cycleDurationUsed.toLong(),
        sleepLatencyUsed = sleepLatencyUsed.toLong(),
        notes = notes,
        createdAt = Timestamp(createdAt.epochSecond, createdAt.nano),
        updatedAt = Timestamp(updatedAt.epochSecond, updatedAt.nano),
    )

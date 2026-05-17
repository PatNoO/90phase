package com.example.a90phase.data.remote.firebase

import com.example.a90phase.domain.entities.SleepLog
import com.google.firebase.Timestamp

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

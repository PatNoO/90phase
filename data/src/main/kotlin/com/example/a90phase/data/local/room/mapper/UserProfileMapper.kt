package com.example.a90phase.data.local.room.mapper

import com.example.a90phase.data.local.room.entity.UserProfileEntity
import com.example.a90phase.domain.entities.DailyRating
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.domain.entities.UserProfile
import java.time.Instant
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        userId = userId,
        email = email,
        displayName = displayName,
        optimalCycleMinutes = optimalCycleMinutes,
        sleepLatencyMinutes = sleepLatencyMinutes,
        preferredCycleCount = preferredCycleCount,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        smartWakeWindowEnabled = smartWakeWindowEnabled,
        discoveryPhase = discoveryPhaseJson?.toDiscoveryPhase(),
    )

fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        rowId = 0,
        userId = userId,
        email = email,
        displayName = displayName,
        optimalCycleMinutes = optimalCycleMinutes,
        sleepLatencyMinutes = sleepLatencyMinutes,
        preferredCycleCount = preferredCycleCount,
        reminderTime = reminderTime,
        notificationsEnabled = notificationsEnabled,
        smartWakeWindowEnabled = smartWakeWindowEnabled,
        discoveryPhaseJson = discoveryPhase?.toJson(),
    )

internal fun DiscoveryPhase.toJson(): String {
    val ratingsArray = JSONArray()
    weeklyRatings.forEach { dailyRating ->
        val ratingJson = JSONObject()
        ratingJson.put("date", dailyRating.date.toEpochDay())
        dailyRating.rating?.let { ratingJson.put("rating", it) }
        ratingJson.put("shiftType", dailyRating.shiftType.toKey())
        dailyRating.actualWakeUpTime?.let { ratingJson.put("actualWakeUpTime", it.toEpochMilli()) }
        ratingsArray.put(ratingJson)
    }
    return JSONObject()
        .put("isActive", isActive)
        .put("currentShift", currentShift.toKey())
        .put("startDate", startDate.toEpochDay())
        .put("isCompleted", isCompleted)
        .put("weeklyRatings", ratingsArray)
        .toString()
}

internal fun String.toDiscoveryPhase(): DiscoveryPhase {
    val json = JSONObject(this)
    val ratingsArray = json.optJSONArray("weeklyRatings") ?: JSONArray()
    val ratings = (0 until ratingsArray.length()).map { i ->
        val r = ratingsArray.getJSONObject(i)
        DailyRating(
            date = LocalDate.ofEpochDay(r.getLong("date")),
            rating = if (r.has("rating")) r.getInt("rating") else null,
            shiftType = r.getString("shiftType").toShiftType(),
            actualWakeUpTime = if (r.has("actualWakeUpTime")) Instant.ofEpochMilli(r.getLong("actualWakeUpTime")) else null,
        )
    }
    return DiscoveryPhase(
        isActive = json.getBoolean("isActive"),
        currentShift = json.getString("currentShift").toShiftType(),
        startDate = LocalDate.ofEpochDay(json.getLong("startDate")),
        weeklyRatings = ratings,
        isCompleted = json.getBoolean("isCompleted"),
    )
}

private fun ShiftType.toKey(): String =
    when (this) {
        is ShiftType.LongerLatency -> "LONGER_LATENCY"
        is ShiftType.LongerCycles -> "LONGER_CYCLES"
        is ShiftType.FewerCycles -> "FEWER_CYCLES"
    }

private fun String.toShiftType(): ShiftType =
    when (this) {
        "LONGER_LATENCY" -> ShiftType.LongerLatency
        "LONGER_CYCLES" -> ShiftType.LongerCycles
        "FEWER_CYCLES" -> ShiftType.FewerCycles
        else -> ShiftType.LongerLatency
    }

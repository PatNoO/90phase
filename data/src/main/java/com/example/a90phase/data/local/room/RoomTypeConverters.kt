package com.example.a90phase.data.local.room

import androidx.room.TypeConverter
import com.example.a90phase.domain.entities.SyncStatus
import java.time.Instant
import java.time.LocalDate

class RoomTypeConverters {

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.ofEpochMilli(value)

    @TypeConverter
    fun fromNullableInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toNullableInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate): Long = value.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}

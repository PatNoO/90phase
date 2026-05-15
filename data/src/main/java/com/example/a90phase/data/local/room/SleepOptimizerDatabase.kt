package com.example.a90phase.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.a90phase.data.local.room.entity.SleepLogEntity
import com.example.a90phase.data.local.room.entity.UserProfileEntity

@Database(
    entities = [SleepLogEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomTypeConverters::class)
abstract class SleepOptimizerDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "sleep_optimizer.db"
    }
}

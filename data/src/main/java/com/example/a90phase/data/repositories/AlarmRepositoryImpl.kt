package com.example.a90phase.data.repositories

import android.app.AlarmManager
import android.content.Context
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.repositories.AlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmRepository {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun getNextAlarm(): Result<SystemAlarm?> = runCatching {
        val info = alarmManager.nextAlarmClock
        Result.Success(
            info?.let { SystemAlarm(time = Instant.ofEpochMilli(it.triggerTime)) },
        )
    }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> =
        when (val next = getNextAlarm()) {
            is Result.Success -> Result.Success(listOfNotNull(next.data))
            is Result.Error -> next
            is Result.Loading -> next
        }
}

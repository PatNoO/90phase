package com.example.a90phase.data.repositories

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.repositories.AlarmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalTime
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

    override suspend fun setAlarm(wakeTime: LocalTime): Result<Unit> = runCatching {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, wakeTime.hour)
            putExtra(AlarmClock.EXTRA_MINUTES, wakeTime.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, ALARM_LABEL)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Result.Success(Unit)
    }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun dismissAlarm(): Result<Unit> = runCatching {
        val intent = Intent(AlarmClock.ACTION_DISMISS_ALARM).apply {
            putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL)
            putExtra(AlarmClock.EXTRA_MESSAGE, ALARM_LABEL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Result.Success(Unit)
    }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    private companion object {
        const val ALARM_LABEL = "90phase wake-up"
    }
}

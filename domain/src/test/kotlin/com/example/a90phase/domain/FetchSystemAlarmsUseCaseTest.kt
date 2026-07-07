package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.SystemAlarm
import com.example.a90phase.domain.repositories.AlarmRepository
import com.example.a90phase.domain.usecases.FetchSystemAlarmsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FetchSystemAlarmsUseCaseTest {
    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    fun `returns alarms from repository`() =
        runTest {
            val alarms =
                listOf(
                    SystemAlarm(time = Instant.parse("2024-01-15T07:00:00Z"), label = "Morning"),
                    SystemAlarm(time = Instant.parse("2024-01-15T08:00:00Z"), label = "Backup"),
                )
            val result = FetchSystemAlarmsUseCase(FakeAlarmRepository(alarms))()

            assertTrue(result is Result.Success)
            assertEquals(alarms, (result as Result.Success).data)
        }

    @Test
    fun `returns empty list when no alarms exist`() =
        runTest {
            val result = FetchSystemAlarmsUseCase(FakeAlarmRepository(emptyList()))()

            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isEmpty())
        }

    // ── Permission denied — graceful degradation ───────────────────────────────

    @Test
    fun `returns empty list when permission is denied`() =
        runTest {
            val result = FetchSystemAlarmsUseCase(PermissionDeniedAlarmRepository())()

            assertTrue(result is Result.Success)
            assertTrue((result as Result.Success).data.isEmpty())
        }

    // ── Other errors propagate ─────────────────────────────────────────────────

    @Test
    fun `propagates non-permission errors`() =
        runTest {
            val result = FetchSystemAlarmsUseCase(FailingAlarmRepository())()

            assertTrue(result is Result.Error)
            assertTrue((result as Result.Error).error is DomainError.DatabaseError)
        }
}

// ── Test doubles ───────────────────────────────────────────────────────────────

private class FakeAlarmRepository(
    private val alarms: List<SystemAlarm>,
) : AlarmRepository {
    override suspend fun getNextAlarm(): Result<SystemAlarm?> = Result.Success(alarms.firstOrNull())

    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> = Result.Success(alarms)

    override suspend fun setAlarm(wakeTime: java.time.LocalTime): Result<Unit> = Result.Success(Unit)

    override suspend fun dismissAlarm(): Result<Unit> = Result.Success(Unit)
}

private class PermissionDeniedAlarmRepository : AlarmRepository {
    override suspend fun getNextAlarm(): Result<SystemAlarm?> = Result.Error(DomainError.PermissionDenied("READ_ALARM"))

    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> = Result.Error(DomainError.PermissionDenied("READ_ALARM"))

    override suspend fun setAlarm(wakeTime: java.time.LocalTime): Result<Unit> = Result.Success(Unit)

    override suspend fun dismissAlarm(): Result<Unit> = Result.Success(Unit)
}

private class FailingAlarmRepository : AlarmRepository {
    override suspend fun getNextAlarm(): Result<SystemAlarm?> = Result.Error(DomainError.DatabaseError("DB unavailable"))

    override suspend fun getAllAlarms(): Result<List<SystemAlarm>> = Result.Error(DomainError.DatabaseError("DB unavailable"))

    override suspend fun setAlarm(wakeTime: java.time.LocalTime): Result<Unit> = Result.Success(Unit)

    override suspend fun dismissAlarm(): Result<Unit> = Result.Success(Unit)
}

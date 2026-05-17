package com.example.a90phase.domain

import app.cash.turbine.test
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import com.example.a90phase.domain.usecases.GetSmartWakeWindowStateUseCase
import com.example.a90phase.domain.usecases.ToggleSmartWakeWindowUseCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SmartWakeWindowUseCasesTest {
    // ToggleSmartWakeWindowUseCase

    @Test
    fun `toggle enabled returns success`() =
        runTest {
            val repo = FakeSmartWakeRepository()
            val useCase = ToggleSmartWakeWindowUseCase(repo)

            val result = useCase(enabled = true)

            result.shouldBeInstanceOf<Result.Success<Unit>>()
            repo.lastEnabled shouldBe true
        }

    @Test
    fun `toggle disabled returns success and propagates false`() =
        runTest {
            val repo = FakeSmartWakeRepository()
            val useCase = ToggleSmartWakeWindowUseCase(repo)

            val result = useCase(enabled = false)

            result.shouldBeInstanceOf<Result.Success<Unit>>()
            repo.lastEnabled shouldBe false
        }

    @Test
    fun `toggle returns error on repository failure`() =
        runTest {
            val repo = FailingSmartWakeRepository()
            val useCase = ToggleSmartWakeWindowUseCase(repo)

            val result = useCase(enabled = true)

            result.shouldBeInstanceOf<Result.Error>()
        }

    // GetSmartWakeWindowStateUseCase

    @Test
    fun `get state emits current value`() =
        runTest {
            val repo = FakeSmartWakeRepository(initialEnabled = true)
            val useCase = GetSmartWakeWindowStateUseCase(repo)

            useCase().test {
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `get state emits updated value after toggle`() =
        runTest {
            val repo = FakeSmartWakeRepository(initialEnabled = false)
            val useCase = GetSmartWakeWindowStateUseCase(repo)

            useCase().test {
                awaitItem() shouldBe false
                repo.setSmartWakeWindowEnabled(true)
                awaitItem() shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class FakeSmartWakeRepository(
    initialEnabled: Boolean = false,
) : UserPreferencesRepository {
    var lastEnabled: Boolean = initialEnabled
    private val enabledState = MutableStateFlow(initialEnabled)

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> {
        lastEnabled = enabled
        enabledState.value = enabled
        return Result.Success(Unit)
    }

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = enabledState.asStateFlow()

    override suspend fun getUserProfile(): Result<UserProfile> = Result.Success(UserProfile("test"))

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = Result.Success(Unit)

    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = Result.Success(Unit)

    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = Result.Success(Unit)

    override suspend fun setReminderTime(time: String): Result<Unit> = Result.Success(Unit)

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = Result.Success(Unit)

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = MutableStateFlow(true).asStateFlow()

    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)

    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = Result.Success(Unit)

    override suspend fun endDiscoveryPhase(): Result<Unit> = Result.Success(Unit)

    override fun observeUserProfile(): Flow<UserProfile> = MutableStateFlow(UserProfile("test")).asStateFlow()
}

private class FailingSmartWakeRepository : UserPreferencesRepository {
    private val error = Result.Error(DomainError.DatabaseError("DataStore unavailable"))

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> = error

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> = MutableStateFlow(false).asStateFlow()

    override suspend fun getUserProfile(): Result<UserProfile> = error

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = error

    override suspend fun setCycleDuration(minutes: Int): Result<Unit> = error

    override suspend fun setSleepLatency(minutes: Int): Result<Unit> = error

    override suspend fun setReminderTime(time: String): Result<Unit> = error

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> = error

    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> = error

    override fun observeDailyCheckInEnabled(): Flow<Boolean> = MutableStateFlow(true).asStateFlow()

    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = error

    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> = error

    override suspend fun endDiscoveryPhase(): Result<Unit> = error

    override fun observeUserProfile(): Flow<UserProfile> = MutableStateFlow(UserProfile("test")).asStateFlow()
}

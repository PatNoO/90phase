package com.example.a90phase.domain

import app.cash.turbine.test
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.UserOnboardingState
import com.example.a90phase.domain.repositories.OnboardingRepository
import com.example.a90phase.domain.usecases.GetOnboardingStateUseCase
import com.example.a90phase.domain.usecases.SaveOnboardingStateUseCase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OnboardingUseCasesTest {
    // ── GetOnboardingStateUseCase ──────────────────────────────────────────────

    @Test
    fun `GetOnboardingStateUseCase emits default state from repository`() =
        runTest {
            val repo = FakeOnboardingRepository()
            GetOnboardingStateUseCase(repo)().test {
                awaitItem() shouldBe UserOnboardingState()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GetOnboardingStateUseCase emits updated state after save`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = GetOnboardingStateUseCase(repo)

            useCase().test {
                awaitItem() shouldBe UserOnboardingState()

                val updated = UserOnboardingState(dailyCheckInEnabled = true)
                repo.saveOnboardingState(updated)
                awaitItem() shouldBe updated

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `GetOnboardingStateUseCase reflects completed state`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = GetOnboardingStateUseCase(repo)

            useCase().test {
                awaitItem()

                repo.markOnboardingCompleted()
                awaitItem().isCompleted shouldBe true

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── SaveOnboardingStateUseCase ─────────────────────────────────────────────

    @Test
    fun `SaveOnboardingStateUseCase returns Success`() =
        runTest {
            val result = SaveOnboardingStateUseCase(FakeOnboardingRepository())(UserOnboardingState())
            result.shouldBeInstanceOf<Result.Success<Unit>>()
        }

    @Test
    fun `SaveOnboardingStateUseCase persists state to repository`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val state = UserOnboardingState(bedtimeReminderEnabled = true, morningRatingEnabled = true)

            SaveOnboardingStateUseCase(repo)(state)

            GetOnboardingStateUseCase(repo)().test {
                awaitItem() shouldBe state
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `SaveOnboardingStateUseCase morningRatingEnabled and morningBedtimeLogEnabled are independent`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val useCase = SaveOnboardingStateUseCase(repo)

            useCase(UserOnboardingState(morningRatingEnabled = true, morningBedtimeLogEnabled = false))

            GetOnboardingStateUseCase(repo)().test {
                val state = awaitItem()
                state.morningRatingEnabled shouldBe true
                state.morningBedtimeLogEnabled shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `markCompleted sets isCompleted to true`() =
        runTest {
            val repo = FakeOnboardingRepository()
            val saveUseCase = SaveOnboardingStateUseCase(repo)

            val result = saveUseCase.markCompleted()
            result.shouldBeInstanceOf<Result.Success<Unit>>()

            GetOnboardingStateUseCase(repo)().test {
                awaitItem().isCompleted shouldBe true
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `SaveOnboardingStateUseCase propagates repository error`() =
        runTest {
            val result = SaveOnboardingStateUseCase(FailingOnboardingRepository())(UserOnboardingState())
            result.shouldBeInstanceOf<Result.Error>()
        }
}

// ── Test doubles ───────────────────────────────────────────────────────────────

private class FakeOnboardingRepository : OnboardingRepository {
    private val state = MutableStateFlow(UserOnboardingState())

    override fun getOnboardingState(): Flow<UserOnboardingState> = state

    override suspend fun saveOnboardingState(onboardingState: UserOnboardingState): Result<Unit> {
        state.value = onboardingState
        return Result.Success(Unit)
    }

    override suspend fun markOnboardingCompleted(): Result<Unit> {
        state.value = state.value.copy(isCompleted = true)
        return Result.Success(Unit)
    }
}

private class FailingOnboardingRepository : OnboardingRepository {
    override fun getOnboardingState(): Flow<UserOnboardingState> = MutableStateFlow(UserOnboardingState())

    override suspend fun saveOnboardingState(onboardingState: UserOnboardingState): Result<Unit> =
        Result.Error(DomainError.DatabaseError("DB unavailable"))

    override suspend fun markOnboardingCompleted(): Result<Unit> = Result.Error(DomainError.DatabaseError("DB unavailable"))
}

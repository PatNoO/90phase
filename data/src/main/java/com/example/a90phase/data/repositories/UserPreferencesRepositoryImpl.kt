package com.example.a90phase.data.repositories

import com.example.a90phase.data.local.datastore.UserPreferencesDataStore
import com.example.a90phase.data.local.room.dao.UserProfileDao
import com.example.a90phase.data.local.room.mapper.toDomain
import com.example.a90phase.data.local.room.mapper.toEntity
import com.example.a90phase.data.local.room.mapper.toJson
import com.example.a90phase.data.sync.SyncScheduler
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.entities.DiscoveryPhase
import com.example.a90phase.domain.entities.UserProfile
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Suppress("TooManyFunctions")
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val dataStore: UserPreferencesDataStore,
    private val syncScheduler: SyncScheduler,
) : UserPreferencesRepository {

    override suspend fun setSmartWakeWindowEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.setSmartWakeWindowEnabled(enabled)
            if (enabled) syncScheduler.scheduleSmartWakeMonitor() else syncScheduler.cancelSmartWakeMonitor()
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(smartWakeWindowEnabled = enabled).toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override fun observeSmartWakeWindowEnabled(): Flow<Boolean> =
        dataStore.observeSmartWakeWindowEnabled()

    override suspend fun getUserProfile(): Result<UserProfile> =
        runCatching {
            val entity = userProfileDao.getUserProfile()
            Result.Success(entity?.toDomain() ?: defaultProfile())
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> =
        runCatching {
            userProfileDao.insertOrUpdateProfile(profile.toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun setCycleDuration(minutes: Int): Result<Unit> =
        runCatching {
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(optimalCycleMinutes = minutes).toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun setSleepLatency(minutes: Int): Result<Unit> =
        runCatching {
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(sleepLatencyMinutes = minutes).toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun setReminderTime(time: String): Result<Unit> =
        runCatching {
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(reminderTime = time).toEntity())
            dataStore.setNotificationTime(time)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun setNotificationsEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(notificationsEnabled = enabled).toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun startDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> =
        runCatching {
            val profile = getOrCreateProfile()
            userProfileDao.insertOrUpdateProfile(profile.copy(discoveryPhase = phase).toEntity())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun updateDiscoveryPhase(phase: DiscoveryPhase): Result<Unit> =
        runCatching {
            userProfileDao.updateDiscoveryPhase(phase.toJson())
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun endDiscoveryPhase(): Result<Unit> =
        runCatching {
            userProfileDao.updateDiscoveryPhase(null)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override suspend fun setDailyCheckInEnabled(enabled: Boolean): Result<Unit> =
        runCatching {
            dataStore.setDailyCheckInEnabled(enabled)
            Result.Success(Unit)
        }.getOrElse { Result.Error(DomainError.DatabaseError(it.message)) }

    override fun observeDailyCheckInEnabled(): Flow<Boolean> =
        dataStore.observeDailyCheckInEnabled()

    override fun observeUserProfile(): Flow<UserProfile> =
        userProfileDao.getUserProfileFlow().map { it?.toDomain() ?: defaultProfile() }

    private suspend fun getOrCreateProfile(): UserProfile =
        userProfileDao.getUserProfile()?.toDomain() ?: defaultProfile()

    private fun defaultProfile() = UserProfile(userId = DEFAULT_USER_ID)

    companion object {
        private const val DEFAULT_USER_ID = "local_user"
    }
}

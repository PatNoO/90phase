package com.example.a90phase.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

@Suppress("TooManyFunctions")
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore get() = context.userPreferencesDataStore

    suspend fun setSmartWakeWindowEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SMART_WAKE_ENABLED] = enabled }
    }

    fun observeSmartWakeWindowEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.SMART_WAKE_ENABLED] ?: false }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    fun observeOnboardingComplete(): Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING_COMPLETE] ?: false }

    suspend fun setNotificationTime(time: String) {
        dataStore.edit { it[Keys.NOTIFICATION_TIME] = time }
    }

    fun observeNotificationTime(): Flow<String> =
        dataStore.data.map { it[Keys.NOTIFICATION_TIME] ?: DEFAULT_NOTIFICATION_TIME }

    suspend fun setLastSyncTimestamp(epochMillis: Long) {
        dataStore.edit { it[Keys.LAST_SYNC_TIMESTAMP] = epochMillis }
    }

    fun observeLastSyncTimestamp(): Flow<Long> =
        dataStore.data.map { it[Keys.LAST_SYNC_TIMESTAMP] ?: 0L }

    suspend fun setDailyCheckInEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DAILY_CHECKIN_ENABLED] = enabled }
    }

    fun observeDailyCheckInEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.DAILY_CHECKIN_ENABLED] ?: true }

    suspend fun setBedtimeReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BEDTIME_REMINDER_ENABLED] = enabled }
    }

    fun observeBedtimeReminderEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.BEDTIME_REMINDER_ENABLED] ?: false }

    suspend fun setSelectedBedtime(hour: Int, minute: Int, cycleCount: Int, durationMinutes: Int) {
        dataStore.edit {
            it[Keys.SELECTED_BEDTIME_HOUR] = hour
            it[Keys.SELECTED_BEDTIME_MINUTE] = minute
            it[Keys.SELECTED_BEDTIME_CYCLES] = cycleCount
            it[Keys.SELECTED_BEDTIME_DURATION] = durationMinutes
        }
    }

    fun observeSelectedBedtimeCycles(): Flow<Int> =
        dataStore.data.map { it[Keys.SELECTED_BEDTIME_CYCLES] ?: 5 }

    fun observeSelectedBedtimeDuration(): Flow<Int> =
        dataStore.data.map { it[Keys.SELECTED_BEDTIME_DURATION] ?: 450 }

    suspend fun setMorningRatingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MORNING_RATING_ENABLED] = enabled }
    }

    fun observeMorningRatingEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.MORNING_RATING_ENABLED] ?: true }

    suspend fun setMorningBedtimeLogEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MORNING_BEDTIME_LOG_ENABLED] = enabled }
    }

    fun observeMorningBedtimeLogEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.MORNING_BEDTIME_LOG_ENABLED] ?: true }

    suspend fun setSelectedWakeTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.SELECTED_WAKE_HOUR] = hour
            it[Keys.SELECTED_WAKE_MINUTE] = minute
        }
    }

    fun observeSelectedWakeHour(): Flow<Int> =
        dataStore.data.map { it[Keys.SELECTED_WAKE_HOUR] ?: DEFAULT_WAKE_HOUR }

    fun observeSelectedWakeMinute(): Flow<Int> =
        dataStore.data.map { it[Keys.SELECTED_WAKE_MINUTE] ?: 0 }

    suspend fun setOnboardingState(json: String?) {
        dataStore.edit { prefs ->
            if (json == null) prefs.remove(Keys.ONBOARDING_STATE_JSON)
            else prefs[Keys.ONBOARDING_STATE_JSON] = json
        }
    }

    fun observeOnboardingState(): Flow<String?> =
        dataStore.data.map { it[Keys.ONBOARDING_STATE_JSON] }

    private object Keys {
        val SMART_WAKE_ENABLED = booleanPreferencesKey("smart_wake_window_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val ONBOARDING_STATE_JSON = stringPreferencesKey("onboarding_state_json")
        val DAILY_CHECKIN_ENABLED = booleanPreferencesKey("daily_checkin_enabled")
        val BEDTIME_REMINDER_ENABLED = booleanPreferencesKey("bedtime_reminder_enabled")
        val SELECTED_BEDTIME_HOUR = intPreferencesKey("selected_bedtime_hour")
        val SELECTED_BEDTIME_MINUTE = intPreferencesKey("selected_bedtime_minute")
        val SELECTED_BEDTIME_CYCLES = intPreferencesKey("selected_bedtime_cycles")
        val SELECTED_BEDTIME_DURATION = intPreferencesKey("selected_bedtime_duration_minutes")
        val MORNING_RATING_ENABLED = booleanPreferencesKey("morning_rating_enabled")
        val MORNING_BEDTIME_LOG_ENABLED = booleanPreferencesKey("morning_bedtime_log_enabled")
        val SELECTED_WAKE_HOUR = intPreferencesKey("selected_wake_hour")
        val SELECTED_WAKE_MINUTE = intPreferencesKey("selected_wake_minute")
    }

    companion object {
        const val DEFAULT_NOTIFICATION_TIME = "18:00"
        const val DEFAULT_WAKE_HOUR = 7
    }
}

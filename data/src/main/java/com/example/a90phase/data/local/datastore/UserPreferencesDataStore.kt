package com.example.a90phase.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    private object Keys {
        val SMART_WAKE_ENABLED = booleanPreferencesKey("smart_wake_window_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    companion object {
        const val DEFAULT_NOTIFICATION_TIME = "18:00"
    }
}

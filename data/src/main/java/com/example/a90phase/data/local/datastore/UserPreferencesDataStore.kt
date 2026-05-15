package com.example.a90phase.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    private object Keys {
        val SMART_WAKE_ENABLED = booleanPreferencesKey("smart_wake_window_enabled")
    }
}

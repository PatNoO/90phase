package com.example.a90phase.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.patternInsightsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pattern_insights",
)

@Singleton
class PatternInsightsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore get() = context.patternInsightsDataStore

    suspend fun setPatternInsightsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.PATTERN_INSIGHTS_ENABLED] = enabled }
    }

    fun observePatternInsightsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.PATTERN_INSIGHTS_ENABLED] ?: false }

    suspend fun addDismissedInsightId(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.DISMISSED_INSIGHT_IDS] ?: emptySet()
            prefs[Keys.DISMISSED_INSIGHT_IDS] = current + id
        }
    }

    fun observeDismissedInsightIds(): Flow<Set<String>> =
        dataStore.data.map { it[Keys.DISMISSED_INSIGHT_IDS] ?: emptySet() }

    private object Keys {
        val PATTERN_INSIGHTS_ENABLED = booleanPreferencesKey("pattern_insights_enabled")
        val DISMISSED_INSIGHT_IDS = stringSetPreferencesKey("dismissed_insight_ids")
    }
}

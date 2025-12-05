package com.speedreader.trainer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val READING_DARK_MODE_KEY = booleanPreferencesKey("reading_dark_mode")
        private val DEFAULT_WPM_KEY = intPreferencesKey("default_wpm")
        private val DEFAULT_FONT_SIZE_KEY = intPreferencesKey("default_font_size")
        private val CHUNKING_ENABLED_KEY = booleanPreferencesKey("chunking_enabled")
        private val DEFAULT_CHUNK_SIZE_KEY = intPreferencesKey("default_chunk_size")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val readingDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[READING_DARK_MODE_KEY] ?: false
    }

    val defaultWpmFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_WPM_KEY] ?: 250
    }

    val defaultFontSizeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_FONT_SIZE_KEY] ?: 48
    }

    val chunkingEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CHUNKING_ENABLED_KEY] ?: false
    }

    val defaultChunkSizeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CHUNK_SIZE_KEY] ?: 2
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setReadingDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[READING_DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setDefaultWpm(wpm: Int) {
        android.util.Log.d("SpeedAdjustment", "SettingsRepository: Saving WPM = $wpm")
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_WPM_KEY] = wpm
        }
    }

    suspend fun setDefaultFontSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_FONT_SIZE_KEY] = size
        }
    }

    suspend fun setChunkingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CHUNKING_ENABLED_KEY] = enabled
        }
    }

    suspend fun setDefaultChunkSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CHUNK_SIZE_KEY] = size
        }
    }
}


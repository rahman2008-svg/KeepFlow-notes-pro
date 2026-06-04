package com.example.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "planner_settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val KEY_DARK_MODE = stringPreferencesKey("dark_mode_preference") // "system", "light", "dark"
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val KEY_DAILY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")
    }

    val darkModeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_DARK_MODE] ?: "system"
        }

    val notificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
        }

    val dailyReminderEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_DAILY_REMINDER_ENABLED] ?: true
        }

    val dailyReminderTimeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[KEY_DAILY_REMINDER_TIME] ?: "08:00"
        }

    suspend fun setDarkMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = mode
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_TIME] = time
        }
    }
}

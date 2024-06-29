package com.example.giveawayreminder.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.giveawayreminder.data.PreferenceKeys.IS_LIST_UPDATED
import com.example.giveawayreminder.data.PreferenceKeys.LIST_UPDATE_INTERVAL
import com.example.giveawayreminder.data.PreferenceKeys.NOTIFICATION_HOUR
import com.example.giveawayreminder.data.PreferenceKeys.NOTIFICATION_INTERVAL
import com.example.giveawayreminder.data.PreferenceKeys.OLD_GAME_LIST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
){
    val notificationHourOfDay: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NOTIFICATION_HOUR] ?: "12 am"
        }

    val notificationInterval: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[NOTIFICATION_INTERVAL] ?: "Every Day"
        }

    val listUpdateInterval: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[LIST_UPDATE_INTERVAL] ?: "6 hours"
        }

    // List saved as a Json string
    val oldGameList: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[OLD_GAME_LIST] ?: ""
        }

    val isListSame: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[IS_LIST_UPDATED] ?: false
        }

    suspend fun saveHourOfDay(notificationHourOfDay: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR] = notificationHourOfDay
        }
    }

    suspend fun saveNotificationInterval(notificationInterval: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_INTERVAL] = notificationInterval
        }
    }

    suspend fun saveUpdateInterval(updateInterval: String) {
        dataStore.edit { preferences ->
            preferences[LIST_UPDATE_INTERVAL] = updateInterval
        }
    }

    private companion object {
        const val TAG = "UserPreferencesRepo"
    }
}
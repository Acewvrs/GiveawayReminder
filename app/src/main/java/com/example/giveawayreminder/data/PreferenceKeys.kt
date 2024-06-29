package com.example.giveawayreminder.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val NOTIFICATION_HOUR = stringPreferencesKey("notification_hour")
    val NOTIFICATION_INTERVAL = stringPreferencesKey("notification_interval")
    val LIST_UPDATE_INTERVAL = stringPreferencesKey("list_update_interval")
    val OLD_GAME_LIST = stringPreferencesKey("old_game_list")
    val IS_LIST_UPDATED = booleanPreferencesKey("is_list_updated")
}
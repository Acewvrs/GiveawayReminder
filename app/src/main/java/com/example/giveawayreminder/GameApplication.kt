package com.example.giveawayreminder

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.giveawayreminder.data.AppContainer
import com.example.giveawayreminder.data.GameAppContainer
import com.example.giveawayreminder.data.UserPreferencesRepository

class GameApplication: Application() {
// * Custom app entry point for manual dependency injection
// */
    lateinit var container: AppContainer
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        container = GameAppContainer(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = LAYOUT_PREFERENCE_NAME
        )
    }
}
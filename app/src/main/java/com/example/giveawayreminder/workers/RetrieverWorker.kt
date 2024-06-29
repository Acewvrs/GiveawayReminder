package com.example.giveawayreminder.workers

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.example.giveawayreminder.GameApplication.Companion.dataStore
import com.example.giveawayreminder.data.Game
import com.example.giveawayreminder.data.GamesRepository
import com.example.giveawayreminder.data.NetworkGamesRepository
import com.example.giveawayreminder.data.PreferenceKeys.IS_LIST_UPDATED
import com.example.giveawayreminder.data.PreferenceKeys.OLD_GAME_LIST
import com.example.giveawayreminder.network.GameApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class RetrieverWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork() : Result {
        return try {
            val epicGamesUrl =
                "https://store-site-backend-static.ak.epicgames.com/"

            val retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(epicGamesUrl)
                .build()

            val retrofitService: GameApiService by lazy {
                retrofit.create(GameApiService::class.java)
            }

            val gamesRepository: GamesRepository = NetworkGamesRepository(retrofitService)
            val gamesList = gamesRepository.getGamePromotions()

            // Convert list to JSON string
            val gson = Gson()
            val gamesListJson = gson.toJson(gamesList)

            // save the list
            saveNewGameList(context, gamesListJson, gamesList)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun saveNewGameList(context: Context, gameListJson: String, newGameList: List<Game>) {
        var oldGameList : List<Game> = emptyList()
        val preferences = context.dataStore.data.first()
        val oldGameListJson = preferences[OLD_GAME_LIST] ?: ""

        if (oldGameListJson != "") {
            // Convert JSON string back to new list
            val gson = Gson()
            val listType: Type = object : TypeToken<List<Game>>() {}.type
            oldGameList = gson.fromJson(oldGameListJson, listType)
        }

        // TODO: check if new game was added
        // compare two lists
        val isListUpdated = newGameList != oldGameList

        context.dataStore.edit { preferences ->
            // save new game list as JSON string
            preferences[OLD_GAME_LIST] = gameListJson

            // save whether the old list is the same as the new list (if not, send notification to users)
            preferences[IS_LIST_UPDATED] = isListUpdated
        }
    }
}
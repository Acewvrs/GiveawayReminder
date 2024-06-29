package com.example.giveawayreminder.data

import com.example.giveawayreminder.network.GameApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context

interface AppContainer {
    val gamesRepository : GamesRepository
    val workManagerRepository: WorkManagerRepository
}

class GameAppContainer(context: Context) : AppContainer {
    private val epicGamesUrl =
        "https://store-site-backend-static.ak.epicgames.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(epicGamesUrl)
        .build()

    private val retrofitService: GameApiService by lazy {
        retrofit.create(GameApiService::class.java)
    }

    override val workManagerRepository = WorkManagerRepository(context)

    override val gamesRepository: GamesRepository by lazy {
        NetworkGamesRepository(retrofitService)
    }
}
package com.example.giveawayreminder.data

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.giveawayreminder.network.GameApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import android.content.Context

interface GamesRepository {
    suspend fun getGamePromotions(): List<Game>
}

class NetworkGamesRepository(
    private val gameApiService: GameApiService,
) : GamesRepository {
    private var response: Response<QueryResponse>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGamePromotions(): List<Game> {
        val res = gameApiService.getPromotions()
        response = res // cache response
        return if (res.isSuccessful) {
            res.body()?.data?.catalog?.searchStore?.getGamesCurrentlyOnPromotion() ?: emptyList()
        } else {
            emptyList()
        }
    }
}

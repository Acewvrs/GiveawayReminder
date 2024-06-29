package com.example.giveawayreminder.network

import com.example.giveawayreminder.data.QueryResponse
import retrofit2.Response
import retrofit2.http.GET

interface GameApiService {
    @GET("freeGamesPromotions")
    suspend fun getPromotions(): Response<QueryResponse>
}
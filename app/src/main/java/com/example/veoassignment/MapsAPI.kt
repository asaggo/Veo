package com.example.veoassignment

import android.location.Location
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MapsAPI {

    companion object {
        val BASE_URL = "https://open.mapquestapi.com"
    }

    @Headers("Content-Type: application/json")
    @GET("/directions/v2/route")
    suspend fun getDirection(
        @Query("key") key: String,
        @Query("from") from: Location,
        @Query("to") to: Location
    ): DirectionResponse


    @Headers("Content-Type: application/json")
    @GET("/search/v2/radius")
    suspend fun radiusSearch(
        @Query("key") key: String,
        @Query("maxMatches") maxMatches: Int,
        @Query("origin") origin: Location
    )
}
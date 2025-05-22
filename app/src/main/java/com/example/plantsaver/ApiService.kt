package com.example.plantsaver

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("current.json")
    suspend fun getCurrentWeather(@Query("key")apiKey:String,@Query("q")location:String, @Query("aqi")value:String) : WeatherResponse
/*
    @GET("random.php")
    suspend fun getRandomDrink() : DrinkDetailsResponse

    @GET("lookup.php")
    suspend fun getDrinkDetails(@Query("i")drinkId:String) : DrinkDetailsResponse

 */
}
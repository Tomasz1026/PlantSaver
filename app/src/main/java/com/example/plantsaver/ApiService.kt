package com.example.plantsaver

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.plantsaver.BuildConfig.WEATHER_API_KEY

interface ApiService {
    @GET("current.json?key=${WEATHER_API_KEY}")
    suspend fun getCurrentWeather(@Query("q")location:String, @Query("aqi")value:String) : WeatherResponse
}
package com.example.plantsaver

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String = "",
    val country: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

data class Current(
    val last_updated: String = "",
    val temp_c: Double = 0.0,
    val temp_f: Double = 0.0,
)
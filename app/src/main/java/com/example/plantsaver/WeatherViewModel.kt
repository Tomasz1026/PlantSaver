package com.example.plantsaver

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel: ViewModel() {
    private val _weatherData = mutableStateOf<WeatherResponse?>(null)
    val weatherData: State<WeatherResponse?> = _weatherData


    fun getCurrentWeather() {

        viewModelScope.launch {
            try {
                _weatherData.value = RetrofitInstance.api.getCurrentWeather("71911e68ab6f4494991103240252205", "Poznan", "no")

            } catch(e: Exception) {
                println(e)
            }
        }
    }
}
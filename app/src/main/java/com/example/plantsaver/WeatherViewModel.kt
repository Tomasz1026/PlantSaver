package com.example.plantsaver

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.CountDownTimer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed class LocationState {
    object NoPermission: LocationState()
    object LocationDisabled: LocationState()
    object LocationLoading: LocationState()
    data class LocationAvailable(val location: LatLng): LocationState()
    object Error: LocationState()
}

data class AutocompleteResult(
    val address: String,
    val placeId: String
)

class WeatherViewModel: ViewModel() {
    private val apiKey = "71911e68ab6f4494991103240252205"

    private val _weatherData = mutableStateOf<WeatherResponse?>(null)
    val weatherData: State<WeatherResponse?> = _weatherData
    var weatherDataError = mutableStateOf(false)

    val textSearch= mutableStateOf<String>("")

    var timer = object: CountDownTimer(0, 0) {
        override fun onTick(millisUntilFinished: Long) {
        }
        override fun onFinish() {
        }
    }

    var currentLatLong = mutableStateOf(LatLng(0.0, 0.0))
    val locationAutofill = mutableStateListOf<AutocompleteResult>()

    lateinit var placesClient: PlacesClient

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geoCoder: Geocoder

    var locationState = mutableStateOf<LocationState>(LocationState.LocationAvailable(currentLatLong.value))

    private var job: Job? = null

    fun setCurrentLocation(userData: UserData) {
        println("Location updated inside weatherViewModel")
        currentLatLong.value = LatLng(userData.userLatitude, userData.userLongitude)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        locationState.value = LocationState.LocationLoading
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                locationState.value = if (location == null && locationState.value !is LocationState.LocationAvailable) {
                    LocationState.Error
                } else {
                    LocationState.LocationAvailable(LatLng(location.latitude, location.longitude))
                }
            }
    }

    fun searchPlaces(query: String) {
        job?.cancel()
        locationAutofill.clear()
        job = viewModelScope.launch {
            val request = FindAutocompletePredictionsRequest
                .builder()
                .setQuery(query)
                .build()
            placesClient
                .findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    locationAutofill += response.autocompletePredictions.map {
                        AutocompleteResult(
                            it.getFullText(null).toString(),
                            it.placeId
                        )
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    println(it.cause)
                    println(it.message)
                }
        }
    }

    fun getCoordinates(result: AutocompleteResult) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                if (it != null) {
                    currentLatLong.value = it.place.latLng!!
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun getAddress(latLng: LatLng) {
        viewModelScope.launch {
            val address = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            textSearch.value = address?.get(0)?.getAddressLine(0).toString()
        }
    }

    fun getCurrentWeather() {
        println("try to get weather data")
        viewModelScope.launch {
            try {
                _weatherData.value = RetrofitInstance.api.getCurrentWeather(apiKey, "${currentLatLong.value.latitude},${currentLatLong.value.longitude}", "no")
                weatherDataError.value = false
                timer.cancel()
            } catch(e: Exception) {
                weatherDataError.value = true
                timer = object: CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        println("tik")
                    }

                    override fun onFinish() {
                        getCurrentWeather()
                    }
                }.start()
                println(e)
            }
        }
    }
}
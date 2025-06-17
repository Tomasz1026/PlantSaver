package com.example.plantsaver

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DataBaseViewModel(private val repository: PlantRepository) : ViewModel() {
    private val _plants = mutableStateOf<List<Plant>>(emptyList())
    private val _plantsToWater = mutableStateOf<List<Plant>>(emptyList())
    private val _lastWatering = mutableStateOf<List<Watering>>(emptyList())
    private val _userData = mutableStateOf<UserData>(UserData(1, 0.0, 0.0))
    val plants: State<List<Plant>> = _plants
    val plantsToWater: State<List<Plant>> = _plantsToWater
    val lastWatering: State<List<Watering>> = _lastWatering
    val userData: State<UserData> = _userData
    val plantId = mutableIntStateOf(0)

    init {
        checkUserData()
        loadPlants()
        getPlantsToWater()
    }

    fun loadPlants() {
        viewModelScope.launch {
            _plants.value = repository.getAllPlants()

            if(_plants.value.isNotEmpty()) {
                plantId.intValue = _plants.value.last().plantId
            }
        }
    }

    fun addPlant(
        plantName: String,
        plantSpecies: String,
        plantFrequency: Int
    ) {
        viewModelScope.launch {
            repository.addPlant(plantName, plantSpecies, plantFrequency)
            loadPlants()
            getPlantsToWater()
        }
    }

    fun addPlant(
        plantName: String,
        plantSpecies: String,
        plantDate: Long,
        plantFrequency: Int
    ) {
        viewModelScope.launch {
            repository.addPlant(plantName, plantSpecies, plantDate, plantFrequency)
            loadPlants()
        }
    }

    fun removePlant(plant: Plant) {
        viewModelScope.launch {
            repository.removePlant(plant)
        }
    }

    fun getPlantHistory(plantId: Int) {
        viewModelScope.launch {
            _lastWatering.value = repository.getPlantHistory(plantId)
        }
    }

    fun getPlantsToWater() {
        viewModelScope.launch {
            _plantsToWater.value = repository.getPlantsToWater()
        }
    }

    fun addWatering(plantId: Int) {
        viewModelScope.launch {
            repository.addWatering(plantId)
            getPlantsToWater()
        }
    }

    fun addWatering(plantId: Int, date: Long) {
        viewModelScope.launch {
            repository.addWatering(plantId, date)
            getPlantsToWater()
        }
    }

    fun deleteAllPlant() {
        viewModelScope.launch {
            repository.deleteAllPlant()
        }
    }

    fun deleteAllWatering() {
        viewModelScope.launch {
            repository.deleteAllWatering()
        }
    }

    fun deleteAll() {
        deleteAllWatering()
        deleteAllPlant()
    }

    fun checkUserData() {
        viewModelScope.launch {
            if(repository.getLastUserLocation().isEmpty()){
                repository.addUserLocation()
            }

            _userData.value = repository.getLastUserLocation()[0]
        }
    }

    fun updateUserLocation(latitude:Double, longitude:Double) {
        viewModelScope.launch {
            repository.updateUserLocation(latitude, longitude)
        }
    }
}
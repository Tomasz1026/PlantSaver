package com.example.plantsaver

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class DataBaseViewModel(private val repository: PlantRepository) : ViewModel() {
    private val _plants = mutableStateOf<List<Plant>>(emptyList())
    private val _watering = mutableStateOf<List<Watering>>(emptyList())
    val plants: State<List<Plant>> = _plants
    val watering: State<List<Watering>> = _watering
    val plantId = mutableIntStateOf(0)

    init {
        loadPlants()
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
        plantId.intValue += 1
        addPlant(Plant(plantId.intValue, plantName, plantSpecies, "", plantFrequency, 0))
        _plants.value += Plant(plantId.intValue, plantName, plantSpecies, "", plantFrequency, 0)
    }

    fun addPlant(plant: Plant) {
        viewModelScope.launch {
            repository.addPlant(plant)
        }
    }

    fun removePlant(plant: Plant) {
        viewModelScope.launch {
            repository.removePlant(plant)
        }
    }

    fun getPlantHistory(plantId: Int) {
        viewModelScope.launch {
            _watering.value = repository.getPlantHistory(plantId)
        }
    }
}
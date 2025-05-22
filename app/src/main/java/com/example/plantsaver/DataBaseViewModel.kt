package com.example.plantsaver

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class DataBaseViewModel(private val repository: PlantRepository) : ViewModel() {
    private val _plants = mutableStateOf<List<Plant>>(emptyList())
    val plants: State<List<Plant>> = _plants

    init {
        loadPlants()
    }

    fun loadPlants() {
        viewModelScope.launch {
            _plants.value = repository.getAllPlants()
        }
    }

    fun addDrink(plant: Plant) {
        viewModelScope.launch {
            repository.addPlant(plant)
        }
    }

    fun removePlant(plant: Plant) {
        viewModelScope.launch {
            repository.removePlant(plant)
        }
    }
}
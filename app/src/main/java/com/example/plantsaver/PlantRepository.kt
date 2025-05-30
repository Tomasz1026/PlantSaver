package com.example.plantsaver

class PlantRepository(private val db: LocalDB) {
    suspend fun getAllPlants() = db.plantDao().getAll()
    suspend fun addPlant(plant: Plant) = db.plantDao().insert(plant)
    suspend fun removePlant(plant: Plant) = db.plantDao().delete(plant)
}
package com.example.plantsaver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants")
    suspend fun getAll(): List<Plant>

    @Query("SELECT * FROM plants WHERE (strftime('%s', 'now') - plants.frequency * 86400) > COALESCE((SELECT MAX(date) FROM watering WHERE watering.plant_id = plants.plantId), 0)")
    suspend fun getPlantsToWater(): List<Plant>

    @Query("INSERT INTO plants(name, species, frequency) VALUES(:name, :species, :frequency)")
    suspend fun insert(name:String, species:String, frequency: Int)

    @Query("INSERT INTO plants(name, species, date, frequency) VALUES(:name, :species, :date, :frequency)")
    suspend fun insert(name:String, species:String, date:Long, frequency: Int)

    @Query("DELETE FROM plants")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(plant: Plant)
}
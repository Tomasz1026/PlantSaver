package com.example.plantsaver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WateringDao {
    @Query("SELECT * FROM watering WHERE plant_id LIKE (:plantId)")
    suspend fun getPlantHistory(plantId: Int): List<Watering>

    @Insert
    suspend fun insert(vararg watering: Watering)

    @Delete
    suspend fun delete(watering: Watering)
}
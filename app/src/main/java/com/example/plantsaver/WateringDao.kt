package com.example.plantsaver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WateringDao {
    @Query("SELECT * FROM watering WHERE plant_id LIKE (:plantId) ORDER BY date DESC LIMIT 1")
    suspend fun getPlantHistory(plantId: Int): List<Watering>

    @Query("INSERT INTO watering(plant_id, date) VALUES(:plantId, :date)")
    suspend fun insert(plantId: Int, date: Long)

    @Query("INSERT INTO watering(plant_id) VALUES(:plantId)")
    suspend fun insert(plantId: Int)

    @Query("DELETE FROM watering")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(vararg watering: Watering)

    @Delete
    suspend fun delete(watering: Watering)
}
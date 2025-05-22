package com.example.plantsaver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants")
    suspend fun getAll(): List<Plant>

    @Insert
    suspend fun insert(vararg plant: Plant)

    @Delete
    suspend fun delete(plant: Plant)
}
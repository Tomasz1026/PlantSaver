package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watering")
data class Watering(
    @PrimaryKey val wateringId: Int,
    @ColumnInfo(name = "plant_id") val wateringPlantId: Int,
    @ColumnInfo(name = "date") val wateringDate: String
)
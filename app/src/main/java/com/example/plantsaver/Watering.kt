package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watering")
data class Watering(
    @PrimaryKey(autoGenerate = true) val wateringId: Int,
    @ColumnInfo(name = "plant_id") val wateringPlantId: Int,
    @ColumnInfo(name = "date", defaultValue = "(strftime('%s', 'now'))") val wateringDate: Long
)
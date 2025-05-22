package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey val plantId: Int,
    @ColumnInfo(name = "plant_name") val plantName: String,
    @ColumnInfo(name = "plant_species") val plantSpecies: String
)

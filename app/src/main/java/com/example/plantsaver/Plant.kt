package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey val plantId: Int,
    @ColumnInfo(name = "name") val plantName: String,
    @ColumnInfo(name = "species") val plantSpecies: String,
    @ColumnInfo(name = "date") val plantDate: String,
    @ColumnInfo(name = "frequency") val plantFrequency: Int,
    @ColumnInfo(name = "streak") val plantStreak: Int

)

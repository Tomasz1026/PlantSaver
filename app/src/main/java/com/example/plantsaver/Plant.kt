package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val plantId: Int,
    @ColumnInfo(name = "name") val plantName: String,
    @ColumnInfo(name = "species") val plantSpecies: String,
    @ColumnInfo(name = "date", defaultValue = "(strftime('%s', 'now'))") val plantDate: Long,
    @ColumnInfo(name = "frequency") val plantFrequency: Int,
    @ColumnInfo(name = "streak", defaultValue = "0") val plantStreak: Int
)

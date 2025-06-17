package com.example.plantsaver

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey val userId: Int,
    @ColumnInfo(name = "latitude") val userLatitude: Double,
    @ColumnInfo(name = "longitude") val userLongitude: Double
)
package com.example.plantsaver

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Plant::class, Watering::class],
    version = 3,
)
abstract class LocalDB : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun wateringDao(): WateringDao
}
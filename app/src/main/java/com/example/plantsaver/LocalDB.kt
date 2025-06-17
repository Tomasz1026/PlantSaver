package com.example.plantsaver

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Plant::class, Watering::class, UserData::class],
    version = 12,
)

abstract class LocalDB : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun wateringDao(): WateringDao
    abstract fun userDataDao(): UserDataDao
}
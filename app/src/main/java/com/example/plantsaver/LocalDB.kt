package com.example.plantsaver

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Plant::class],
    version = 1,
)
abstract class LocalDB : RoomDatabase() {
    abstract fun plantDao(): PlantDao
}
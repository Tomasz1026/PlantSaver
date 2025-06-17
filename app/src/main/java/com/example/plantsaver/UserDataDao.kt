package com.example.plantsaver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDataDao {
    @Query("SELECT * FROM user_data")
    suspend fun getLastUserLocation(): List<UserData>

    @Query("UPDATE user_data SET latitude=:latitude, longitude=:longitude WHERE userId=1")
    suspend fun updateUserLocation(latitude:Double, longitude:Double)

    @Insert
    suspend fun insert(vararg userData: UserData)

    @Delete
    suspend fun delete(userData: UserData)
}
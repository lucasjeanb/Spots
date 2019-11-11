package com.example.spots.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SpotDAO{
    @Query("SELECT * FROM spot_table ORDER BY id ASC")
    fun getSpots(): LiveData<List<Spot>>

    @Query("DELETE FROM spot_table")
    fun deleteAllSpots()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setSpot(newSpot: Spot)
}
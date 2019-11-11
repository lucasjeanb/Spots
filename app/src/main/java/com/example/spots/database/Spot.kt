package com.example.spots.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spot_table")
data class Spot(
    @PrimaryKey(autoGenerate = true)
    var id:Int,

    @ColumnInfo(name = "spot_name")
    var spotName: String

)

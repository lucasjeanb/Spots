package com.bonnelife.spots.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spot_table")
data class Spot(
    @PrimaryKey(autoGenerate = true)
    var id:Int?,

    @ColumnInfo(name = "spot_name")
    var spotName: String,

    @ColumnInfo(name = "favorite_latitude") var spotLatitude: Double,
    @ColumnInfo(name = "favorite_longitude") var spotLongitude: Double

)

{
    constructor(
        spotName: String, spotLatitude: Double, spotLongitude: Double
    ) : this(null, spotName, spotLatitude, spotLongitude)
}

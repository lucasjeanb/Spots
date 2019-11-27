package com.bonnelife.spots.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Spot::class], version = 1, exportSchema = false)
abstract class SpotDatabase : RoomDatabase() {

    abstract fun spotsDao(): SpotDAO

    companion object {

        @Volatile
        private var INSTANCE: SpotDatabase? = null

        fun getDatabase(context: Context): SpotDatabase? {
            if (INSTANCE == null) {
                synchronized(SpotDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            SpotDatabase::class.java, "spot_database"
                        )
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}
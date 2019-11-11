package com.example.spots.database

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SpotRepository(application: Application) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var spotDao: SpotDAO?

    init {
        val db = SpotDatabase.getDatabase(application)
        spotDao = db?.spotsDao()
    }

    fun getSpots() = spotDao?.getSpots()

    fun setSpot(spot: Spot) {
        launch  { setSpotBG(spot) }
    }

    private suspend fun setSpotBG(spot: Spot){
        withContext(Dispatchers.IO){
            spotDao?.setSpot(spot)
        }
    }

}
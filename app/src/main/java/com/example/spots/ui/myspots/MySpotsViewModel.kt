package com.example.spots.ui.myspots

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.spots.database.Spot
import com.example.spots.database.SpotRepository
import com.mayowa.android.locationwithlivedata.LocationData

class MySpotsViewModel (application: Application) : AndroidViewModel(application) {

    private var repository:SpotRepository = SpotRepository(application)
    private val locationData = LocationData(application)


    fun getSpots() = repository.getSpots()

    fun delete(spot: Spot) {repository.delete(spot)}

    fun setSpot(spot: Spot) { repository.setSpot(spot)}

    fun getLocationData() = locationData
}
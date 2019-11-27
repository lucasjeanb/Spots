package com.bonnelife.spots.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bonnelife.spots.database.Spot
import com.bonnelife.spots.database.SpotRepository
import com.mayowa.android.locationwithlivedata.LocationData

class MapViewModel (application: Application) : AndroidViewModel(application) {

    private var repository: SpotRepository = SpotRepository(application)
    private val locationData = LocationData(application)


    fun getSpots() = repository.getSpots()

    fun delete(spot: Spot) {repository.delete(spot)}

    fun setSpot(spot: Spot) { repository.setSpot(spot)}

    fun getLocationData() = locationData


}
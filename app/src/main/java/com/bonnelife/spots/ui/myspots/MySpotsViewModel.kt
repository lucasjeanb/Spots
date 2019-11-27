package com.bonnelife.spots.ui.myspots

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bonnelife.spots.database.Spot
import com.bonnelife.spots.database.SpotRepository
import com.mayowa.android.locationwithlivedata.LocationData

class MySpotsViewModel (application: Application) : AndroidViewModel(application) {

    val latitudeViewModel = MutableLiveData<Double>()
    val longitudeViewModel = MutableLiveData<Double>()
    private var repository:SpotRepository = SpotRepository(application)
    private val locationData = LocationData(application)

    fun coord(item: Double) {
        latitudeViewModel.value = item
        longitudeViewModel.value = item
    }

    fun getSpots() = repository.getSpots()

    fun delete(spot: Spot) {repository.delete(spot)}

    fun setSpot(spot: Spot) { repository.setSpot(spot)}

    fun getLocationData() = locationData
}
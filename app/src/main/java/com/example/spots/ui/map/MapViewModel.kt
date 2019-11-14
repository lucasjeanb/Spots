package com.example.spots.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spots.database.Spot
import com.example.spots.database.SpotRepository
import com.mayowa.android.locationwithlivedata.LocationData

class MapViewModel (application: Application) : AndroidViewModel(application) {

    private var repository: SpotRepository = SpotRepository(application)
    private val locationData = LocationData(application)


    fun getSpots() = repository.getSpots()

    fun delete(spot: Spot) {repository.delete(spot)}

    fun setSpot(spot: Spot) { repository.setSpot(spot)}

    fun getLocationData() = locationData


}
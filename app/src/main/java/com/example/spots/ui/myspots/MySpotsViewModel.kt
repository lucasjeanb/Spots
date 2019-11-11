package com.example.spots.ui.myspots

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spots.database.Spot
import com.example.spots.database.SpotRepository

class MySpotsViewModel (application: Application) : AndroidViewModel(application) {

    private var repository:SpotRepository = SpotRepository(application)

    fun getSpots() = repository.getSpots()

    fun setSpot(spot: Spot) { repository.setSpot(spot)}

}
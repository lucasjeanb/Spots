package com.example.spots.ui.myspots


import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spots.R
import com.example.spots.adapter.MySpotsAdapter
import com.example.spots.database.Spot
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_add_location.*
import kotlinx.android.synthetic.main.fragment_myspots.*

/**
 * A simple [Fragment] subclass.
 */
class AddLocationFragment : Fragment() {

    private lateinit var homeViewModel: MySpotsViewModel
    var latitude: Double = 1.0
    var longitude: Double = 1.0
    var latitudeVM: Double = 1.0
    var longitudeVM: Double = 1.0


    val viewModel: MySpotsViewModel by lazy {
        ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(MySpotsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = activity?.run {
            ViewModelProviders.of(this)[MySpotsViewModel::class.java]
        }?: throw Exception("Invalid Activity")

        homeViewModel.latitudeViewModel.observe(this, Observer {
            latitude = homeViewModel.latitudeViewModel.value!!
            Log.d("LOG_X", latitude.toString())
        })
        homeViewModel.longitudeViewModel.observe(this, Observer {
            longitude = homeViewModel.longitudeViewModel.value!!
            Log.d("LOG_X", longitude.toString())

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
          startLocationUpdate()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectLocation_button.setOnClickListener {
            var selectLocationFragment = SelectLocationFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.selectLocation_framelayout, selectLocationFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        currentLocation_button.setOnClickListener {
            locationSelect_textview.text =  "$latitude, $longitude"
        }

        confirmLocation_button.setOnClickListener {
            val spotName = locationName_edittext.text.toString()
            val newSpot = Spot(spotName, latitude, longitude)
            homeViewModel.setSpot(newSpot)
            fragmentManager?.popBackStack()
        }
    }

    private fun startLocationUpdate() {
        homeViewModel.getLocationData().observe(this, Observer {
            longitude = it.longitude
            latitude = it.latitude
        })
    }




}

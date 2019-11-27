package com.bonnelife.spots.ui.myspots


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.bonnelife.spots.R
import com.bonnelife.spots.util.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_select_location.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SelectLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var homeViewModel: MySpotsViewModel
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var selectLatitude: Double = 0.0
    var selectLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_select_location, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        homeViewModel =activity?.run {
            ViewModelProviders.of(this).get(MySpotsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        startLocationUpdate()

        okSelect_button.setOnClickListener {
            if (selectLatitude != 0.0){
                homeViewModel.latitudeViewModel.value = selectLatitude
                homeViewModel.longitudeViewModel.value = selectLongitude
                Log.d("LOG_X", "$selectLatitude, $selectLongitude")
                requireContext().toast("Location on Map Selected")

                fragmentManager?.popBackStack()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        setMapClick(map)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 4f))
        map.animateCamera(CameraUpdateFactory.zoomTo(11f))

        map.setMyLocationEnabled(true)

        map.setPadding(0,80,30,0)

    }

    private fun startLocationUpdate() {
            homeViewModel.getLocationData().observe(this, Observer {
                longitude = it.longitude
                latitude = it.latitude
            })
    }
    private fun setMapClick(map:GoogleMap) {
        map.setOnMapClickListener { latLng ->
            selectLatitude = latLng.latitude
            selectLongitude = latLng.longitude
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long:%2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            /*
            val newPerson = FavoriteEntity(latLng.latitude, latLng.longitude, "Dropped Pin")
            viewModel.addPlace(newPerson)

             */
            map.clear()

            map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Dropped Pin")
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
                Log.d("LOG_X", "$selectLatitude, $selectLongitude")
        }
    }
}

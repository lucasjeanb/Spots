package com.example.spots.ui.myspots


import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.example.spots.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SelectLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var homeViewModel: MySpotsViewModel
    var  singleMarker = false
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(MySpotsViewModel::class.java)
        startLocationUpdate()

        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_select_location, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        setMapLongClick(map)

        map.setMyLocationEnabled(true)
    }

    private fun startLocationUpdate() {
            homeViewModel.getLocationData().observe(this, Observer {
                longitude = it.longitude
                latitude = it.latitude
                Log.d("LOG_X", "$latitude, $longitude")
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 4f))
                map.animateCamera(CameraUpdateFactory.zoomTo(11f))
            })
    }
    lateinit var marker: Marker

    private fun setMapLongClick(map:GoogleMap) {
        map.setOnMapClickListener { latLng ->
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
                Log.d("LOG_X", latLng.toString())


        }
        }

}

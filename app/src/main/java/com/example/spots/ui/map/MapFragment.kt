package com.example.spots.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.spots.R
import com.example.spots.database.Spot
import com.example.spots.database.model.SpotDTO
import com.example.spots.ui.myspots.MySpotsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_location.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.net.URL

class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var viewModel: MapViewModel
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    var latitude: Double = 0.0
    var longitude: Double = 0.0



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_map, container, false)
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)

        profile_button.setOnClickListener {
            var profileFragment = ProfileFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.profile_framelayout, profileFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        enableMyLocation()

        startLocationUpdate()

        map.setPadding(0,80,30,0)

        addMarker()
        //viewModel.getSpots()?.observe(this, Observer<List<Spot>> { this.addMarker(it) })

        }

    private fun addMarker(){

        var spotDTOs: ArrayList<SpotDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()
        var firestore = FirebaseFirestore.getInstance()


        firestore?.collection("spots")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    spotDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(SpotDTO::class.java)
                        spotDTOs.add(item!!)
                        var imageUrl = Uri.parse(item.imageUrl)
                        var bmp : Bitmap
                        contentUidList.add(snapshot.id)
                        Glide.with(this)
                            .asBitmap()
                            .load(imageUrl)
                            .into(object : CustomTarget<Bitmap>(){
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    bmp = resource
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(item.latitude!!, item.longitude!!))
                                            .title(item.message)
                                            .snippet("${item.latitude!!}, ${item.longitude!!}")
                                            .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                                    )
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // this is called when imageView is cleared on lifecycle call or for
                                    // some other reason.
                                    // if you are referencing the bitmap somewhere else too other than this imageView
                                    // clear it here as you can no longer have the bitmap
                                }
                            })


                    }
                }
    }

    private fun startLocationUpdate() {
        viewModel.getLocationData().observe(this, Observer {
            longitude = it.longitude
            latitude = it.latitude

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 4f))
            map.animateCamera(CameraUpdateFactory.zoomTo(11f))
        })

    }

    private fun enableMyLocation() {
        if (isPermissionGranted()){
            map.setMyLocationEnabled(true)
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                enableMyLocation()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.context!!,
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }
}

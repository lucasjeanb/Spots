package com.example.spots.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.spots.R
import com.example.spots.database.Spot
import com.example.spots.database.model.ContentDTO
import com.example.spots.database.model.SpotDTO
import com.example.spots.ui.myspots.MySpotsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.contact_item_view_layout.view.*
import kotlinx.android.synthetic.main.contact_item_view_layout.view.contactName_textview
import kotlinx.android.synthetic.main.contact_item_view_layout.view.contact_imageview
import kotlinx.android.synthetic.main.contact_item_view_layout.view.coord_textview
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.select_item_view_layout.view.*
import kotlinx.android.synthetic.main.sheet_map.view.*

class MapFragment : Fragment(), OnMapReadyCallback {

    lateinit var viewModel: MapViewModel
    private lateinit var map: GoogleMap
    private val REQUEST_LOCATION_PERMISSION = 1
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var firestore : FirebaseFirestore? = null
    var uid : String? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid


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

    override fun onResume() {
        super.onResume()
        initComponent()

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
                        var imageView :ImageView
                        spotDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                        map.addMarker(
                            MarkerOptions()
                                .position(LatLng(item.latitude!!, item.longitude!!))
                                .title(item.message)
                                .snippet(item.userId)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        )
                    }
                }
    }

    private fun initComponent() {
        // get the bottom sheet view
        val llBottomSheet = requireActivity().findViewById(R.id.bottom_sheet) as LinearLayout
        // init the bottom sheet behavior
        var bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        llBottomSheet.select_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        llBottomSheet.select_recyclerview.layoutManager = LinearLayoutManager(activity)

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(p0: View, p1: Int) {
            }
        })

        fab_directions.setOnClickListener {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)

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


    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var spotDTOs : ArrayList<SpotDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {

            firestore?.collection("spots")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    spotDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(SpotDTO::class.java)
                        spotDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }

            firestore?.collection("userInfo")
                ?.document(uid!!)?.collection("friends")
                ?.whereEqualTo("friend",true)
                ?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.select_item_view_layout,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.contactName_textview.text = contentDTOs[p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().circleCrop()).into(viewholder.contact_imageview)

            //Explain of content
            viewholder.coord_textview.text = contentDTOs[p1].timestamp.toString()
            viewholder.select_itemview.setOnClickListener {
               // addMarkerFriend(contentDTOs[p1].userId.toString())
            }


        }

    }
    private fun addMarkerFriend(userId: String){

        var spotDTOs: ArrayList<SpotDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()
        var firestore = FirebaseFirestore.getInstance()


        firestore?.collection("spots").whereEqualTo("userId", userId)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                spotDTOs.clear()
                contentUidList.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(SpotDTO::class.java)
                    var imageView :ImageView
                    spotDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(item.latitude!!, item.longitude!!))
                            .title(item.message)
                            .snippet(item.userId)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }
            }
    }


}

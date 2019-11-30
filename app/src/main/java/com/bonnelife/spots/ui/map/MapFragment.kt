package com.bonnelife.spots.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bonnelife.spots.R
import com.bonnelife.spots.database.model.ContentDTO
import com.bonnelife.spots.database.model.SpotDTO
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.contact_item_view_layout.view.contactName_textview
import kotlinx.android.synthetic.main.contact_item_view_layout.view.contact_imageview
import kotlinx.android.synthetic.main.contact_item_view_layout.view.coord_textview
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.myspot_item_view_layout.view.*
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
    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)

        val llBottomSheet = requireActivity().findViewById(R.id.bottom_sheet) as LinearLayout
        var bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)

        fab_directions.setOnClickListener {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED)
            fab_directions.visibility = View.GONE
        }
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
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        firestore?.collection("userInfo")
            ?.document(uid!!)?.collection("friends")
            ?.whereEqualTo("friend",true)
            ?.orderBy("userId")
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                    firestore?.collection("spots")
                        ?.whereEqualTo("userId",item.userId)
                        ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            spotDTOs.clear()
                            contentUidList.clear()
                            //Sometimes, This code return null of querySnapshot when it signout
                            if (querySnapshot == null) return@addSnapshotListener

                            for (snapshot1 in querySnapshot!!.documents) {
                                var spotitem = snapshot1.toObject(SpotDTO::class.java)
                                var imageView :ImageView
                                spotDTOs.add(spotitem!!)
                                contentUidList.add(snapshot1.id)

                                if (isAdded) {

                                    Glide.with(requireActivity())
                                        .asBitmap()
                                        .load(spotitem.imageUrl)
                                        .into(object : CustomTarget<Bitmap>() {
                                            override fun onResourceReady(
                                                resource: Bitmap,
                                                transition: Transition<in Bitmap>?
                                            ) {
                                                map.addMarker(
                                                    MarkerOptions()
                                                        .position(
                                                            LatLng(
                                                                spotitem.latitude!!,
                                                                spotitem.longitude!!
                                                            )
                                                        )
                                                        .title(spotitem.message)
                                                        .snippet(spotitem.userId)
                                                        .icon(
                                                            BitmapDescriptorFactory.fromBitmap(
                                                                createCustomMarker(
                                                                    requireContext(),
                                                                    resource,
                                                                    "test"
                                                                )
                                                            )
                                                        )
                                                )
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {
                                                map.addMarker(
                                                    MarkerOptions()
                                                        .position(
                                                            LatLng(
                                                                spotitem.latitude!!,
                                                                spotitem.longitude!!
                                                            )
                                                        )
                                                        .title(spotitem.message)
                                                        .snippet(spotitem.userId)
                                                        .icon(
                                                            BitmapDescriptorFactory.defaultMarker(
                                                                BitmapDescriptorFactory.HUE_BLUE
                                                            )
                                                        )
                                                )
                                            }
                                        })
                                }

                            }
                        }
                }
            }

    }

    fun createCustomMarker(context:Context, ressource:Bitmap, _name:String):Bitmap {
        val marker = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.custom_marker_layout, null)
        val markerImage = marker.findViewById(R.id.user_dp) as CircleImageView
        markerImage.setImageBitmap(ressource)
        val txt_name = marker.findViewById(R.id.name) as TextView
        txt_name.setText(_name)
        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        marker.setLayoutParams(ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT))
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        marker.buildDrawingCache()
        val bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        marker.draw(canvas)
        return bitmap
    }

    private fun initComponent() {
        // get the bottom sheet view
        val llBottomSheet = requireActivity().findViewById(R.id.bottom_sheet) as LinearLayout
        // init the bottom sheet behavior
        var bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)


        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        llBottomSheet.select_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        llBottomSheet.select_recyclerview.layoutManager = LinearLayoutManager(activity)

        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.peekHeight = 400
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            @SuppressLint("RestrictedApi")
            override fun onStateChanged(p0: View, p1: Int) {
                bottomSheetBehavior.isHideable == false
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)

                    fab_directions.visibility = View.GONE
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN)
                fab_directions.visibility = View.VISIBLE
            }

        })
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

            firestore?.collection("userInfo")
                ?.document(uid!!)?.collection("friends")
                ?.whereEqualTo("friend",true)
                ?.orderBy("userId")
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
            var contentDTOuid = contentDTOs[p1].uid
            var contentDTOuser = contentDTOs[p1].userId

            //UserId
            viewholder.contactName_textview.text = contentDTOs[p1].userId

            //Image
            Glide.with(this@MapFragment).load(contentDTOs[p1].imageUrl).apply(RequestOptions().circleCrop()).into(viewholder.contact_imageview)

            viewholder.addSelect_imageview.setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Are you sure you want to remove $contentDTOuser ?")
                    setPositiveButton("Yes") { _, _ ->
                        var friendDTO = ContentDTO.Friend()
                        friendDTO.userId = contentDTOuser
                        friendDTO.uid = contentDTOuid
                        friendDTO.friend = false
                        friendDTO.timestamp = System.currentTimeMillis()
                        FirebaseFirestore.getInstance().collection("userInfo").document(uid!!)
                            .collection("friends").document(contentDTOuid!!).set(friendDTO)
                            ?.addOnSuccessListener {
                                Toast.makeText(requireContext(), "Friend Removed", Toast.LENGTH_SHORT).show()
                                //notifyItemRemoved(p1)
                                val llBottomSheet = requireActivity().findViewById(R.id.bottom_sheet) as LinearLayout
                                var bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN)
                            }
                    }
                    setNegativeButton("Cancel") { _, _ ->
                    }
                }.create().show()
            }
            //Explain of content
            viewholder.coord_textview.text = contentDTOs[p1].timestamp.toString()
            viewholder.select_itemview.setOnClickListener {
               // addMarkerFriend(contentDTOs[p1].userId.toString())
                //favoriteEvent(p1)
            }
        }
        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("userInfo")?.document(contentUidList[position])?.collection("friends")
                ?.document(uid!!)
            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                Log.d("LOG_X",contentDTO?.userId.toString())

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
    }

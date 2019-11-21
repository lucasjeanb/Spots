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
import com.example.spots.database.model.SpotDTO
import com.example.spots.util.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_add_location.*
import kotlinx.android.synthetic.main.fragment_myspots.*
import kotlinx.android.synthetic.main.fragment_profile.*

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
        fragmentadd_background_click.setOnClickListener{
            fragmentManager?.popBackStack()
        }
        selectLocation_button.setOnClickListener {
            var selectLocationFragment = SelectLocationFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.selectLocation_framelayout, selectLocationFragment)
                ?.addToBackStack(null)
                ?.commit()
            locationSelect_textview.text =  "Location on Map Selected"

        }

        currentLocation_button.setOnClickListener {
            val currentLocation = "$latitude, $longitude"
            locationSelect_textview.text =  "Current Location Selected"
            requireContext().toast("Current Location Selected")

        }

        confirmLocation_button.setOnClickListener {
            val spotName = locationName_edittext.text.toString().trim()

            if (spotName.isEmpty()) {
                locationName_edittext.error = "Location Name Required"
                locationName_edittext.requestFocus()
                return@setOnClickListener
            }
            val newSpot = Spot(spotName, latitude, longitude)
            spotOnDB(latitude, longitude, spotName)
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

    fun spotOnDB(lat: Double, long: Double, message: String){
        var spotDTO = SpotDTO()
        spotDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        spotDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        spotDTO.message = message
        spotDTO.latitude = lat
        spotDTO.longitude = long
        spotDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("spots").document().set(spotDTO)
    }




}

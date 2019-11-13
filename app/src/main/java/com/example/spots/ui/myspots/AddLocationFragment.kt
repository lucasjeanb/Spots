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

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var coordinate: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    val viewModel: MySpotsViewModel by lazy {
        ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(MySpotsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLastLocation()

        selectLocation_button.setOnClickListener {
            var bundle = Bundle()
            arguments?.putDouble("latitude", latitude)

            var selectLocationFragment = SelectLocationFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.selectLocation_framelayout, selectLocationFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        currentLocation_button.setOnClickListener {
            locationSelect_textview.text = coordinate

        }

        confirmLocation_button.setOnClickListener {
            val spotName = locationName_edittext.text.toString()
            val newSpot = Spot(spotName, latitude, longitude)
            viewModel.setSpot(newSpot)
        }
    }
    private fun getLastLocation() {

                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    var location: Location? = task.result

                    coordinate = "${location?.latitude.toString()}, ${location?.longitude.toString()}"
                    latitude = location!!.latitude
                    longitude = location!!.longitude
                    Log.d("LOG_X", coordinate)

                }
            }




}

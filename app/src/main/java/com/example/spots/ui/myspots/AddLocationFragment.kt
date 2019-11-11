package com.example.spots.ui.myspots


import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.spots.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_add_location.*

/**
 * A simple [Fragment] subclass.
 */
class AddLocationFragment : Fragment() {

    lateinit var mFusedLocationClient: FusedLocationProviderClient

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

        currentLocation_button.setOnClickListener {
            getLastLocation()
        }
    }
    private fun getLastLocation() {

                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    var location: Location? = task.result

                        Log.d("LOG_X", location?.latitude.toString() + location?.longitude.toString())
                    locationSelect_textview.text = "${location?.latitude.toString()}, ${location?.longitude.toString()}"

                }
            }


}

package com.example.spots.ui.myspots

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spots.R
import com.example.spots.database.model.SpotDTO
import com.example.spots.util.logout
import com.example.spots.util.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_myspots.*
import kotlinx.android.synthetic.main.fragment_myspots.view.*
import kotlinx.android.synthetic.main.myspot_item_view_layout.view.*
import kotlinx.android.synthetic.main.myspot_item_view_layout.view.coord_textview
import kotlinx.android.synthetic.main.select_item_view_layout.view.*


class MySpotsFragment : Fragment() {

    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_myspots, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.myspots_recyclerview.adapter = MySpotsRecyclerViewAdapter()
        view.myspots_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLocation_fab.setOnClickListener {
            var addLocationFragment = AddLocationFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.addLocation_framelayout, addLocationFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    inner class MySpotsRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var spotDTOs: ArrayList<SpotDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("spots")
                ?.whereEqualTo("uid",uid)
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
        }


        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(activity).inflate(R.layout.myspot_item_view_layout, p0, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return spotDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.spotName_textview.text = spotDTOs[p1].message


            //Explain of content
            viewholder.coord_textview.text = "${spotDTOs[p1].latitude.toString()}, ${spotDTOs[p1].longitude.toString()}"

            viewholder.remove_imageview.setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("Are you sure you want to remove this spot?")
                    setPositiveButton("Yes") { _, _ ->
                        firestore?.collection("spots")?.document(spotDTOs[p1].timestamp.toString())?.delete()
                            ?.addOnSuccessListener {
                                Toast.makeText(requireContext(), "Spot Successfully Removed", Toast.LENGTH_SHORT).show()
                                notifyDataSetChanged()
                            }
                    }
                    setNegativeButton("Cancel") { _, _ ->
                    }
                }.create().show()
            }

        }
    }
}
package com.example.spots.ui.myspots

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spots.R
import com.example.spots.adapter.MySpotsAdapter
import com.example.spots.database.Spot
import kotlinx.android.synthetic.main.fragment_myspots.*

class MySpotsFragment : Fragment(), MySpotsAdapter.SpotsAdapterDelegate {


    private lateinit var homeViewModel: MySpotsViewModel
    lateinit var adapter : MySpotsAdapter
    val viewModel: MySpotsViewModel by lazy {
        ViewModelProvider(requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(MySpotsViewModel::class.java)
    }

    override fun spotsSelect(spot: Spot) {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.getSpots()?.observe(this, Observer<List<Spot>> { this.renderSpots(it) })
        homeViewModel =
            ViewModelProviders.of(this).get(MySpotsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_myspots, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerViewItemTouchListener()

        addLocation_fab.setOnClickListener {
            var addLocationFragment = AddLocationFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.addLocation_framelayout, addLocationFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }



    fun renderSpots(spots: List<Spot>?){

        adapter = MySpotsAdapter(null,requireContext(), spots)
        val layoutManager = LinearLayoutManager(context?.applicationContext)
        layoutManager.stackFromEnd = true
        myspots_recyclerview.layoutManager = layoutManager
        myspots_recyclerview.adapter = adapter
    }

    private fun setRecyclerViewItemTouchListener() {

        //1
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                //2
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //3
                homeViewModel.delete(adapter.getSpotAt(viewHolder.adapterPosition))
            }
        }

        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(myspots_recyclerview)
    }
}
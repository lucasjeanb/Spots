package com.example.spots.ui.myspots

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.spots.R
import kotlinx.android.synthetic.main.fragment_myspots.*

class MySpotsFragment : Fragment() {

    private lateinit var homeViewModel: MySpotsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(MySpotsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_myspots, container, false)
        val textView: TextView = root.findViewById(R.id.text_myspots)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
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
}
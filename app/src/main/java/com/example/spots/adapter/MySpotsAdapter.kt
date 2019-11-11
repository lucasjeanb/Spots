package com.example.spots.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spots.R
import com.example.spots.database.Spot

class MySpotsAdapter (private val context: Context, private val spots: List<Spot>?) : RecyclerView.Adapter<MySpotsAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, index: Int): ViewHolder {
        val rootView = LayoutInflater.from(viewGroup.context).inflate(R.layout.myspot_item_view_layout, viewGroup, false)
        return ViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return spots?.size!!
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, index: Int) {
        viewHolder.itemView.animation = AnimationUtils.loadAnimation(context,R.anim.item_animation_fall_down)
        viewHolder.spotName.text = spots?.get(index)?.spotName
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var spotName: TextView = itemView.findViewById(R.id.spotName_textview) as TextView
        var spotName: TextView = itemView.findViewById(R.id.coord_textview)asTextView
    }

}
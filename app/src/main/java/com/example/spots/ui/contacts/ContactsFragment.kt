package com.example.spots.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.spots.R
import com.example.spots.database.model.ContentDTO
import com.example.spots.util.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.contact_item_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ContactsFragment : Fragment() {

    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_contacts, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.contact_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.contact_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var friendDTOs: ArrayList<ContentDTO.Friend> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("userInfo")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context)
                .inflate(R.layout.contact_item_view_layout, p0, false)
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
            Glide.with(requireContext()).load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().circleCrop()).into(viewholder.contact_imageview)

            //Explain of content
            viewholder.coord_textview.text = contentDTOs[p1].timestamp.toString()

            viewholder.add_imageview.setOnClickListener {
                friendEvent(p1,viewholder, true)
                Toast.makeText(requireContext(), "${contentDTOs[p1].userId} have been added", Toast.LENGTH_SHORT).show()

            }

            viewholder.remove_imageview.setOnClickListener {
                friendEvent(p1,viewholder, false)
                Toast.makeText(requireContext(), "${contentDTOs[p1].userId} have been removed", Toast.LENGTH_SHORT).show()

            }

            /*
            firestore?.collection("userInfo")?.document(contentDTOs[p1].uid.toString())
                ?.collection("friends")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    friendDTOs.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        var item = snapshot.toObject(ContentDTO.Friend::class.java)
                        friendDTOs.add(item!!)
                        Log.d("LOG_X", "${item.userId}, ${item.friend}")
                        if (item.friend == false) {
                            viewholder.friend_imageview?.setImageResource(R.drawable.ic_remove)
                        }
                        if (item.friend == true) {
                            viewholder.friend_imageview?.setImageResource(R.drawable.ic_add)
                        }
                    }
                }

             */
        }

        fun friendEvent(position: Int, view: View, boolean: Boolean) {
            var friendDTO = ContentDTO.Friend()

            firestore?.collection("userInfo")?.document(contentUidList[position])
                ?.collection("friends")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->


                friendDTO.userId = contentDTOs[position].userId
                friendDTO.uid = contentUidList[position]
                friendDTO.friend = boolean
                friendDTO.timestamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("userInfo").document(uid!!)
                    .collection("friends").document(contentUidList[position]).set(friendDTO)
                /*
                if (friendDTO.friend == false) {
                    view.friend_imageview.setImageResource(R.drawable.ic_remove)
                }
                if (friendDTO.friend == true) {
                    view.friend_imageview.setImageResource(R.drawable.ic_add)
                }

                 */
            }
        }
    }
}
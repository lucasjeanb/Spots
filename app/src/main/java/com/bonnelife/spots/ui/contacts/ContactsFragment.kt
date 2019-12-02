package com.bonnelife.spots.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bonnelife.spots.R
import com.bonnelife.spots.database.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.contact_item_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*

class ContactsFragment : Fragment() {

    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =
            LayoutInflater.from(requireActivity()).inflate(R.layout.fragment_contacts, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.contact_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.contact_recyclerview.layoutManager = LinearLayoutManager(requireActivity())
        return view
    }

    inner class DetailViewRecyclerViewAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO.Friend> = arrayListOf()
        var friendDTOs: ArrayList<ContentDTO.Friend> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {

            firestore?.collection("userInfo")
                ?.document(uid!!)?.collection("friends")
                ?.whereEqualTo("friend",false)
                ?.orderBy("userId")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUidList.clear()
                    //Sometimes, This code return null of querySnapshot when it signout
                    if(querySnapshot == null) return@addSnapshotListener

                    for(snapshot in querySnapshot!!.documents){
                        var item = snapshot.toObject(ContentDTO.Friend::class.java)
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
            var contentDTOuid = contentDTOs[p1].uid
            var contentDTOuser = contentDTOs[p1].userId
            var contentDTOfriend = contentDTOs[p1].friend
            //UserId
            viewholder.contactName_textview.text = contentDTOs[p1].userId

            //Image
            /*
            Glide.with(requireContext()).load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().circleCrop()).into(viewholder.contact_imageview)
             */

            //Explain of content
            viewholder.coord_textview.text = contentDTOs[p1].timestamp.toString()

            viewholder.add_imageview.setOnClickListener {
                    var friendDTO = ContentDTO.Friend()
                    friendDTO.userId = contentDTOuser
                    friendDTO.uid = contentDTOuid
                    friendDTO.friend = true
                    friendDTO.timestamp = System.currentTimeMillis()
                    FirebaseFirestore.getInstance().collection("userInfo").document(uid!!)
                        .collection("friends").document(contentDTOuid!!).set(friendDTO)
                        ?.addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "${contentDTOuser} have been added",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    viewholder.add_imageview?.setImageResource(R.drawable.ic_check)
            }
        }

        fun friendEvent(position: Int, view: View, boolean: Boolean) {
            var friendDTO = ContentDTO.Friend()

            firestore?.runTransaction { transaction ->


                friendDTO.userId = contentDTOs[position].userId
                friendDTO.uid = contentDTOs[position].uid
                friendDTO.friend = boolean
                friendDTO.timestamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("userInfo").document(uid!!)
                    .collection("friends").document(contentDTOs[position].uid.toString()).set(friendDTO)

            }
        }
    }
}
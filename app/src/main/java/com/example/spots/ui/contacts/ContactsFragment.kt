package com.example.spots.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.spots.R
import com.example.spots.database.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.contact_item_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ContactsFragment : Fragment() {

    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_contacts,container,false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.contact_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.contact_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var friendDTOs : ArrayList<ContentDTO.Friend> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init {


            firestore?.collection("userInfo")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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
            firestore?.collection("userInfo")
                ?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentUidList.clear()
                friendDTOs.clear()
                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO.Friend::class.java)
                    friendDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.contact_item_view_layout,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return friendDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.contactName_textview.text = contentDTOs[p1].userId

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().circleCrop()).into(viewholder.contact_imageview)

            //Explain of content
            viewholder.coord_textview.text = contentDTOs[p1].timestamp.toString()

            viewholder.friend_imageview.setOnClickListener {
                favoriteEvent(p1)
            }

            if(friendDTOs[p1].friend == true){
                viewholder.friend_imageview.setImageResource(R.drawable.ic_remove)

            }else{
                viewholder.friend_imageview.setImageResource(R.drawable.ic_add)
            }
        }
        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("userInfo")?.document(contentUidList[position])
                ?.collection("friends")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                var friendDTO = ContentDTO.Friend()

                if(friendDTO.friend == true){
                    friendDTO.friend = false
                    Log.d("LOG_X","if == true: ${friendDTO.friend}")

                }
                if(friendDTO.friend == false) {
                    friendDTO.friend = true
                    Log.d("LOG_X", "if == false: ${friendDTO.friend}")
                }
                else{
                    friendDTO.userId = contentDTOs[position].userId
                    friendDTO.uid = contentUidList[position]
                    friendDTO.friend = true
                    friendDTO.timestamp = System.currentTimeMillis()
                    Log.d("LOG_X","else: ${friendDTO.friend}")
                }
                FirebaseFirestore.getInstance().collection("userInfo").document(uid!!)
                    .collection("friends").document(contentUidList[position]).set(friendDTO)

                /*
                if(contentDTO!!.favorites.containsKey(uid)){
                    contentDTO?.favorites.remove(uid)
                }else{
                    contentDTO?.favorites[uid!!] = true
                }
                transaction.set(tsDoc,contentDTO)

                 */
            }
        }
    }
}
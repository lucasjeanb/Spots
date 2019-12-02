package com.bonnelife.spots.ui.map


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bonnelife.spots.R
import com.bonnelife.spots.database.model.ContentDTO
import com.bonnelife.spots.util.logout
import com.bonnelife.spots.util.toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.progressbar
import kotlinx.android.synthetic.main.fragment_profile.text_email
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    var fragmentView : View? = null
    var PICK_IMAGE_FROM_ALBUM = 0
    lateinit var storage : FirebaseStorage
    var originalPhotoUri: Uri? = null
    var compressedPhotoUri : Uri? = null
    lateinit var bitmapPhoto: Bitmap
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var uid: String? = null
    var imageUrlStart : String? = null
    var PICK_PROFILE_FROM_ALBUM = 10
    var contentDTO = ContentDTO()
    lateinit var imageUri: Uri

    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"

    private val REQUEST_IMAGE_CAPTURE = 100

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_profile, container, false)
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        uid = FirebaseAuth.getInstance().currentUser?.uid




        // Inflate the layout for this fragment
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment_background_click.setOnClickListener{
            fragmentManager?.popBackStack()
        }

        currentUser?.let { user ->
            /*
            Glide.with(this)
                .load(user.photoUrl)
                .into(image_view)

             */
            edit_text_name.setText(user.displayName)
            text_email.text = user.email

            if (user.isEmailVerified) {
                text_not_verified.visibility = View.INVISIBLE
            } else {
                text_not_verified.visibility = View.VISIBLE
            }
        }

        signout_button.setOnClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Are you sure?")
                setPositiveButton("Yes") { _, _ ->

                    FirebaseAuth.getInstance().signOut()
                   requireContext().logout()
                }
                setNegativeButton("Cancel") { _, _ ->
                }
            }.create().show()
        }



        upload_image_view.setOnClickListener {
            //takePictureIntent()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }

        button_save.setOnClickListener {

            progressbar.visibility = View.VISIBLE

            //changePicture()
            context?.toast("Profile Updated")

            progressbar.visibility = View.INVISIBLE

        }


        text_not_verified.setOnClickListener {

            currentUser?.sendEmailVerification()
                ?.addOnCompleteListener {
                    if(it.isSuccessful){
                        context?.toast("Verification Email Sent")
                    }else{
                        context?.toast(it.exception?.message!!)
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            progressbar.visibility = View.VISIBLE
            imageUri = data?.data!!
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                progressbar.visibility = View.GONE
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                contentDTO.imageUrl = uri.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.userId = auth?.currentUser?.email
                contentDTO.timestamp = System.currentTimeMillis()
                firestore?.collection("userInfo")?.document(uid!!)?.set(contentDTO)
                FirebaseFirestore.getInstance().collection("profileImages").document(uid!!).set(map)
            }
        }
    }
}

package com.example.spots.ui.map


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.spots.R
import com.example.spots.database.model.ContentDTO
import com.example.spots.util.logout
import com.example.spots.util.toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_profile.button_save
import kotlinx.android.synthetic.main.fragment_profile.edit_text_name
import kotlinx.android.synthetic.main.fragment_profile.image_view
import kotlinx.android.synthetic.main.fragment_profile.progressbar
import kotlinx.android.synthetic.main.fragment_profile.progressbar_pic
import kotlinx.android.synthetic.main.fragment_profile.signout_button
import kotlinx.android.synthetic.main.fragment_profile.text_email
import kotlinx.android.synthetic.main.fragment_profile.text_not_verified
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    var PICK_IMAGE_FROM_ALBUM = 0
    lateinit var storage : FirebaseStorage
    var originalPhotoUri: Uri? = null
    var compressedPhotoUri : Uri? = null
    lateinit var bitmapPhoto: Bitmap
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null


    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"

    private val REQUEST_IMAGE_CAPTURE = 100

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .into(image_view)
            edit_text_name.setText(user.displayName)
            text_email.text = user.email

            if (user.isEmailVerified) {
                text_not_verified.visibility = View.INVISIBLE
            } else {
                text_not_verified.visibility = View.VISIBLE
            }
        }
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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



        image_view.setOnClickListener {
            //takePictureIntent()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }

        button_save.setOnClickListener {
            contentUpload()

            var photo = when {
                compressedPhotoUri != null -> compressedPhotoUri
                currentUser?.photoUrl == null -> Uri.parse(DEFAULT_IMAGE_URL)
                else -> currentUser.photoUrl
            }

            val name = edit_text_name.text.toString().trim()

            if (name.isEmpty()) {
                edit_text_name.error = "name required"
                edit_text_name.requestFocus()
                return@setOnClickListener
            }

            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photo)
                .build()

            progressbar.visibility = View.VISIBLE
/*
            currentUser?.updateProfile(updates)
                ?.addOnCompleteListener { task ->
                    progressbar.visibility = View.INVISIBLE
                    if (task.isSuccessful) {
                        context?.toast("Profile Updated")
                    } else {
                        context?.toast(task.exception?.message!!)
                    }
                }

 */
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

    fun contentUpload() {
        //Make filename

        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("pics")?.child(imageFileName)

        //Promise method

            var contentDTO = ContentDTO()

            //Insert downloadUrl of image
            contentDTO.imageUrl = compressedPhotoUri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = edit_text_name.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            fragmentManager?.popBackStack()

    }

        private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pictureIntent ->
            pictureIntent.resolveActivity(activity?.packageManager!!)?.also {
                startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //This is path to the selected image
                originalPhotoUri = data?.data
                bitmapPhoto = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver, originalPhotoUri)
                uploadImageAndSaveUri(bitmapPhoto)
                image_view.setImageURI(compressedPhotoUri)


            }else{
                //Exit the addPhotoActivity if you leave the album without selecting it
                requireActivity().finish()

            }
        }
    }

    private fun uploadImageAndSaveUri(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val storageRef =
        // = FirebaseStorage.getInstance()
            storage.reference
            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        progressbar_pic.visibility = View.VISIBLE
        upload.addOnCompleteListener { uploadTask ->
            progressbar_pic.visibility = View.INVISIBLE

            if (uploadTask.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        compressedPhotoUri = it
                        activity?.toast(compressedPhotoUri.toString())
                        image_view.setImageBitmap(bitmap)
                    }
                }
            } else {
                uploadTask.exception?.let {
                    activity?.toast(it.message!!)
                }
            }
        }

    }

}

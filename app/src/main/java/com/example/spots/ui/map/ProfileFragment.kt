package com.example.spots.ui.map


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.spots.R
import com.example.spots.util.logout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editprofile_button.setOnClickListener {
            var editProfileFragment = EditProfileFragment()
            fragmentManager?.beginTransaction()
                ?.add(R.id.editProfile_framelayout, editProfileFragment)
                ?.addToBackStack(null)
                ?.commit()
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
    }



}

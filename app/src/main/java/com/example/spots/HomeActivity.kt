package com.example.spots

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.NonNull
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.spots.util.makeStatusBarTransparent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponent()
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_myspots, R.id.navigation_contacts
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

         */
        navView.setupWithNavController(navController)

        makeStatusBarTransparent()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { _, insets ->
            insets.consumeSystemWindowInsets()
        }
    }

    private fun initComponent() {
        // get the bottom sheet view
        val llBottomSheet = findViewById(R.id.bottom_sheet) as LinearLayout
        // init the bottom sheet behavior
       var  bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet)
        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(object:BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(p0: View, p1: Int) {
            }
        })

    }



}

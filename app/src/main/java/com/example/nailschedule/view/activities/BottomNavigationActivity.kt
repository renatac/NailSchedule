package com.example.nailschedule.view.activities

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityBottonNavigationBinding
import com.example.nailschedule.view.activities.LoginActivity.Companion.EXTRA_DISPLAY_NAME
import com.example.nailschedule.view.activities.LoginActivity.Companion.EXTRA_PHOTO_URL

class BottomNavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottonNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottonNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getExtras()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_botton_navigation)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getExtras() {
        val displayName = intent.getBundleExtra(LoginActivity.EXTRA_USER_DATA)
            ?.getString(EXTRA_DISPLAY_NAME)
        val photoUrl = Uri.parse(intent.getBundleExtra(LoginActivity.EXTRA_USER_DATA)
            ?.getString(EXTRA_PHOTO_URL))
    }
}
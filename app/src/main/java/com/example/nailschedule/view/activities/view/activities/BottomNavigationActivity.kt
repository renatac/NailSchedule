package com.example.nailschedule.view.activities.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityBottomNavigationBinding
import com.example.nailschedule.view.activities.utils.SharedPreferencesHelper
import com.example.nailschedule.view.activities.utils.isLoggedInFacebook
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class BottomNavigationActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var activityBottomNavigationBinding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var photoUrl: Uri? = null
    private var displayName: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBottomNavigationBinding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(activityBottomNavigationBinding.root)

        val bottomNavigationView: BottomNavigationView = activityBottomNavigationBinding.bottomNavView

        val navigationView: NavigationView = activityBottomNavigationBinding.navView
        navigationView.setNavigationItemSelectedListener(this)

        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_gallery, R.id.navigation_scheduling, R.id.navigation_scheduled
            ),
            activityBottomNavigationBinding.drawerLayout
        )

        val toggle = ActionBarDrawerToggle(
            this,
            activityBottomNavigationBinding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        activityBottomNavigationBinding.drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        getExtras()

        //Set the appBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        //Enable BottomNavigationView Click
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        setupNavHeaderElements()
    }

    override fun onBackPressed() {
        Log.i("test", "onBackPressed: ")
        activityBottomNavigationBinding.apply {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.i("test", "onSupportNavigateUp: ")
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun getExtras() {
        displayName = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_DISPLAY_NAME, "")
        photoUrl = Uri.parse(
            SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_PHOTO_URL, ""))
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, "")
    }

    private fun setupNavHeaderElements() {
            val headerView = activityBottomNavigationBinding.navView.getHeaderView(0)
            val navHeaderTvName = headerView.findViewById(R.id.nav_header_tv_name) as TextView
            displayName?.let { navHeaderTvName.text = displayName }
            val navHeaderIv = headerView.findViewById(R.id.nav_header_iv) as ImageView
            //Setting the image to imageView using Glide Library
            photoUrl?.let {
                Glide.with(this@BottomNavigationActivity).load(photoUrl).into(navHeaderIv)
            }
            val navHeaderTvEmail = headerView.findViewById(R.id.nav_header_tv_email) as TextView
            email?.let { navHeaderTvEmail.text = email }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_off -> {
                signOut()
            }
            else -> {
                finishAffinity()
            }
        }
        activityBottomNavigationBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        //It separates if it's Facebook or Google
        if(isLoggedInFacebook()) {
            LoginManager.getInstance().logOut()
            redirectToLoginActivity()
        } else {
            LoginActivity.googleSignInClientGetInstance(this).signOut()
                .addOnCompleteListener(this, OnCompleteListener<Void?> {
                    redirectToLoginActivity()
                })
        }
    }

    private fun redirectToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
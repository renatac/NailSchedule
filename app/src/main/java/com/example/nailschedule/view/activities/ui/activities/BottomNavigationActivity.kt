package com.example.nailschedule.view.activities.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityBottomNavigationBinding
import com.example.nailschedule.databinding.NavHeaderBinding
import com.example.nailschedule.view.activities.utils.SharedPreferenceHelper
import com.example.nailschedule.view.activities.utils.isLoggedInFacebook
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class BottomNavigationActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var activityBottomNavigationBinding: ActivityBottomNavigationBinding
    private lateinit var navHeaderBinding: NavHeaderBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var photoUrl: Uri? = null
    private var displayName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityBottomNavigationBinding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)
        setContentView(activityBottomNavigationBinding.root)

        val bottomNavigationView: BottomNavigationView = activityBottomNavigationBinding.bottomNavView

        val navigationView: NavigationView = activityBottomNavigationBinding.navView
        navigationView.setNavigationItemSelectedListener(this)

        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
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
        setupNavHeaderElements()

        //Set the appBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        //NavigationUI.setupWithNavController(binding.navView, navController)

        //Enable BottomNavigationView Click
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
        //setupActionBarWithNavController(navController, appBarConfiguration)
        //bottomNavigationView.setupWithNavController(navController)
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
        displayName = SharedPreferenceHelper.read(
            SharedPreferenceHelper.EXTRA_DISPLAY_NAME, "")
        photoUrl = Uri.parse(
            SharedPreferenceHelper.read(
            SharedPreferenceHelper.EXTRA_PHOTO_URL, ""))
    }

    private fun setupNavHeaderElements() {
        navHeaderBinding.apply {
            navHeaderTextView.text = displayName
            navHeaderImageView.setImageURI(photoUrl)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_one -> {
                Toast.makeText(this, "Item 1", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_item_two -> {
                Toast.makeText(this, "Item 2", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_item_three -> {
                Toast.makeText(this, "Item 3", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_item_four -> {
                Toast.makeText(this, "Item 4", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_item_five-> {
                Toast.makeText(this, "Item 5", Toast.LENGTH_SHORT).show()
            }
            R.id.log_off -> {
                signOut()
            }
            R.id.exit -> {
                finishAffinity()
            }
            else -> {
                Toast.makeText(this, "Menu Default", Toast.LENGTH_SHORT).show()
            }
        }
        activityBottomNavigationBinding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        if(isLoggedInFacebook()) {
            //Separar se é google ou facebook
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
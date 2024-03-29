package com.example.nailschedule.view.activities.view.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityBottomNavigationBinding
import com.example.nailschedule.view.activities.utils.*
import com.example.nailschedule.view.activities.viewmodels.ConnectivityViewModel
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class BottomNavigationActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var connectivityViewModel: ConnectivityViewModel

    private var photoUrl: Uri? = null
    private var displayName: String? = null
    private var email: String? = null

    companion object {
        const val LOG_OUT = "log_out"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavigationView: BottomNavigationView = binding.bottomNavView
        connectivityViewModel =
            ViewModelProvider(this).get(ConnectivityViewModel::class.java)

        setupObserver()

        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_gallery, R.id.navigation_scheduling, R.id.navigation_scheduled
            ),
            binding.drawerLayout
        )

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        getExtras()

        //Set the appBarConfiguration
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        //Enable BottomNavigationView Click
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        setupNavHeaderElements()
    }

    private fun showNoIntern() {
        showToast(this@BottomNavigationActivity, R.string.no_internet)
    }

    private fun setupObserver() {
        connectivityViewModel.hasInternet.observe(this,
            {
                if (it.first) {
                    if (it.second == LOG_OUT) {
                        //It separates if it's Facebook or Google
                        if (isLoggedInFacebook()) {
                            LoginManager.getInstance().logOut()
                            Firebase.auth.signOut()
                            showLoginScreen(this)
                        } else {
                            LoginActivity.googleSignInClientGetInstance(this).signOut()
                                .addOnCompleteListener(this, OnCompleteListener<Void?> {
                                    Firebase.auth.signOut()
                                    showLoginScreen(this)
                                })
                        }
                    }
                } else {
                    showNoIntern()
                }
            })
    }

    override fun onBackPressed() {
        Log.i("test", "onBackPressed: ")
        binding.apply {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                val isGalleryFragment =
                    navController.currentDestination?.label.toString() == getString(R.string.title_gallery)
                if (isGalleryFragment) {
                    finishAffinity()
                } else {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.i("test", "onSupportNavigateUp: ")
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    private fun getExtras() {
        displayName = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_DISPLAY_NAME, String.empty()
        )
        photoUrl = Uri.parse(
            SharedPreferencesHelper.read(
                SharedPreferencesHelper.EXTRA_PHOTO_URL, String.empty()
            )
        )
        email = SharedPreferencesHelper.read(
            SharedPreferencesHelper.EXTRA_EMAIL, String.empty()
        )
    }

    private fun setupNavHeaderElements() {
        val headerView = binding.navView.getHeaderView(0)
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
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun signOut() {
        connectivityViewModel.checkForInternet(
            this@BottomNavigationActivity, LOG_OUT)
    }
}
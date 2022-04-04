package com.example.nailschedule.view.activities.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nailschedule.R
import com.example.nailschedule.databinding.ActivityPhotoBinding
import com.example.nailschedule.view.activities.view.scheduled.ScheduledFragment


class PhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupPhoto()
    }

    private fun setupToolbar() = binding.apply {
        toolbar.apply {
            title = getString(R.string.photo)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupPhoto() {
        val uriString = intent.getStringExtra(ScheduledFragment.EXTRA_URI_STRING)
        //Setting the image to imageView using Glide Library
        Glide.with(this).load(uriString).into(binding.iv)
    }
}
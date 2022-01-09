package com.example.nailschedule.view.activities.view.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            showLoginScreen()}, 4000)

        binding.llSplash.animate().apply {
            duration = 1500
            translationYBy(360f)
            alpha(0.5f)
        }.withEndAction {
            binding.llSplash.animate().apply {
                duration = 1500
                alpha(1f)
                translationYBy(360f)
            }
        }
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
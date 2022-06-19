package com.example.nailschedule.view.activities.view.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.databinding.ActivitySplashBinding
import com.example.nailschedule.view.activities.utils.showLoginScreen

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createAnimation()
    }

    private fun createAnimation() {
        Handler(Looper.getMainLooper()).postDelayed({
            showLoginScreen(this)}, 4000)

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
}
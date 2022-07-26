package com.example.nailschedule.view.activities.view.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nailschedule.databinding.ActivitySplashBinding
import com.example.nailschedule.view.activities.utils.showLoginScreen

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    companion object {
        const val FOUR_THOUSAND = 4000L
        const val THOUSAND_FIFTEEN = 1500L
        const val THREE_HUNDRED_AND_SIXTY = 360f
        const val ONE = 1f
        const val HALF = 0.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createAnimation()
    }

    private fun createAnimation() {
        Handler(Looper.getMainLooper()).postDelayed({
            showLoginScreen(this)}, FOUR_THOUSAND)

        binding.llSplash.animate().apply {
            duration = THOUSAND_FIFTEEN
            translationYBy(THREE_HUNDRED_AND_SIXTY)
            alpha(HALF)
        }.withEndAction {
            binding.llSplash.animate().apply {
                duration = THOUSAND_FIFTEEN
                alpha(ONE)
                translationYBy(THREE_HUNDRED_AND_SIXTY)
            }
        }
    }
}
package com.example.task

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.animation.AnimatorListenerAdapter
import android.view.Window

class SplashActivity : AppCompatActivity() {

    private val splashDuration: Long = 3000 // Total duration of splash screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashLogo = findViewById<ImageView>(R.id.splashLogo)

        // Initially set visibility to visible and scale down the logo
        splashLogo.apply {
            visibility = ImageView.VISIBLE
            scaleX = 0.5f
            scaleY = 0.5f
            alpha = 0f
        }

        // Create an alpha animation (fade-in)
        val fadeIn = ObjectAnimator.ofFloat(splashLogo, "alpha", 0f, 1f).apply {
            duration = 1500 // Duration of the fade-in animation
        }

        // Create a scale animation (scale-up)
        val scaleX = ObjectAnimator.ofFloat(splashLogo, "scaleX", 0.5f, 1f).apply {
            duration = 1500 // Duration of the scale animation
        }
        val scaleY = ObjectAnimator.ofFloat(splashLogo, "scaleY", 0.5f, 1f).apply {
            duration = 1500 // Duration of the scale animation
        }

        // Set interpolator for smooth animation
        val interpolator = AccelerateDecelerateInterpolator()
        fadeIn.interpolator = interpolator
        scaleX.interpolator = interpolator
        scaleY.interpolator = interpolator

        // Combine animations into an AnimatorSet
        val animatorSet = AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    // Start the main activity immediately after the animation ends
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
            start()
        }
        changeStatusBarColor()
    }
    private fun changeStatusBarColor() {
        // Get the window of the current activity
        val window: Window = window
        // Set the status bar color
        window.statusBarColor = getColor(R.color.colorSecondary)
    }
}

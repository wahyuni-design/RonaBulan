package com.ronabulan.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION = 2800L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar for splash
        supportActionBar?.hide()

        val logoContainer = findViewById<LinearLayout>(R.id.logo_container)
        val tvAppName = findViewById<TextView>(R.id.tv_app_name)
        val tvTagline = findViewById<TextView>(R.id.tv_tagline)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        // Animate logo: scale + fade in
        val scaleAnim = ScaleAnimation(
            0.4f, 1.0f,
            0.4f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply { duration = 700; fillAfter = true }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 700
            fillAfter = true
        }

        val logoAnimSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(fadeIn)
        }
        logoContainer.startAnimation(logoAnimSet)

        // Slide up for tagline (delayed)
        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp = TranslateAnimation(0f, 0f, 30f, 0f).apply {
                duration = 500
                fillAfter = true
            }
            val taglineFade = AlphaAnimation(0f, 1f).apply {
                duration = 500
                fillAfter = true
            }
            tvTagline.startAnimation(AnimationSet(true).also {
                it.addAnimation(slideUp)
                it.addAnimation(taglineFade)
            })
        }, 600)

        // Navigate to HomeActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_DURATION)
    }
}


package com.projectorganizer.projectorganizer.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import com.projectorganizer.projectorganizer.R

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //nascondo la status bar
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        //modifico il font dentro la splashscreen
        val tf:Typeface = Typeface.createFromAsset(assets,"Bulletto Killa.ttf")
        findViewById<TextView>(R.id.tv_app_name).typeface=tf


        //eseguo la splashscreen per 2 sec
        Handler(Looper.getMainLooper()).postDelayed(
            {
                startActivity(
                    Intent(this, IntroActivity::class.java)
                )
                finish()
            },2000)
    }
}
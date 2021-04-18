package com.todoapp.todoapp.activities

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
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass

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
                var currentUserID= FirestoreClass().getCurrentUserId()
                if(currentUserID.isNotEmpty()) // se nel dispositivo c'e' gia' un account dell'utente, fa il login auto
                    startActivity(Intent(this, MainActivity::class.java))
                else //altrimenti sign in o signup
                    startActivity(Intent(this, IntroActivity::class.java))
                finish() //Call this when your activity is done and should be closed.
            },2000)
    }
}
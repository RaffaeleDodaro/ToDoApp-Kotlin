package com.projectorganizer.projectorganizer.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.accountHandler.LoginActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.SignUpActivity

class IntroActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        auth = FirebaseAuth.getInstance()
        val user=auth.currentUser


        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        if(user !=null) {
            val dashboardIntent = Intent(this, MainActivity::class.java)
            startActivity(dashboardIntent)
        }
        else {
            findViewById<Button>(R.id.btn_sign_up_intro).setOnClickListener {
                startActivity(
                    // permette di attivare ogni componente di una applicazione,
                    // quindi anche una Activity, ma in generale di trasmettere
                    // delle informazioni tra componenti e di farli comunicare l'uno con l'altro
                    // o con altre applicazioni.
                    Intent(this, SignUpActivity::class.java)
                )
            }


            findViewById<Button>(R.id.btn_sign_in_intro).setOnClickListener {
                startActivity(
                    // permette di attivare ogni componente di una applicazione,
                    // quindi anche una Activity, ma in generale di trasmettere
                    // delle informazioni tra componenti e di farli comunicare l'uno con l'altro
                    // o con altre applicazioni.
                    Intent(this, LoginActivity::class.java)
                )
            }
        }
    }



}
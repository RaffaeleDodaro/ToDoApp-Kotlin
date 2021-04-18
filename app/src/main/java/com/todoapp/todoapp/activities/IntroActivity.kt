package com.todoapp.todoapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.accountHandler.LoginActivity
import com.todoapp.todoapp.activities.accountHandler.SignUpActivity

class IntroActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth // The entry point of the Firebase Authentication SDK.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)


        auth = FirebaseAuth.getInstance()
        val user=auth.currentUser // verifico se l'utente in precedenza aveva gia' fatto l'accesso
        val currentUser = Firebase.auth.currentUser

        //nascondo la statusbar
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        if(user !=null || currentUser!=null) {

            val dashboardIntent = Intent(this, MainActivity::class.java)  // permette di attivare ogni componente di una applicazione,
                                                                                        // quindi anche una Activity, ma in generale di trasmettere
                                                                                        // delle informazioni tra componenti e di farli comunicare l'uno con l'altro
                                                                                        // o con altre applicazioni.
            startActivity(dashboardIntent)
        }
        else {
            findViewById<Button>(R.id.btn_sign_up_intro).setOnClickListener {
                startActivity(
                    Intent(this, SignUpActivity::class.java)  // permette di attivare ogni componente di una applicazione,
                                                                            // quindi anche una Activity, ma in generale di trasmettere
                                                                            // delle informazioni tra componenti e di farli comunicare l'uno con l'altro
                                                                            // o con altre applicazioni.
                )
            }


            findViewById<Button>(R.id.btn_sign_in_intro).setOnClickListener {
                startActivity(
                    Intent(this, LoginActivity::class.java)   // permette di attivare ogni componente di una applicazione,
                                                                            // quindi anche una Activity, ma in generale di trasmettere
                                                                            // delle informazioni tra componenti e di farli comunicare l'uno con l'altro
                                                                            // o con altre applicazioni.
                )
            }
        }
    }



}
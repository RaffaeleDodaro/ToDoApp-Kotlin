package com.projectorganizer.projectorganizer.activities.accountHandler

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.BaseActivity
import com.projectorganizer.projectorganizer.activities.MainActivity

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

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

        setupActionBar()


        //leggo i valori per login
        findViewById<Button>(R.id.btn_sign_in).setOnClickListener {
            val email:String=findViewById<TextView>(R.id.et_emailSignIn).text.toString()
            val password:String=findViewById<TextView>(R.id.et_passwordSignIn).text.toString()
            if (validateForm(email,password))
            {
                showProgressDialog(resources.getString(R.string.please_wait))
                login(email,password)
            }

        }

    }

    private fun validateForm(email:String,password:String):Boolean
    {
        //when equivale allo switch
        return when{
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Inserisci un indirizzo email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Inserisci una password")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun login(email:String,password:String)
    {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Sign in", "signInWithEmail:success")
                        val user = auth.currentUser
                        startActivity(Intent(this,MainActivity::class.java))
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Sign in", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Utente non trovato o email/password non corrette",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }


    //aggiunge una freccia alla schermata sign in che permette di tornare indietro
    private fun setupActionBar() {
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_sign_in_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        findViewById<Toolbar>(R.id.toolbar_sign_in_activity).setNavigationOnClickListener { onBackPressed() }
    }
}
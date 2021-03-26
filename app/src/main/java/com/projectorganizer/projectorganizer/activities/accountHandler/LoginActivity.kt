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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.BaseActivity
import com.projectorganizer.projectorganizer.activities.MainActivity
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.User

class LoginActivity : BaseActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
                showErrorSnackBar("Inserisci un indirizzo email") // disegna un rettangolo arancione in fondo lo schermo
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Inserisci una password")// disegna un rettangolo arancione in fondo lo schermo
                false
            }
            else -> {
                true
            }
        }
    }

    private fun login(email:String,password:String)
    {
        /*auth.signInWithEmailAndPassword(email, password) //signInWithEmailAndPassword e' un metodo di firebase auth
                .addOnCompleteListener(this) { task -> //addOnCompleteListener serve per gestire il successo e fallimento del listener
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        // Login effettuato con successo
                        FirestoreClass().signInUser(this)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Utente non trovato o email/password non corrette",
                                Toast.LENGTH_SHORT).show()
                    }
                }*/
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Calling the FirestoreClass signInUser function to get the data of user from database.
                    FirestoreClass().loadUserData(this)
                } else {
                    Toast.makeText(
                        this,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
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

    fun signInSuccess(user: User)
    {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
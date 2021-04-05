package com.projectorganizer.projectorganizer.activities.accountHandler

import android.R.id.message
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.BaseActivity
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.User


class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //nascondo la statusbar
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                    //Window flag: hide all screen decorations (such as the status bar) while this window is displayed.
                    // This allows the window to use the entire display space for itself
            )
        }

        setupActionBar()
    }

    //aggiunge una freccia alla schermata sign up che permette di tornare indietro
    private fun setupActionBar()
    {
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_sign_in_activity)) //Set a Toolbar to act as the ActionBar for this Activity window
        val actionBar=supportActionBar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        findViewById<Toolbar>(R.id.toolbar_sign_in_activity).setNavigationOnClickListener{onBackPressed()}
        findViewById<Button>(R.id.btn_sign_up).setOnClickListener {
            registerUser()
        }
    }

    fun userRegisteredSuccess()
    {
        Toast.makeText(this, "Ti sei registrato correttamente", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        //sendEmail(this)
        FirebaseAuth.getInstance().signOut() //faccio il signout e obbligo l'utente ad accedere al nuovo account
        finish()
    }

    /*fun sendEmail(context: Context) {

    }
*/
    private fun validateForm(name: String, email: String, password: String):Boolean
    {
        //when equivale allo switch
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Inserisci un nome")
                false
            }
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
    private var emailProva:String=""
    private fun registerUser()
    {
        val name:String = findViewById<TextView>(R.id.et_name).text.toString().trim() //trim ritorna una stringa senza spazi
        val email:String = findViewById<TextView>(R.id.et_email).text.toString().trim()
        val password:String = findViewById<TextView>(R.id.et_password).text.toString()
        if(validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task -> //addOnCompleteListener serve per gestire il successo e fallimento del listener
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!! // !! lancia un'eccezione se result o user sono nulli
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registeredEmail) // uid Returns a string used to uniquely identify your user in your Firebase project's
                    emailProva=email
                    FirestoreClass().registerUser(this, user)
                } else {
                    Toast.makeText(this, "Registrazione non riuscita", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
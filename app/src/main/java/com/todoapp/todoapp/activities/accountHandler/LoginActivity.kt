package com.todoapp.todoapp.activities.accountHandler

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.BaseActivity
import com.todoapp.todoapp.activities.MainActivity
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.User


class LoginActivity : BaseActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "GoogleActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setupActionBar()

        val signInButton = findViewById<SignInButton>(R.id.btn_sign_inGoogle)
        signInButton.setSize(SignInButton.SIZE_STANDARD)

        //leggo i valori per login
        findViewById<Button>(R.id.btn_sign_in).setOnClickListener {
            val email: String = findViewById<TextView>(R.id.et_emailSignIn).text.toString()
            val password: String = findViewById<TextView>(R.id.et_passwordSignIn).text.toString()
            if (validateForm(email, password)) {
                showProgressDialog(resources.getString(R.string.please_wait))
                login(email, password)
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<SignInButton>(R.id.btn_sign_inGoogle).setOnClickListener {
            signInWithGoogle()
        }
        auth = Firebase.auth

        findViewById<Button>(R.id.btn_resetpassword).setOnClickListener {
            val email: String = findViewById<TextView>(R.id.et_emailSignIn).text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                            Toast.makeText(
                                this@LoginActivity,
                                "Dai un'occhiata alla tua email per completare il reset",
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            Toast.makeText(
                                this@LoginActivity,
                                "Account non trovato!",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
            } else
                showErrorSnackBar("Inserisci una email!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
        //RC_SIGN_IN = 9001
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            hideProgressDialog()
            startActivity(Intent(this, MainActivity::class.java))
            finish() // finisce l'attivita'
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    showProgressDialog(getString(R.string.please_wait))
                    FirestoreClass().loadUserData(this)
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun validateForm(email: String, password: String): Boolean {
        //when equivale allo switch
        return when {
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Inserisci un indirizzo email") // disegna un rettangolo arancione in fondo lo schermo
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Inserisci una password")// disegna un rettangolo arancione in fondo lo schermo
                false
            }
            else -> {
                true
            }
        }
    }

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user!!.isEmailVerified)// !! vuol dire che user NON deve essere nullo
                        FirestoreClass().loadUserData(this)
                    else {
                        Toast.makeText(
                            this,
                            "Non hai verificato l'account. Controlla l'email",
                            Toast.LENGTH_LONG
                        ).show()
                        hideProgressDialog()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Email/password non corretti",
                        Toast.LENGTH_LONG
                    ).show()
                    hideProgressDialog()
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

    fun signInSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish() // finisce l'attivita'
    }
}
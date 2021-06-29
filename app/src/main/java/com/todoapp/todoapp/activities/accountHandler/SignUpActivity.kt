package com.todoapp.todoapp.activities.accountHandler

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

class SignUpActivity : BaseActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val RC_SIGN_IN = 9001
    private val TAG = "GoogleActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val signInButton = findViewById<SignInButton>(R.id.btn_sign_upGoogle)
        signInButton.setSize(SignInButton.SIZE_STANDARD)

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        setupActionBar()

        findViewById<SignInButton>(R.id.btn_sign_upGoogle).setOnClickListener {
            signUpWithGoogle()
        }

        findViewById<Button>(R.id.btn_sign_up).setOnClickListener {
            registerUser()
        }

        auth = Firebase.auth
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                //if(!userExist(account.email!!)){
                Log.d(TAG, "firebaseRegisterWithGoogle: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
                //}
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign up failed", e)
            }
        }
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
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signUpWithCredential:success")
                    val user = auth.currentUser
                    showProgressDialog(getString(R.string.please_wait))

                    val firebaseUser: FirebaseUser =
                        task.result!!.user!! // !! lancia un'eccezione se result o user sono nulli
                    val user2 = User(
                        firebaseUser.uid,
                        user!!.displayName!!,
                        user.email!!
                    ) // uid Returns a string used to uniquely identify your user in your Firebase project's

                    FirestoreClass().registerUser(this, user2, true)
                    updateUI(user)
                } else {
                    Log.w(TAG, "signUpWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signUpWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }

    //aggiunge una freccia alla schermata sign up che permette di tornare indietro
    private fun setupActionBar() {
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_sign_in_activity)) //Set a Toolbar to act as the ActionBar for this Activity window
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        findViewById<Toolbar>(R.id.toolbar_sign_in_activity).setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        //when equivale allo switch
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Inserisci un nome")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Inserisci un indirizzo email")
                false
            }
            password.length < 6 -> {
                showErrorSnackBar("Inserisci una password di almeno 6 caratteri")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Inserisci una password")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun registerUser() {
        val name: String = findViewById<TextView>(R.id.et_name).text.toString()
            .trim() //trim ritorna una stringa senza spazi
        val email: String = findViewById<TextView>(R.id.et_email).text.toString().trim()
        val password: String = findViewById<TextView>(R.id.et_password).text.toString()

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task -> //addOnCompleteListener serve per gestire il successo e fallimento del listener
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser =
                            task.result!!.user!! // !! lancia un'eccezione se result o user sono nulli
                        val registeredEmail = firebaseUser.email!!
                        val user = User(
                            firebaseUser.uid,
                            name,
                            registeredEmail
                        ) // uid Returns a string used to uniquely identify your user in your Firebase project's

                        FirestoreClass().registerUser(this, user,false)
                    } else {
                        Toast.makeText(this, "Utente gia' presente", Toast.LENGTH_SHORT).show()
                        hideProgressDialog()
                    }
                }
        }
    }

    fun userRegisteredSuccess(google:Boolean) {
        if(!google)
            Toast.makeText(
                this,
                "Ti sei registrato correttamente. Controlla l'email!",
                Toast.LENGTH_LONG
            ).show()
        else
            Toast.makeText(
                this,
                "Ti sei registrato correttamente.",
                Toast.LENGTH_LONG
            ).show()

        hideProgressDialog()
        finish()
    }
}
package com.projectorganizer.projectorganizer.activities.accountHandler

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.identity.SignInClient
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
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.BaseActivity
import com.projectorganizer.projectorganizer.activities.MainActivity
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.User


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
            val email:String=findViewById<TextView>(R.id.et_emailSignIn).text.toString()
            val password:String=findViewById<TextView>(R.id.et_passwordSignIn).text.toString()
            if (validateForm(email, password))
            {
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
                Log.d(TAG, "firebaseAuthWithGoogle: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
                //}
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user!= null) {
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
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    showProgressDialog(getString(R.string.please_wait))
                    FirestoreClass().loadUserData(this)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(
            signInIntent, RC_SIGN_IN
        )
    }



    private fun validateForm(email: String, password: String):Boolean
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

    private fun login(email: String, password: String)
    {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Calling the FirestoreClass signInUser function to get the data of user from database.
                    FirestoreClass().loadUserData(this)
                } else {
                    Toast.makeText(
                        this,
                        "Email/password non corretti", // !! vuol dire che task NON deve essere nullo
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

    fun signInSuccess(user: User)
    {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish() // finisce l'attivita'
    }
}
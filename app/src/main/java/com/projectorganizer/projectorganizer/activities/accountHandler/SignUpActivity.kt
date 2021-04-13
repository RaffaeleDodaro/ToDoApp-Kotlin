package com.projectorganizer.projectorganizer.activities.accountHandler

import android.R.id.message
import android.content.Context
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


class SignUpActivity : BaseActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "GoogleActivity"
    private lateinit var auth: FirebaseAuth
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        setupActionBar()
        findViewById<Button>(R.id.btn_sign_upGoogle).setOnClickListener {

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
                Log.d(TAG, "firebaseAuthWithGoogle: " + account.id)
                firebaseAuthWithGoogle(account.idToken!!)

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

                    val firebaseUser: FirebaseUser = task.result!!.user!! // !! lancia un'eccezione se result o user sono nulli
                    val user2 = User(firebaseUser.uid, user.displayName, user.email) // uid Returns a string used to uniquely identify your user in your Firebase project's

                    FirestoreClass().registerUser(this, user2)
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
        //FirebaseAuth.getInstance().signOut() //faccio il signout e obbligo l'utente ad accedere al nuovo account
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
                    //emailProva=email
                    FirestoreClass().registerUser(this, user)
                } else {
                    Toast.makeText(this, "Registrazione non riuscita", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
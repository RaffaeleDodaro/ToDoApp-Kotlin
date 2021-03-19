package com.projectorganizer.projectorganizer.activities.accountHandler

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
            )
        }

        setupActionBar()
    }

    //aggiunge una freccia alla schermata sign up che permette di tornare indietro
    private fun setupActionBar()
    {
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_sign_in_activity))
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

    private fun validateForm(name:String,email:String,password:String):Boolean
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

    private fun registerUser()
    {
        val name:String = findViewById<TextView>(R.id.et_name).text.toString().trim(){it<=' '} //trim ritorna una stringa senza spazi
        val email:String = findViewById<TextView>(R.id.et_email).text.toString().trim(){it<=' '}
        val password:String = findViewById<TextView>(R.id.et_password).text.toString()
        if(validateForm(name,email,password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(){
                task -> hideProgressDialog()
                if (task.isSuccessful)
                {
                    val firebaseUser:FirebaseUser = task.result!!.user!!
                    val registeredEmail=firebaseUser.email!!
                    Toast.makeText(this,"$name Ti sei registrato correttamente con l'email $registeredEmail",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
                else
                {
                    Toast.makeText(this,"Registrazione non riuscita",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
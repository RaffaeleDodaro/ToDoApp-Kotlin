package com.todoapp.todoapp.activities.accountHandler

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.BaseActivity
import com.todoapp.todoapp.activities.IntroActivity
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.User


class MyProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        setupActionBar()
        FirestoreClass().loadUserData(this)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar // Retrieve a reference to this activity's ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        findViewById<Toolbar>(R.id.toolbar_my_profile_activity).setNavigationOnClickListener {
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            println("MMMMMMMMMMMMMMMMMMMMMMM")
            updateData()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }

    }

    fun setUserDataInUI(user: User) {
        /*
            Glide is a fast and efficient open source media management and image loading framework for
            Android that wraps media decoding, memory and disk caching, and resource pooling into a
            simple and easy to use interface.
        */
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById<ImageView>(R.id.iv_user_image))
        findViewById<TextView>(R.id.et_name).text = user.name
        findViewById<TextView>(R.id.et_email).text = user.email
    }

    private fun updateData() {
        //showProgressDialog(resources.getString(R.string.please_wait))
        val user = FirebaseAuth.getInstance().currentUser
        val email: String = findViewById<EditText>(R.id.et_email).text.toString()
        val name: String = findViewById<EditText>(R.id.et_name).text.toString()
        val password: String = findViewById<EditText>(R.id.et_password).text.toString()

//        if (password != "") {
//            user!!.updatePassword(password)
//        }

        if(!(user!!.email.equals(email,true))) { //da tenere sott'occhio l'if
            user.verifyBeforeUpdateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email sent.
                        // User must click the email link before the email is updated.
                        FirestoreClass().editEmail(this, user.uid, email)
                    } else {
                        // An error occurred.
                        println("errore verifyBeforeUpdateEmail")
                    }
                }
        }

        if(!(user.displayName.equals(name,true)))
            FirestoreClass().editName(this, user.uid, name)
    }

    fun editUserSuccessfully() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, IntroActivity::class.java) //cambio activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
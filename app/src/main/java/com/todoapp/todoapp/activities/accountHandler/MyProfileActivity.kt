package com.todoapp.todoapp.activities.accountHandler

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.BaseActivity
import com.todoapp.todoapp.activities.IntroActivity
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.User
import com.todoapp.todoapp.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException


class MyProfileActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)
        findViewById<CircleImageView>(R.id.iv_profile_user_image).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
                Constants.showImageChooser(this)
            else {
                ActivityCompat.requestPermissions( //fa apparire il rettangolino dove l'utente sceglie se concedere i permessi
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Constants.showImageChooser(this) //apro la galleria del telefono
            else
                Toast.makeText(
                    this,
                    "Hai negato i permessi per lo storage ma li puoi accettare dalle impostazioni del dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(findViewById<ImageView>(R.id.iv_profile_user_image))
                /*  Glide is a fast and efficient open source media management and
                    image loading framework for Android that wraps media decoding,
                    memory and disk caching, and resource pooling into a simple and
                    easy to use interface.*/
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_my_profile_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_title)
        }
        findViewById<Toolbar>(R.id.toolbar_my_profile_activity).setNavigationOnClickListener {
            onBackPressed()
            // Set a listener to respond to navigation events.
            // This listener will be called whenever the user clicks the navigation button at the start of the toolbar.
            // An icon must be set for the navigation button to appear.
        }
        findViewById<Button>(R.id.btn_update).setOnClickListener {
            if (mSelectedImageFileUri != null)
                uploadUserImage()
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateData()
            }
        }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user
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
            .into(findViewById<ImageView>(R.id.iv_profile_user_image))
        findViewById<TextView>(R.id.et_name).text = user.name
        findViewById<TextView>(R.id.et_email).text = user.email
    }

    private fun updateData() {
        //showProgressDialog(resources.getString(R.string.please_wait))
        val name: String = findViewById<EditText>(R.id.et_name).text.toString()
        val email: String = findViewById<EditText>(R.id.et_email).text.toString()
        val password: String = findViewById<EditText>(R.id.et_password).text.toString()
        var anyChanges: Boolean = false

        if (email.isNotEmpty() && email != mUserDetails.email)
            alertDialogForChangeData(email, "")

        if (password.isNotEmpty())
            alertDialogForChangeData("", password)

        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChanges = true
        }

        if (name != mUserDetails.name) {
            userHashMap[Constants.NAME] = name
            anyChanges = true
        }

        if (anyChanges)
            FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    private fun changePassword(password: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user!!.updatePassword(password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this,
                    "Password cambiata correttamente.\n Accedi nuovamente",
                    Toast.LENGTH_LONG
                )
                    .show()
                editUserSuccessfully()
            } else
                Toast.makeText(this, "Password non cambiata correttamente", Toast.LENGTH_LONG)
                    .show()
        }
    }

    private fun changeEmail(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        println(" nuova email: $email")
        if (!(user!!.email.equals(email, true))) { //da tenere sott'occhio l'if
            user.verifyBeforeUpdateEmail(email)
                .addOnCompleteListener { task ->
                    println(" nuova email: $email")
                    if (task.isSuccessful) {
                        // Email sent.
                        // User must click the email link before the email is updated.
                        FirestoreClass().editEmail(this, user.uid, email)
                    } else {
                        Toast.makeText(this, "Operazione non completata!", Toast.LENGTH_SHORT).show()
                        println("errore verifyBeforeUpdateEmail")
                    }
                }
        }
    }

    private fun alertDialogForChangeData(email: String, password: String) {
        //faccio apparire un messagebox per far confermare le modifiche
        val builder = AlertDialog.Builder(this@MyProfileActivity)
        builder.setTitle("Attenzione")

        if (email != "" && password != "")
            builder.setMessage("Sei sicuro di voler modificare mail e password?")
        else if (email != "" && password == "")
            builder.setMessage("Sei sicuro di voler modificare l'indirizzo email?")
        else if (email == "" && password != "")
            builder.setMessage("Sei sicuro di voler modificare la password?")

        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Si") { dialogInterface, which ->
            dialogInterface.dismiss() //Dismiss this dialog, removing it from the screen.

            if (email != "" && password != "") {
                println(" nuova email: $email")
                changeEmail(email)
                changePassword(password)
            } else if (password != "")
                changePassword(password)
            else if (password == "")
                changeEmail(email)
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss() //Dismiss this dialog, removing it from the screen.
            Toast.makeText(applicationContext, "Hai annullato la modifica", Toast.LENGTH_LONG)
                .show()
            //ApplicationContext is an interface for providing configuration information to an application.
        }
        builder.create().show()
    }

    fun editUserSuccessfully() {
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, IntroActivity::class.java) //cambio activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        // FLAG_ACTIVITY_CLEAR_TOP - If set, and the activity being launched is already running
        // in the current task, then instead of launching a new instance of that activity, all
        // of the other activities on top of it will be closed and this Intent will be delivered
        // to the (now on top) old activity as a new Intent.

        // FLAG_ACTIVITY_NEW_TASK - If set, this activity will become the start of a new task on
        // this history stack. A task (from the activity that started it to the next task activity)
        // defines an atomic group of activities that the user can move to. Tasks can be moved to
        // the foreground and background; all of the activities inside of a particular task always
        // remain in the same order.
        startActivity(intent)
        finish()
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"
                        + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this,
                    mSelectedImageFileUri
                )
            )
            // StorageReference = Represents a reference to a Google Cloud Storage object.
            // Developers can upload and download objects, get/set object metadata, and delete
            // an object at a specified path.


            // putFile = uploads from a content URI to this StorageReference
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
                Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.i("Url immagine scaricabile: ", uri.toString())
                    mProfileImageURL = uri.toString()
                    updateData()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG)
                        .show()
                    hideProgressDialog()
                }
            }
            findViewById<Button>(R.id.btn_update).setOnClickListener {
                if (mSelectedImageFileUri != null)
                    uploadUserImage()
            }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK) //Call this to set the result that your activity will return to its caller.
        finish()
    }
}
package com.todoapp.todoapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.Board
import com.todoapp.todoapp.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class EditBoardActivity : BaseActivity() {
    private lateinit var oldTitle: String
    private lateinit var document_id: String
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_board)

        if (intent.hasExtra(Constants.TITLE))
            oldTitle = intent.getStringExtra(Constants.TITLE).toString()
        if (intent.hasExtra(Constants.DOCUMENT_ID))
            document_id = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        setupActionBar()

        findViewById<EditText>(R.id.et_board_name_edit).setText(oldTitle)

        findViewById<Button>(R.id.btn_edit).setOnClickListener {
            val newName: String = findViewById<EditText>(R.id.et_board_name_edit).text.toString()
            if (newName != "") {
                showProgressDialog(resources.getString(R.string.please_wait))
                editBoard(newName)
            } else Toast.makeText(this, "Inserisci il nome", Toast.LENGTH_SHORT).show()
        }

        findViewById<CircleImageView>(R.id.iv_board_image).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
                //fa apparire il rettangolino dove l'utente sceglie se concedere i permessi
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            } else {
                Toast.makeText(
                    this,
                    "Hai negato i permessi per lo storage ma li puoi accettare dalle impostazioni del dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(findViewById<ImageView>(R.id.iv_board_image))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun editBoard(newName: String) {
        
        FirestoreClass().editBoardName(this, document_id, newName)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_edit_board)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar // Retrieve a reference to this activity's ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            actionBar.title = oldTitle
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    fun editBoardSuccessfully() {
        startActivity(Intent(this, MainActivity::class.java))
        hideProgressDialog()
        finish()
    }
}
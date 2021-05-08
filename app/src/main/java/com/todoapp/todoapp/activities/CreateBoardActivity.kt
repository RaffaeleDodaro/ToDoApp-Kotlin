package com.todoapp.todoapp.activities

import Board
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isEmpty
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
import kotlin.random.Random

class CreateBoardActivity : BaseActivity() {

    private lateinit var userName: String
    private var mSelectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createboard)
        setupActionBar()

        if (intent.hasExtra(Constants.NAME))
            userName = intent.getStringExtra(Constants.NAME).toString()

        findViewById<Button>(R.id.btn_create).setOnClickListener {
            if (findViewById<EditText>(R.id.et_board_name).text.toString() != "") {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
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

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar // Retrieve a reference to this activity's ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        findViewById<Toolbar>(R.id.toolbar_create_board).setNavigationOnClickListener {
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }

    }

    private fun randomImage(): String {
        val images = ArrayList<String>()
        images.add("https://image.flaticon.com/icons/png/512/1567/1567073.png")
        images.add("https://icons.iconarchive.com/icons/cornmanthe3rd/squareplex/512/Utilities-tasks-icon.png")
        images.add("https://icons.iconarchive.com/icons/cornmanthe3rd/metronome/256/Utilities-tasks-icon.png")
        images.add("https://www.kpcommunication.it/wordpress/wp-content/uploads/2018/02/to-do-list-png-big-image-png-1923.png")
        images.add("https://icons.iconarchive.com/icons/oxygen-icons.org/oxygen/256/Actions-view-calendar-tasks-icon.png")
        return images.get(Random.nextInt(0, images.size - 1))
    }

    private fun createBoard() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
//        val board = Board(findViewById<AppCompatEditText>(R.id.et_board_name).text.toString(),
//                randomImage(), userName, assignedUsersArrayList)
        val board = Board(
            findViewById<AppCompatEditText>(R.id.et_board_name).text.toString(),
            mSelectedImageFileUri.toString(), userName, assignedUsersArrayList
        )
        FirestoreClass().createBoard(this, board)
    }

    fun createBoardFromBackup(board: Board) {
        FirestoreClass().createBoardFromBackup(this, board)
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun boardCreatedSuccessfullyFromBackup() {
        //setResult(Activity.RESULT_OK)
        finish()
    }
}

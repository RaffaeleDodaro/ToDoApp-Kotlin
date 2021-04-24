package com.todoapp.todoapp.activities

import Board
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isEmpty
import com.google.android.material.textfield.TextInputLayout
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.utils.Constants
import kotlin.random.Random

class CreateBoardActivity : BaseActivity() {

    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createboard)
        setupActionBar()

        if (intent.hasExtra(Constants.NAME))
            userName = intent.getStringExtra(Constants.NAME).toString()

        findViewById<Button>(R.id.btn_create).setOnClickListener {
            if(findViewById<EditText>(R.id.et_board_name).text.toString() != ""){
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
            else Toast.makeText(this,"Inserisci il nome",Toast.LENGTH_SHORT).show()
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
        var board = Board(findViewById<AppCompatEditText>(R.id.et_board_name).text.toString(),
                randomImage(), userName, assignedUsersArrayList)
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

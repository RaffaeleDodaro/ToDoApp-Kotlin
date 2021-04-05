package com.projectorganizer.projectorganizer.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.utils.Constants

class CreateBoardActivity : BaseActivity() {

    private lateinit var userName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createboard)
        setupActionBar()

        if(intent.hasExtra(Constants.NAME))
            userName = intent.getStringExtra(Constants.NAME).toString()


        findViewById<Button>(R.id.btn_create).setOnClickListener {
            showProgressDialog(resources.getString(R.string.please_wait))
            createBoard()
        }
    }

    private fun setupActionBar()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board)
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar // Retrieve a reference to this activity's ActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=resources.getString(R.string.create_board_title)
        }
        findViewById<Toolbar>(R.id.toolbar_create_board).setNavigationOnClickListener{
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    private fun createBoard()
    {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
        var board= Board(findViewById<AppCompatEditText>(R.id.et_board_name).text.toString(),
        "",userName,assignedUsersArrayList)
        FirestoreClass().createBoard(this,board)
    }

    fun boardCreatedSuccessfully()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}
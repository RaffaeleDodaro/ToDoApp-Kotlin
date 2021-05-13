package com.todoapp.todoapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.todoapp.todoapp.R
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.utils.Constants

class EditBoardActivity : BaseActivity() {
    private lateinit var oldTitle: String
    private lateinit var document_id: String

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
package com.projectorganizer.projectorganizer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.utils.Constants

class CardDetailsActivity : AppCompatActivity() {

    private lateinit var boardDetail: Board
    private var taskListPosition=-1
    private var cardPosition=-1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        setupActionBar()
        var et_name_card_details=findViewById<EditText>(R.id.et_name_card_details)
        et_name_card_details.setText(boardDetail.taskList[taskListPosition].cards[cardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

    }
    private fun setupActionBar()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_card_details_activity)
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar // Retrieve a reference to this activity's ActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=boardDetail.taskList[taskListPosition].cards[cardPosition].name
        }
        findViewById<Toolbar>(R.id.toolbar_card_details_activity).setNavigationOnClickListener{
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun getIntentData()
    {
        if(intent.hasExtra(Constants.BOARD_DETAIL))
            boardDetail= intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL) as Board

        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION))
            taskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)

        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION))
            cardPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)

    }
}
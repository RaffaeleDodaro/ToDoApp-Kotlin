package com.projectorganizer.projectorganizer.activities

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.adapters.TaskListItemAdapter
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.models.Card
import com.projectorganizer.projectorganizer.models.Task
import com.projectorganizer.projectorganizer.utils.Constants

class CardDetailsActivity : BaseActivity() {

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

        findViewById<Button>(R.id.btn_update_card_details).setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty())
                updateCardDetails()
            else
                Toast.makeText(this,"Inserisci un nome della sottoattivita'",Toast.LENGTH_SHORT).show()
        }
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

    private fun updateCardDetails() {

        // Here we have updated the card name using the data model class.
        val card = Card(
                findViewById<EditText>(R.id.et_name_card_details).text.toString(),
                boardDetail.taskList[taskListPosition].cards[cardPosition].createdBy,
        )

        // Here we have assigned the update card details to the task list using the card position.
        boardDetail.taskList[taskListPosition].cards[cardPosition] = card

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetail)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun deleteCard() {
        // Here we have got the cards list from the task item list using the task list position.
        val cardsList: ArrayList<Card> = boardDetail.taskList[taskListPosition].cards
        // Here we will remove the item from cards list using the card position.
        cardsList.removeAt(cardPosition)

        val taskList: ArrayList<Task> = boardDetail.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[taskListPosition].cards = cardsList

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, boardDetail)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(
                resources.getString(
                        R.string.confirmation_message_to_delete_card,
                        cardName
                )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_delete_card -> {
                // TODO (Step 9: Call the function for showing an alert dialog for deleting the card.)
                // START
                alertDialogForDeleteCard(boardDetail.taskList[taskListPosition].cards[cardPosition].name)
                // END
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
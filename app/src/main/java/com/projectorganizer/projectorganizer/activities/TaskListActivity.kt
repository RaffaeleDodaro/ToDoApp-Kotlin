package com.projectorganizer.projectorganizer.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import java.text.FieldPosition

class TaskListActivity : BaseActivity() {

    //variabili globali che vengono inizializzate piu' tardi
    private lateinit var boardDetails:Board
    private lateinit var boardDocumentId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if(intent.hasExtra(Constants.DOCUMENT_ID))
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,boardDocumentId)
    }

    private fun setupActionBar()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar // Retrieve a reference to this activity's ActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=boardDetails.name
        }
        findViewById<Toolbar>(R.id.toolbar_task_list_activity).setNavigationOnClickListener{
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    fun boardDetails(board:Board)
    {
        boardDetails=board

        hideProgressDialog()
        setupActionBar()

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList= Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        var rv=findViewById<RecyclerView>(R.id.rv_task_list)

        rv.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        rv.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter= TaskListItemAdapter(this,board.taskList)
        rv.adapter=adapter// Attach the adapter to the recyclerView.
    }

    /**
     * mi serve per aggiornare o aggiungere alementi alla task list
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, boardDetails.documentId)
    }

    fun createTaskList(taskListName:String)
    {
        val task=Task(taskListName,FirestoreClass().getCurrentUserId())
        boardDetails.taskList.add(0,task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,boardDetails)
    }

    fun updateTaskList(position:Int,listName:String,model:Task)
    {
        val task=Task(listName,model.createdBy)
        boardDetails.taskList[position]=task
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,boardDetails)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, boardDocumentId)
        }
        // END
        else {
            Log.e("Error", "Error")
        }
    }

    fun deleteTaskList(position: Int)
    {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this,boardDetails)
    }

    fun addCardToTaskList(position: Int,cardName:String)
    {
        boardDetails.taskList.removeAt(boardDetails.taskList.size-1)

        val cardAssignedUsersList:ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())
        val card= Card(cardName,FirestoreClass().getCurrentUserId(),
                cardAssignedUsersList)

        val cardsList=boardDetails.taskList[position].cards
        cardsList.add(card)

        val task=Task(
                boardDetails.taskList[position].title,
                boardDetails.taskList[position].createdBy,
                cardsList
        )
        boardDetails.taskList[position]=task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity,boardDetails)

    }

    fun cardDetails(taskListPosition: Int,cardPosition: Int)
    {
        val intent=Intent(this@TaskListActivity,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    companion object{
        const val CARD_DETAILS_REQUEST_CODE:Int=14
    }
}
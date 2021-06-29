package com.todoapp.todoapp.activities

import com.todoapp.todoapp.models.Board
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.todoapp.todoapp.R
import com.todoapp.todoapp.adapters.TaskListItemAdapter
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.Card
import com.todoapp.todoapp.models.Task
import com.todoapp.todoapp.utils.Constants
import java.io.*
import java.lang.StringBuilder
import java.util.concurrent.CopyOnWriteArrayList


class TaskListActivity : BaseActivity() {

    val CARD_DETAILS_REQUEST_CODE: Int = 14
    val EDIT_BOARD_REQUEST_CODE: Int = 999
    //variabili globali che vengono inizializzate piu' tardi
    private lateinit var boardDetails: Board
    private lateinit var boardDocumentId: String

    private val mFireStore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID))
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this, boardDocumentId)
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar // Retrieve a reference to this activity's ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)

            actionBar.title = boardDetails.name
        }
        findViewById<Toolbar>(R.id.toolbar_task_list_activity).setNavigationOnClickListener {
            onBackPressed()
            // Set a listener to respond to navigation events.
            // This listener will be called whenever the user clicks the navigation button at the
            // start of the toolbar. An icon must be set for the navigation button to appear.
        }
        findViewById<Button>(R.id.btn_deleteBoard).setOnClickListener {
            alertDialogForDeleteBoard(boardDetails.name)
        }
        findViewById<Button>(R.id.btn_edit).setOnClickListener {
            editData()
        }
    }

    private fun editData() {
        val intent = Intent(this@TaskListActivity, EditBoardActivity::class.java)
        intent.putExtra(Constants.TITLE, boardDetails.name)
        intent.putExtra(Constants.DOCUMENT_ID, boardDetails.documentId)
        startActivityForResult(intent, EDIT_BOARD_REQUEST_CODE)
    }

    fun boardDetails(board: Board) {
        boardDetails = board

        hideProgressDialog()
        setupActionBar()

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList = Task(resources.getString(R.string.add_activity))
        board.taskList.add(addTaskList)

        val rv = findViewById<RecyclerView>(R.id.rv_task_list)

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.setHasFixedSize(true)

        // Create an instance of TaskListItemsAdapter and pass the task list to it.
        val adapter = TaskListItemAdapter(this, board.taskList)
        rv.adapter = adapter// Attach the adapter to the recyclerView.
    }

    /**
     * mi serve per aggiornare o aggiungere alementi alla task list
     */
    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this@TaskListActivity, boardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FirestoreClass().getCurrentUserId())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)
        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == CARD_DETAILS_REQUEST_CODE
        ) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, boardDocumentId)
        } else
            Log.e("Error", "Error")
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())
        val card = Card(
            cardName, FirestoreClass().getCurrentUserId(),
            cardAssignedUsersList
        )

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )
        boardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@TaskListActivity, boardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this@TaskListActivity, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    fun deleteBoardSuccessfully() {
        startActivity(Intent(this, MainActivity::class.java))
        hideProgressDialog()
        finish()
    }

    private fun alertDialogForDeleteBoard(title: String) {
        val builder = AlertDialog.Builder(this@TaskListActivity)
        builder.setTitle("Attenzione")
        builder.setMessage("Sei sicuro di voler cancellare $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Si") { dialogInterface, which ->
            dialogInterface.dismiss() //fa scomparire la finestra
            FirestoreClass().deleteBoard(this@TaskListActivity, boardDetails)
        }
        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext, "Hai annullato l'eliminazione", Toast.LENGTH_SHORT)
                .show()
        }
        builder.create().show()
    }
}
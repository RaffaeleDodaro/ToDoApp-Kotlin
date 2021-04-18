package com.todoapp.todoapp.activities

import Board
import android.R.attr
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
import com.todoapp.todoapp.R
import com.todoapp.todoapp.adapters.TaskListItemAdapter
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.Card
import com.todoapp.todoapp.models.Task
import com.todoapp.todoapp.utils.Constants
import java.io.*


class TaskListActivity : BaseActivity() {

    //variabili globali che vengono inizializzate piu' tardi
    private lateinit var boardDetails: Board
    private lateinit var boardDocumentId: String
    private val REQUEST_SAF=101
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
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
        findViewById<Button>(R.id.btn_export).setOnClickListener {
            exportData()
        }
        findViewById<Button>(R.id.btn_deleteBoard).setOnClickListener {
            alertDialogForDeleteBoard(boardDetails.name)
        }
        findViewById<Button>(R.id.btn_share).setOnClickListener {
            //shareData()
        }
    }
/*
    private fun shareData() {
        val file:File=exportData()
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, file)
            type = "text/txt"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }*/
    fun boardDetails(board: Board) {
        boardDetails = board

        hideProgressDialog()
        setupActionBar()

        // Here we are appending an item view for adding a list task list for the board.
        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        var rv = findViewById<RecyclerView>(R.id.rv_task_list)

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
                && requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this, boardDocumentId)
        }
        else if(requestCode==REQUEST_SAF) {
            when (resultCode) {
                RESULT_OK ->
                    if (attr.data != null
                            && data!!.data != null) {
                        writeInFile(data!!.data)
                    }

                RESULT_CANCELED -> {
                }
            }
        }
        else
            Log.e("Error", "Error")
    }

    private fun writeInFile(data: Uri?) {
        val outputStream: OutputStream?
        try {
            outputStream = data?.let { contentResolver.openOutputStream(it) }
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            bw.write(boardDetails.name + "\n") //"Nome della lista: " +
            bw.write(boardDetails.image + "\n") //"Immagine della lista: " +
            bw.write(boardDetails.createdBy + "\n") //"Creato da: " +
            for (i in boardDetails.assignedTo)
                bw.write(i + "\n") //"ASSEGNATO A: " +

            bw.write("fine assegnato a\n")
            var k = 0
            while (k < boardDetails.taskList.size - 1) {
                bw.write(boardDetails.taskList[k].title + "\n") //"Nome Task: " +
                bw.write(boardDetails.taskList[k].createdBy + "\n") //"Nome CreatoreTAsk: " +
                bw.write(".\n")
                var j = 0
                while (j < boardDetails.taskList[k].cards.size) {
                    bw.write(boardDetails.taskList[k].cards[j].name + "\n")//"Nome card: " +
                    bw.write(boardDetails.taskList[k].cards[j].createdBy + "\n")//"Nome createdBy: " +
                    bw.write(boardDetails.taskList[k].cards[j].assignedTo[0] + "\n")//"Nome assignedTo: " +
                    bw.write("/\n")
                    j += 1
                }
                k += 1
            }
            bw.write("\n")

            bw.flush()
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
        val card = Card(cardName, FirestoreClass().getCurrentUserId(),
                cardAssignedUsersList)

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

    private fun exportData() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .setType("text/plain")
                .addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_TITLE, boardDetails.name + ".txt");

        startActivityForResult(intent, REQUEST_SAF)
    }

    fun importBackup(array: ArrayList<String>) {




        /*
        var nomeBoard: String = array[0]
        var immagineBoard: String = array[1]
        var creatoDaBoard: String = array[2]

        array.removeAt(2)
        array.removeAt(1)
        array.removeAt(0)
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        println("ARRAY[0]: " + array[0])
        println("confronto: " + array[0].equals("fine assegnato a\n", true))
        while (!(array[0].contains("fine assegnato a"))) {
            assignedUsersArrayList.add(array[0])
            array.removeAt(0)
        }



        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
        println(assignedUsersArrayList)
        val board = Board(nomeBoard, immagineBoard, creatoDaBoard, assignedUsersArrayList)


        BoardActivity().createBoardFromBackup(board)

        FirestoreClass().getBoardDetails(this, board.documentId)

        for (i in array) {
            if (i.equals("."))
                break
            var nomeTask: String = i
            var nomeCreatoreTask: String = i + 1
            array.remove(i)
            array.remove(i + 1)
            val task = Task(nomeTask, nomeCreatoreTask)
            boardDetails.taskList.add(0, task)
            boardDetails.taskList.removeAt(board.taskList.size - 1)
            //showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().addUpdateTaskList(this, boardDetails)
        }*/




        /*var j = 0
        var cardsList = ArrayList<Card>()
        for (i in array) {
            var nome: String = array[0]
            var creatoDa: String = array[1]
            var assigned: String = array[2]

            var assignedTo = ArrayList<String>()

            assignedTo.add(assigned)
            assignedTo.add(FirestoreClass().getCurrentUserId())

            array.removeAt(2)
            array.removeAt(1)
            array.removeAt(0)

            val card = Card(nome, creatoDa, assignedTo)
            cardsList = board.taskList[j].cards

            cardsList.add(card)

            //addCardToTaskList(j,nome)
            val task = Task(
                    board.taskList[j].title,
                    board.taskList[j].createdBy,
                    cardsList
            )

            board.taskList[j] = task


            j += 1
        }
        /*val task = Task(
                board.taskList[j].title,
                board.taskList[j].createdBy,
                cardsList
        )

        board.taskList[j] = task

        FirestoreClass().addUpdateTaskList(this, board)*/*/
    }

    fun deleteBoardSuccessfully()
    {
        startActivityForResult(intent, MainActivity.DELETE_BOARD_REQUEST_CODE)
        hideProgressDialog()
        finish()
    }

    private fun alertDialogForDeleteBoard(title: String) {
        println("Attenzione")
        val builder = AlertDialog.Builder(this@TaskListActivity)
        builder.setTitle("Attenzione")
        builder.setMessage("Sei sicuro di voler cancellare $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Si") { dialogInterface, which ->
            dialogInterface.dismiss()
            FirestoreClass().deleteBoard(this@TaskListActivity, boardDetails)
        }
        builder.setNegativeButton("No"){ dialog, which ->
            Toast.makeText(applicationContext, "Hai annullato l'eliminazione", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    companion object {
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }

}
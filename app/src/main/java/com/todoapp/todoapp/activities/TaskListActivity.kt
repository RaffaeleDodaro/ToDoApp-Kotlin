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

    //variabili globali che vengono inizializzate piu' tardi
    private lateinit var boardDetails: Board
    private lateinit var boardDocumentId: String
    private val REQUEST_SAF = 101

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
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
        findViewById<Button>(R.id.btn_export).setOnClickListener {
            exportData()
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
        startActivityForResult(intent, TaskListActivity.EDIT_BOARD_REQUEST_CODE)
    }

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
        } else if (requestCode == REQUEST_SAF) {
            when (resultCode) {
                RESULT_OK ->
                    if (attr.data != null
                            && data!!.data != null) {
                        exportBackup(data!!.data)
                    }

                RESULT_CANCELED -> {
                }
            }
        } else
            Log.e("Error", "Error")
    }

    private fun exportBackup(data: Uri?) {
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
                bw.write("[" + boardDetails.taskList[k].title + "\n") //"Nome Task: " +
                bw.write("]" + boardDetails.taskList[k].createdBy + "\n") //"Nome CreatoreTAsk: " +
                var j = 0
                while (j < boardDetails.taskList[k].cards.size) {
                    bw.write("{" + boardDetails.taskList[k].cards[j].name + "\n")//"Nome card: " +
                    bw.write("}" + boardDetails.taskList[k].cards[j].createdBy + "\n")//"Nome createdBy: " +

                    for ((f, l) in boardDetails.taskList[k].cards[j].assignedTo.withIndex()) {
                        bw.write("(" + boardDetails.taskList[k].cards[j].assignedTo[f] + "\n")
                    }//"Nome assignedTo: " +
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

    fun importBackup(reader: BufferedReader) {
        //showProgressDialog("Attendi")
        var array = CopyOnWriteArrayList<String>()
        while (reader.ready()) {
            var line: String = reader.readLine()
            array.add(line)
        }


        var nomeBoard: String = array[0]
        var immagineBoard: String = array[1]
        var creatoDaBoard: String = array[2]

        array.removeAt(2)
        array.removeAt(1)
        array.removeAt(0)

        println(nomeBoard)
        println(immagineBoard)
        println(creatoDaBoard)

        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        //println("ARRAY[0]: " + array[0])
        //println("confronto: " + array[0].equals("fine assegnato a\n", true))
        while (!(array[0].contains("fine assegnato a"))) {
            assignedUsersArrayList.add(array[0])
            array.removeAt(0)
        }
        array.removeAt(0)
        //println(assignedUsersArrayList[0])


        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
        println(assignedUsersArrayList)
        val board: Board = Board(nomeBoard, immagineBoard, creatoDaBoard, assignedUsersArrayList)


        CreateBoardActivity().createBoardFromBackup(board)







        mFireStore.collection(Constants.BOARDS)
                // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
                .whereArrayContains(Constants.ASSIGNED_TO, FirestoreClass().getCurrentUserId())
                .get() // Will get the documents snapshots.
                .addOnSuccessListener { document ->

                    for (i in document.documents) {
                        val board2 = i.toObject(Board::class.java)!!
                        if (board2.name == board.name) {
                            board2.documentId = i.id
                            FirestoreClass().getBoardDetails(this, board2.documentId)
                            var j = -1
                            var nomeTask: String = ""
                            var nomeCreatoreTask: String = ""
                            var nomeCard: String = ""
                            var nomeCreatoreCard: String = ""
                            for (i in array) {
                                if (i.startsWith("[")) {
                                    j = -1
                                    nomeTask = deleteFirstCharacter(i)
                                }

                                if (i.startsWith("]"))
                                    nomeCreatoreTask = deleteFirstCharacter(i)

                                if (i.startsWith("{")) {
                                    nomeCard = deleteFirstCharacter(i)
                                    j++
                                }

                                if (i.startsWith("}"))
                                    nomeCreatoreCard = deleteFirstCharacter(i)

                                if (nomeTask != "" && nomeCreatoreTask != "" &&
                                        nomeCard != "" && nomeCreatoreCard != "") {

                                    println("DENTRO IF nome task $nomeTask $nomeCreatoreTask")

                                    var task = Task(nomeTask, nomeCreatoreTask)
                                    board2.taskList.add(0, task)


                                    val taskListHashMap = HashMap<String, Any>()
                                    taskListHashMap[Constants.TASK_LIST] = board2.taskList

                                    mFireStore.collection(Constants.BOARDS)
                                            .document(board2.documentId)
                                            .update(taskListHashMap)
                                            .addOnSuccessListener {
                                                println("task creata correttamente")
                                            }
                                            .addOnFailureListener { e ->
                                            }


/*
                                    val cardAssignedUsersList: ArrayList<String> = ArrayList()
                                    cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())
                                    val card = Card(nomeCard, FirestoreClass().getCurrentUserId(),
                                            cardAssignedUsersList)

                                    val cardsList = board2.taskList[j].cards
                                    cardsList.add(card)

                                    task = Task(
                                            nomeTask
                                            nomeCreatoreTask,
                                            cardsList
                                    )


                                    board2.taskList[j] = task




                                    //codice di addUpdateTaskList firestoreclass
                                    val taskListHashMap = HashMap<String, Any>()
                                    taskListHashMap[Constants.TASK_LIST] = board2.taskList

                                    mFireStore.collection(Constants.BOARDS)
                                            .document(board2.documentId)
                                            .update(taskListHashMap)
                                            .addOnSuccessListener {
                                                println("card importate")
//                                                    setResult(Activity.RESULT_OK)
//                                                    finish()
                                            }
                                            .addOnFailureListener { e ->
                                            }

*/
                                    //nomeTask = ""
                                }
                            }
                        }
                    }
                }

                .addOnFailureListener { e ->
                }
    }

    private fun deleteFirstCharacter(stringa: String): String {
        return StringBuilder(stringa).deleteCharAt(0).toString()
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
            dialogInterface.dismiss()
            FirestoreClass().deleteBoard(this@TaskListActivity, boardDetails)
        }
        builder.setNegativeButton("No") { dialog, which ->
            Toast.makeText(applicationContext, "Hai annullato l'eliminazione", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    companion object {
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
        val EDIT_BOARD_REQUEST_CODE: Int = 999
    }
}
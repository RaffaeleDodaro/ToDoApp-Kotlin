package com.projectorganizer.projectorganizer.firebase

import Board
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.*
import com.projectorganizer.projectorganizer.activities.accountHandler.LoginActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.MyProfileActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.SignUpActivity
import com.projectorganizer.projectorganizer.models.User
import com.projectorganizer.projectorganizer.utils.Constants

class FirestoreClass:BaseActivity() {
    private val mFireStore=FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User) // activity e' l'attivita' in cui viene richiamato il metodo registerUser
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(userInfo,
            SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
        }.addOnFailureListener{
            e-> // e rappresenta exception
            Log.e(activity.javaClass.simpleName,"Errore",e)
        }
    }

    fun loadUserData(activity: Activity, readBoardList:Boolean=false) // activity e' l'attivita' in cui viene richiamato il metodo loadUserData
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get().addOnSuccessListener {document ->
            Log.e(activity.javaClass.simpleName, document.toString())
            var loggedInUser=document.toObject(User::class.java)
            if(loggedInUser !=null){
                when(activity){
                    is LoginActivity ->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser,readBoardList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            else
            {

            }

        }.addOnFailureListener{

                e->// e rappresenta exception
            when(activity){
                is LoginActivity ->{
                    activity.hideProgressDialog()
                }
                is MainActivity -> {
                    activity.hideProgressDialog()
                }
                is MyProfileActivity -> {
                    activity.hideProgressDialog()
                }
            }
            Log.e(activity.javaClass.simpleName,"Errore",e)
        }
    }

    //ritorna l'id univoco dell'utente
    fun getCurrentUserId():String{
        var currentUser = FirebaseAuth.getInstance().currentUser //Returns the currently signed-in FirebaseUser or null if there is none.
        var currentUserId=""
        if(currentUser!=null)
            currentUserId=currentUser.uid //Returns a string used to uniquely identify your user in your Firebase project's user database

        return currentUserId
    }

    fun getBoardDetails(activity: TaskListActivity,documentId:String)
    {
        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .document(documentId)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.toString())
               
                val board=document.toObject(Board::class.java)!!
                board.documentId=document.id
                // Here we have created a new instance for Boards ArrayList.
                // val boardsList: ArrayList<Board> = ArrayList()


                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
            }
    }

    fun createBoard(createBoardActivity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())

            .addOnSuccessListener {
                Log.e(createBoardActivity.javaClass.simpleName,"Board creata correttamente!")
                Toast.makeText(createBoardActivity,"Board creata correttamente!",Toast.LENGTH_SHORT).show()
                createBoardActivity.boardCreatedSuccessfully()
            }.addOnFailureListener{
                    e->
                    createBoardActivity.hideProgressDialog()
                    Log.e(
                            createBoardActivity.javaClass.simpleName,
                            "Errore",
                            e
                    )}
    }

    fun getBoardsList(activity: MainActivity) //from database
    {
        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
                // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
                .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
                .get() // Will get the documents snapshots.
                .addOnSuccessListener { document ->
                    // Here we get the list of boards in the form of documents.
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    // Here we have created a new instance for Boards ArrayList.
                    val boardsList: ArrayList<Board> = ArrayList()

                    // A for loop as per the list of documents to convert them into Boards ArrayList.
                    for (i in document.documents) {

                        val board = i.toObject(Board::class.java)!!
                        board.documentId = i.id

                        boardsList.add(board)
                    }

                    // Here pass the result to the base activity.
                    activity.populateBoardsListToUI(boardsList)
                }
                .addOnFailureListener { e ->

                    activity.hideProgressDialog()
                    Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                }
    }

    fun addUpdateTaskList(activity:Activity,board:Board)
    {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(taskListHashMap)
                .addOnSuccessListener {
                    Log.e(activity.javaClass.simpleName, "TaskList updated successfully.")

                    if (activity is TaskListActivity) {
                        activity.addUpdateTaskListSuccess()
                    } else if (activity is CardDetailsActivity) {
                        activity.addUpdateTaskListSuccess()
                    }
                }
                .addOnFailureListener { e ->
                    if (activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    } else if (activity is TaskListActivity) {
                        activity.hideProgressDialog()
                    }
                    Log.e(activity.javaClass.simpleName, "Error while creating a board.", e)
                }
    }

    fun importBackup(array:ArrayList<String>) {
        var nome: String = array[0]
        var immagine: String = array[1]
        var creatoDa: String = array[2]


        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
        var board: Board = Board(nome, immagine, creatoDa,assignedUsersArrayList)
        println(board.name)
        println(board.image)
        println(board.createdBy)
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())

                .addOnSuccessListener {
                    //Log.e(this.javaClass.simpleName,"Board creata correttamente!")
                    //Toast.makeText(this,"Board creata correttamente!",Toast.LENGTH_SHORT).show()
                    //hideProgressDialog()
                    setResult(Activity.RESULT_OK)
                    //finish()
                }.addOnFailureListener { e ->
                    hideProgressDialog()
                    println(nome)
                    println(immagine)
                    println(creatoDa)
                }
    }
}
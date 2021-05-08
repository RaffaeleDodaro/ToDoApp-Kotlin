package com.todoapp.todoapp.firebase

import Board
import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.*
import com.todoapp.todoapp.activities.accountHandler.LoginActivity
import com.todoapp.todoapp.activities.accountHandler.MyProfileActivity
import com.todoapp.todoapp.activities.accountHandler.SignUpActivity
import com.todoapp.todoapp.models.User
import com.todoapp.todoapp.utils.Constants

class FirestoreClass : BaseActivity() {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) // activity e' l'attivita' in cui viene richiamato il metodo registerUser
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(
            userInfo,
            SetOptions.merge()
        ).addOnSuccessListener {
            FirebaseAuth.getInstance().currentUser!!.sendEmailVerification()
            activity.userRegisteredSuccess()
        }.addOnFailureListener { e -> // e rappresenta exception
            Log.e(activity.javaClass.simpleName, "Errore", e)
        }
    }


    fun updateUserProfileData(activity:MyProfileActivity,userHashMap:HashMap<String,Any>)
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profilo aggiornato correttamente!")
                Toast.makeText(activity,"Profilo aggiornato correttamente!",Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }.addOnFailureListener{e->
                activity.hideProgressDialog()
                Log.i(activity.javaClass.simpleName,"Profilo non aggiornato")
                Toast.makeText(activity,"Profilo non aggiornato",Toast.LENGTH_LONG).show()
            }

    }

    fun loadUserData(
        activity: Activity,
        readBoardList: Boolean = false
    ) // activity e' l'attivita' in cui viene richiamato il metodo loadUserData
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get()
            .addOnSuccessListener { document ->
                //Log.e(activity.javaClass.simpleName, document.toString())
                val loggedInUser = document.toObject(User::class.java)
                if (loggedInUser != null) {
                    when (activity) {
                        is LoginActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, readBoardList)
                        }
                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

            }.addOnFailureListener {

                    e ->// e rappresenta exception
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Errore", e)
            }
    }

    //ritorna l'id univoco dell'utente
    fun getCurrentUserId(): String {
        val currentUser =
            FirebaseAuth.getInstance().currentUser //Returns the currently signed-in FirebaseUser or null if there is none.
        var currentUserId = ""
        if (currentUser != null)
            currentUserId = currentUser.uid //Returns a string used to uniquely identify your user in your Firebase project's user database

        return currentUserId
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        // The collection name for BOARDS
        mFireStore.collection(Constants.BOARDS)
            // A where array query as we want the list of the board in which the user is assigned. So here you can pass the current user id.
            .document(documentId)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                //Log.e(activity.javaClass.simpleName, document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                // Here we have created a new instance for Boards ArrayList.
                // val boardsList: ArrayList<Board> = ArrayList()


                activity.boardDetails(board)
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error mentre creo la board.", e)
            }
    }

    fun createBoard(createBoardActivity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(createBoardActivity.javaClass.simpleName, "Board creata correttamente!")

                Toast.makeText(createBoardActivity,"Board creata correttamente!",Toast.LENGTH_LONG).show()
                createBoardActivity.boardCreatedSuccessfully()
            }.addOnFailureListener { e ->
                createBoardActivity.hideProgressDialog()
                Log.e(createBoardActivity.javaClass.simpleName,"Errore createboard",e)
            }
    }

    fun createBoardFromBackup(createBoardActivity: CreateBoardActivity, board: Board) {
        mFireStore.collection(Constants.BOARDS).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(createBoardActivity.javaClass.simpleName, "Board creata correttamente!")
                Toast.makeText(createBoardActivity,"Board creata correttamente!",Toast.LENGTH_LONG).show()
                //boardActivity.boardCreatedSuccessfullyFromBackup()
            }.addOnFailureListener { e ->
                createBoardActivity.hideProgressDialog()
                Log.e(
                    createBoardActivity.javaClass.simpleName,
                    "Errore createboardfrombackup",
                    e
                )
            }
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
                //Log.e(activity.javaClass.simpleName, document.documents.toString())
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
                Log.e(activity.javaClass.simpleName, "Errore mentre creo la board.", e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList aggiornata.")

                if (activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, "Error mentre creo la board.", e)
            }
    }

    fun deleteBoard(activity: TaskListActivity, boardDetails: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDetails.documentId)
            .delete()
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board cancellata correttamente!")
                Toast.makeText(activity, "Board cancellata correttamente!", Toast.LENGTH_SHORT).show()
                activity.deleteBoardSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error mnentre creo la board.", e)
            }
    }

    fun editBoardName(activity: EditBoardActivity, documentId: String, newName: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .update(Constants.NAME, newName)
            .addOnSuccessListener {
                Toast.makeText(activity, "Board modificata correttamente!", Toast.LENGTH_SHORT)
                    .show()
                activity.editBoardSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error mnentre creo la board.", e)
            }
    }

    fun editEmail(activity: MyProfileActivity, idUser: String, email: String) {
        mFireStore.collection(Constants.USERS)
            .document(idUser)
            .update(Constants.EMAIL, email)
            .addOnSuccessListener {
                Toast.makeText(
                    activity,
                    "Utente modificato correttamente! Controlla l'email",
                    Toast.LENGTH_LONG
                )
                    .show()
                activity.editUserSuccessfully()
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "Errore mentre modifico email utente.", e)
            }
    }

}
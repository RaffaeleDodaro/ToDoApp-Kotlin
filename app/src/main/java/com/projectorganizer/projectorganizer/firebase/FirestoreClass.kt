package com.projectorganizer.projectorganizer.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectorganizer.projectorganizer.activities.Board
import com.projectorganizer.projectorganizer.activities.CreateBoardActivity
import com.projectorganizer.projectorganizer.activities.MainActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.LoginActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.MyProfileActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.SignUpActivity
import com.projectorganizer.projectorganizer.models.User
import com.projectorganizer.projectorganizer.utils.Constants

class FirestoreClass {
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

    fun loadUserData(activity: Activity) // activity e' l'attivita' in cui viene richiamato il metodo loadUserData
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get().addOnSuccessListener {document ->
        var loggedInUser=document.toObject(User::class.java)!!
            when(activity){
                is LoginActivity ->{
                    activity.signInSuccess(loggedInUser)
                }
                is MainActivity -> {
                    activity.updateNavigationUserDetails(loggedInUser)
                }
                is MyProfileActivity -> {
                    activity.setUserDataInUI(loggedInUser)
                }
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
            }
            Log.e("FirestoreClassLoginActivity","Errore",e)
        }
    }

    //ritorna l'id univoco dell'utente
    fun getCurrentUserId():String{
        var currentUser = FirebaseAuth.getInstance().currentUser //Returns the currently signed-in FirebaseUser or null if there is none.
        var currentUserId=""
        if(currentUser!=null)
        {
            currentUserId=currentUser.uid //Returns a string used to uniquely identify your user in your Firebase project's user database
        }
        return currentUserId
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
}
package com.projectorganizer.projectorganizer.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectorganizer.projectorganizer.activities.accountHandler.LoginActivity
import com.projectorganizer.projectorganizer.activities.accountHandler.SignUpActivity
import com.projectorganizer.projectorganizer.models.User
import com.projectorganizer.projectorganizer.utils.Constants

class FirestoreClass {
    private val mFireStore=FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo: User)
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).set(userInfo,
            SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
        }.addOnFailureListener{
            e->
            Log.e(activity.javaClass.simpleName,"Errore",e)
        }
    }

    fun signInUser(activity:LoginActivity)
    {
        mFireStore.collection(Constants.USERS).document(getCurrentUserId()).get().addOnSuccessListener {document ->
        var loggedInUser=document.toObject(User::class.java)!!
                activity.signInSuccess(loggedInUser)
        }.addOnFailureListener{
                e->
            Log.e("FirestoreClassLoginActivity","Errore",e)
        }
    }

    //ritorna l'id univoco dell'utente
    fun getCurrentUserId():String{
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId=""
        if(currentUser!=null)
        {
            currentUserId=currentUser.uid
        }
        return currentUserId
    }
}
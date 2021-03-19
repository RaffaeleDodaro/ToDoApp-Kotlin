package com.projectorganizer.projectorganizer.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.projectorganizer.projectorganizer.activities.accountHandler.SignUpActivity

class FirestoreClass {
    private val mFireStore=FirebaseFirestore.getInstance()

    fun registerUser(activity:SignUpActivity,userInfo:User)
    {

    }
}
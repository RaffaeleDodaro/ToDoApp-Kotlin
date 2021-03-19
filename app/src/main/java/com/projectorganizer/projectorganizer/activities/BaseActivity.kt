package com.projectorganizer.projectorganizer.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.projectorganizer.projectorganizer.R

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExitPressedOnce=false

    private lateinit var mProgressDialog: Dialog  //ogni volta che voglio far vedere all'utente
    //un caricamento uso questa variabile, come per esempio quando fa il login

    fun showProgressDialog(text:String){
        mProgressDialog=Dialog(this)

        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.findViewById<TextView>(R.id.tv_progress_text).text=text

        mProgressDialog.show()
    }

    fun hideProgressDialog()
    {
        mProgressDialog.dismiss()
    }

    fun getCurrentUserID():String
    {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToexit()
    {
        //al secondo click fai questo
        if(doubleBackToExitPressedOnce)
        {
            super.onBackPressed()
            return
        }

        //al primo click fai questo.
        this.doubleBackToExitPressedOnce=true
        Toast.makeText(this,resources.getString(R.string.please_click_back_again_to_exit),Toast.LENGTH_SHORT).show()

        //se l'utente preme due volte il tasto indietro e la seconda e' all'"interno" di due secodni -> chiudi l'applicazione
        Handler(Looper.getMainLooper()).postDelayed(
            {
                doubleBackToExitPressedOnce=false
            }
            ,2000)
    }

    fun showErrorSnackBar(message:String)
    {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )
        snackBar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

}
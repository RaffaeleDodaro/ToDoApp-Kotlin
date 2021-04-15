package com.projectorganizer.projectorganizer.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.utils.Constants
import kotlin.random.Random

class CreateBoardActivity : BaseActivity() {

    private lateinit var userName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createboard)
        setupActionBar()

        if(intent.hasExtra(Constants.NAME))
            userName = intent.getStringExtra(Constants.NAME).toString()


        findViewById<Button>(R.id.btn_create).setOnClickListener {
            showProgressDialog(resources.getString(R.string.please_wait))
            createBoard()
        }
    }

    private fun setupActionBar()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_create_board)
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar // Retrieve a reference to this activity's ActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=resources.getString(R.string.create_board_title)
        }
        findViewById<Toolbar>(R.id.toolbar_create_board).setNavigationOnClickListener{
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    private fun randomElement(): String {
        val images=ArrayList<String>()
        images.add("https://lh3.googleusercontent.com/proxy/jg2EnUEg9bUPxcKH6eXiMRbSDEPc9CHsH2GZnE0ETYyC_EwWmiaN91FX0_CyGlNbO3kQCttjdLIh7EkJgkPREYUQxMdrjBPMvAPBq_Q_Pid8t36t5uRAQnp_LQ-9LY2y8QzOoUZTl28BIXAXtgmY8wSkT8fmpJZ22uqaUw4_-aQkyOpuIZTad_Sw2_Jx_T4iuFFZu1WkkSc9Xr_esLSY--L1UfAyoHnQWXNxHYCQWxY5vgzSBc01jbJcGDG3y0JbqplgDA0ZMzfgEo-k")
        images.add("https://images-eu.ssl-images-amazon.com/images/I/51NvZiQtk6L.png")
        images.add("https://image.flaticon.com/icons/png/512/1567/1567073.png")
        images.add("https://www.kpcommunication.it/wordpress/wp-content/uploads/2018/02/to-do-list-png-big-image-png-1923.png")
        return images.get(Random.nextInt(0,images.size-1))
    }
    private fun createBoard()
    {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(FirestoreClass().getCurrentUserId())
        var board= Board(findViewById<AppCompatEditText>(R.id.et_board_name).text.toString(),
                randomElement(),userName)
        FirestoreClass().createBoard(this,board)
    }

    fun boardCreatedSuccessfully()
    {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}
package com.projectorganizer.projectorganizer.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.adapters.TaskListItemAdapter
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.models.Task
import com.projectorganizer.projectorganizer.utils.Constants

class TaskListActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        var boardDocumentId=""
        if(intent.hasExtra(Constants.DOCUMENT_ID))
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,boardDocumentId)
    }
    private fun setupActionBar(title:String)
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbar)
        val actionBar=supportActionBar // Retrieve a reference to this activity's ActionBar
        if(actionBar!=null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title=title
        }
        findViewById<Toolbar>(R.id.toolbar_task_list_activity).setNavigationOnClickListener{
            onBackPressed()
            //Set a listener to respond to navigation events.
            //This listener will be called whenever the user clicks the navigation button at the start of the toolbar. An icon must be set for the navigation button to appear.
        }
    }

    fun boardDetails(board:Board)
    {
        hideProgressDialog()
        setupActionBar(board.name)
        val addTaskList= Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        var rv=findViewById<RecyclerView>(R.id.rv_task_list)

        rv.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        rv.setHasFixedSize(true)
        val adapter= TaskListItemAdapter(this,board.taskList)
        
        rv.adapter=adapter
    }
}
package com.todoapp.todoapp.activities

import Board
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.todoapp.todoapp.R
import com.todoapp.todoapp.activities.accountHandler.MyProfileActivity
import com.todoapp.todoapp.adapters.BoardItemsAdapter
import com.todoapp.todoapp.firebase.FirestoreClass
import com.todoapp.todoapp.models.User
import com.todoapp.todoapp.utils.Constants
import java.io.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName: String

    /**
     * A companion object to declare the constants.
     */
    companion object {
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE: Int = 11

        //A code for starting the create board activity for result
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
        const val DELETE_BOARD_REQUEST_CODE: Int = 13

        const val IMPORT_FILE = 111

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this) //Set a listener that will be notified when a menu item is selected.

        // Get the current logged in user details.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@MainActivity, true)

        val fab = findViewById<FloatingActionButton>(R.id.fab_create_board)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, BoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE) //appbarmain.xml
        }
    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()
        val rv_boards_list = findViewById<RecyclerView>(R.id.rv_boards_list)
        if (boardsList.size > 0) {
            rv_boards_list.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.GONE //con gone la tv diventa invisibile


            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)


            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter = BoardItemsAdapter(this, boardsList)
            rv_boards_list.adapter = adapter


            //click event for boards item and launch the TaskListActivity
            adapter.setOnClickListener(object :
                    BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            rv_boards_list.visibility = View.GONE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            showDrawer()
        }
    }

    private fun showDrawer() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)   //DrawerLayout acts as a top-level container for window
        // content that allows for interactive "drawer" views
        // to be pulled out from one or both vertical edges of the window

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            drawer.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout) //DrawerLayout acts as a top-level container for window
        // content that allows for interactive "drawer" views
        // to be pulled out from one or both vertical edges of the window
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            doubleBackToExit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId)//simile allo switch
        {
            R.id.nav_my_profile -> { //se e' stato premuto il mio profilo
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_log_out -> {//se e' stato premuto il logout
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java) //cambio activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            R.id.nav_import -> {//se e' stato premuto il per importa una todo list

                val intent = Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), IMPORT_FILE)

            }
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }


    fun updateNavigationUserDetails(user: User, readBoardList: Boolean) {
        /*
            Glide is a fast and efficient open source media management and image loading framework for
            Android that wraps media decoding, memory and disk caching, and resource pooling into a
            simple and easy to use interface.
        */
        hideProgressDialog()
        mUserName = user.name

        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(findViewById<ImageView>(R.id.nav_user_image_profile))
        findViewById<TextView>(R.id.tv_username).text = user.name
        if (readBoardList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE)
            FirestoreClass().loadUserData(this)
        else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE)
            FirestoreClass().getBoardsList(this)
        else if (resultCode == Activity.RESULT_OK && requestCode == DELETE_BOARD_REQUEST_CODE)
            FirestoreClass().getBoardsList(this)
        else if (resultCode == Activity.RESULT_OK)
            FirestoreClass().getBoardsList(this)
        else
            Log.e("Error", "Error")

        if (requestCode == IMPORT_FILE && resultCode == RESULT_OK) {
            val selectedFile = data?.data //The uri with the location of the file <p>
            readTextFromUri(selectedFile!!)
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri) {
        var array = ArrayList<String>()
        contentResolver.openInputStream(uri)?.use {
            inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use {
                reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    array.add(line)
                    line = reader.readLine()
                }
            }
        }
        TaskListActivity().importBackup(array)
    }
}
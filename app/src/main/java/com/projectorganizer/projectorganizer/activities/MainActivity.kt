package com.projectorganizer.projectorganizer.activities

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
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.accountHandler.MyProfileActivity
import com.projectorganizer.projectorganizer.adapters.BoardItemsAdapter
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.Board
import com.projectorganizer.projectorganizer.models.User
import com.projectorganizer.projectorganizer.utils.Constants
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files.exists
import java.util.jar.Manifest

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName:String

    /**
     * A companion object to declare the constants.
     */
    companion object{
        //A unique code for starting the activity for result
        const val MY_PROFILE_REQUEST_CODE:Int=11

        //A code for starting the create board activity for result
        const val CREATE_BOARD_REQUEST_CODE:Int=12

        const val IMPORT_FILE=111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this) //Set a listener that will be notified when a menu item is selected.

        // Get the current logged in user details.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@MainActivity, true)

        val fab=findViewById<FloatingActionButton>(R.id.fab_create_board)
        fab.setOnClickListener{
            val intent = Intent(this@MainActivity,CreateBoardActivity::class.java)


            intent.putExtra(Constants.NAME,mUserName)


            startActivityForResult(intent,CREATE_BOARD_REQUEST_CODE) //appbarmain.xml
        }
    }

    fun populateBoardsListToUI(boardsList:ArrayList<Board>)
    {
        hideProgressDialog()
        val rv_boards_list = findViewById<RecyclerView>(R.id.rv_boards_list)
        if(boardsList.size>0)
        {
            rv_boards_list.visibility= View.VISIBLE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility=View.GONE //con gone la tv diventa invisibile


            rv_boards_list.layoutManager=LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)


            // Create an instance of BoardItemsAdapter and pass the boardList to it.
            val adapter=BoardItemsAdapter(this,boardsList)
            rv_boards_list.adapter=adapter


            //click event for boards item and launch the TaskListActivity
            adapter.setOnClickListener(object :
                BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    // TODO (Step 4: Pass the documentId of a board through intent.)
                    // START
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    print("QUI\n")
                    startActivity(intent)
                    // END
                }
            })
        }
        else
        {
            rv_boards_list.visibility=View.GONE
            findViewById<TextView>(R.id.tv_no_boards_available).visibility=View.VISIBLE
        }
    }

    private fun setupActionBar()
    {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main_activity)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar.setNavigationOnClickListener {
            showDrawer()
        }
    }

    private fun showDrawer()
    {
        val drawer=findViewById<DrawerLayout>(R.id.drawer_layout)   //DrawerLayout acts as a top-level container for window
                                                                    // content that allows for interactive "drawer" views
                                                                    // to be pulled out from one or both vertical edges of the window

        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            drawer.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed()
    {
        val drawer=findViewById<DrawerLayout>(R.id.drawer_layout) //DrawerLayout acts as a top-level container for window
                                                                    // content that allows for interactive "drawer" views
                                                                    // to be pulled out from one or both vertical edges of the window
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            doubleBackToExit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId)//simile allo switch
        {
            R.id.nav_my_profile ->{ //se e' stato premuto il mio profilo
                startActivityForResult(Intent(this,MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_log_out ->{//se e' stato premuto il logout
                FirebaseAuth.getInstance().signOut()

                val intent=Intent(this,IntroActivity::class.java) //cambio activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            R.id.nav_import ->{//se e' stato premuto il per importa una todo list

                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), IMPORT_FILE)

            }
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }


    fun updateNavigationUserDetails(user: User, readBoardList:Boolean)
    {
        /*
            Glide is a fast and efficient open source media management and image loading framework for
            Android that wraps media decoding, memory and disk caching, and resource pooling into a
            simple and easy to use interface.
        */
        hideProgressDialog()
        mUserName=user.name

        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(findViewById<ImageView>(R.id.nav_user_image_profile))
        findViewById<TextView>(R.id.tv_username).text=user.name
        if(readBoardList)
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }
        else if(resultCode==Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE)
            FirestoreClass().getBoardsList(this)
        else
            Log.e("Error","Error")

        if(requestCode== IMPORT_FILE && resultCode== RESULT_OK)
        {
            val filePath = data?.data?.path
            //val filePath = data?.data?.toString()
            println(filePath)
        }
    }

    private fun openFile(path:Uri?)
    {

    }

}
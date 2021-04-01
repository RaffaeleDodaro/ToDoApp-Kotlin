package com.projectorganizer.projectorganizer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.projectorganizer.projectorganizer.R
import com.projectorganizer.projectorganizer.activities.accountHandler.MyProfileActivity
import com.projectorganizer.projectorganizer.firebase.FirestoreClass
import com.projectorganizer.projectorganizer.models.User

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this) //Set a listener that will be notified when a menu item is selected.
        FirestoreClass().loadUserData(this)

        val fab=findViewById<FloatingActionButton>(R.id.fab_create_board)
        fab.setOnClickListener{
            startActivity(Intent(this,CreateBoardActivity::class.java)) //appbarmain.xml
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
                startActivity(Intent(this,MyProfileActivity::class.java))
            }

            R.id.nav_log_out ->{//se e' stato premuto il logout
                FirebaseAuth.getInstance().signOut()

                val intent=Intent(this,IntroActivity::class.java) //cambio activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User)
    {
        /*
            Glide is a fast and efficient open source media management and image loading framework for
            Android that wraps media decoding, memory and disk caching, and resource pooling into a
            simple and easy to use interface.
        */
        Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(findViewById<ImageView>(R.id.nav_user_image_profile))
        findViewById<TextView>(R.id.tv_username).text=user.name

    }

}
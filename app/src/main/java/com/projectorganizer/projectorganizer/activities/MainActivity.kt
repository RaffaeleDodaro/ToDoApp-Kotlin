package com.projectorganizer.projectorganizer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.projectorganizer.projectorganizer.R

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupActionBar()
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener(this)
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
        val drawer=findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            drawer.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed()
    {
        val drawer=findViewById<DrawerLayout>(R.id.drawer_layout)
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else
            doubleBackToExit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId)//simile allo switch
        {
            R.id.nav_my_profile ->{ //se e' stato premuto il mio profilo
                Toast.makeText(this,"Il mio profilo",Toast.LENGTH_SHORT).show()
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

}
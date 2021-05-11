package com.bb.boombim
//
//import android.R

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(applicationContext)
        //toolBar

    }



    private fun init(applicationContext: Context) {
        //mapView
        val mapView = MapView(this)
        val mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView)

        val toolbar: Toolbar = findViewById(R.id.mainToolBar)
        this.setSupportActionBar(toolbar)

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tool_menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.menu_two)
        val view = MenuItemCompat.getActionView(menuItem)
        val profileImage: CircleImageView = view.findViewById(R.id.toolbar_profile_image)
        Glide
            .with(this)
            .load("https://images.unsplash.com/photo-1478070531059-3db579c041d5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80")
            .into(profileImage)
        profileImage.setOnClickListener {
            Toast.makeText(
                this@MainActivity,
                "Profile Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_one -> Toast.makeText(
                this@MainActivity,
                "Menu One Clicked",
                Toast.LENGTH_SHORT
            ).show()
            R.id.menu_two -> Toast.makeText(
                this@MainActivity,
                "Menu Two Clicked",
                Toast.LENGTH_SHORT
            ).show()
            R.id.menu_three -> Toast.makeText(
                this@MainActivity,
                "Menu Three Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

}
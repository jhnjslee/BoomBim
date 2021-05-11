package com.bb.boombim
//
//import android.R

import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity() {
    lateinit var locationManager : LocationManager

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

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager


        val currentLocationBtn : FloatingActionButton = findViewById(R.id.floatingActionButton)
        currentLocationBtn.setOnClickListener {
            try {

                // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,  // 등록할 위치제공자
                    100,  // 통지사이의 최소 시간간격 (miliSecond)
                    1.0f,  // 통지사이의 최소 변경거리 (m)
                    mLocationListener
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,  // 등록할 위치제공자
                    100,  // 통지사이의 최소 시간간격 (miliSecond)
                    1.0f,  // 통지사이의 최소 변경거리 (m)
                    mLocationListener
                )



                //txtCurrentPositionInfo.setText("위치정보 미수신중");
                //lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
            } catch (ex: SecurityException) {
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tool_menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.menu_three)
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
    private val mLocationListener : LocationListener by lazy{
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Toast.makeText(this@MainActivity , "locationChange",Toast.LENGTH_SHORT).show()
            }

        }
    }

}
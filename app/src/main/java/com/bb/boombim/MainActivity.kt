package com.bb.boombim
//
//import android.R

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import com.bb.boombim.ui.login.LoginActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.imangazaliev.circlemenu.CircleMenu
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.circlie_menu.*
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class MainActivity : AppCompatActivity(), MapView.CurrentLocationEventListener {
    lateinit var locationManager : LocationManager
    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    lateinit var mapView : MapView
    lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(applicationContext)
        //toolBar
        Toasty.error(this, "This is an error toast.", Toast.LENGTH_SHORT, true).show();

        if( !checkLocationServicesStatus()){
            showDialogForLocationServiceSetting()
        }else {
            checkRunTimePermission()
        }
        curvedBottomNavigationView.inflateMenu(R.menu.bottom_menu)
        curvedBottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page1 -> return@OnNavigationItemSelectedListener true
                R.id.page2 -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        val circleMenu = findViewById<CircleMenu>(R.id.floatingBb)
        circleMenu.setOnItemClickListener { menuButton ->
            when(menuButton){
                0 -> {
                    currentMode.text = " 즐겨찾기 "
                }
                1 -> {
                    currentMode.text = " 현재위치 "
                }
                2 -> {
                    currentMode.text = " 좋아요 "
                }
            }
            Log.d("circleMenu", menuButton.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        curvedBottomNavigationView.menu.getItem(0).setChecked(true)
    }

    private fun init(applicationContext: Context) {
        //mapView
        mapView = MapView(this)
        val mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView)

        val toolbar: Toolbar = findViewById(R.id.mainToolBar)
        this.setSupportActionBar(toolbar)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        mapView.setCurrentLocationEventListener(this)

        loadingDialog = LoadingDialog(this@MainActivity)
        currentMode.text = " 기본 "
        val currentLocationBtn : FloatingActionButton = findViewById(R.id.floatingCurrentLocation)
        currentLocationBtn.setOnClickListener {
            try {
                loadingDialog.show()
//                LoadingDialog(this).show()
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

        floatingBb.setOnClickListener {

            loadingDialog.show()
//            LoadingDialog(this).dismiss()
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

            R.id.menu_two -> {
                //로그인
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
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

                loadingDialog.dismiss()
                if(loadingDialog.isShowing){
                    loadingDialog.dismiss()
                }
                Toast.makeText(this@MainActivity, "피융", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }


    fun checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음
//            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    REQUIRED_PERMISSIONS.get(0)
                )
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(this@MainActivity, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG)
                    .show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@MainActivity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@MainActivity, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            """
            앱을 사용하기 위해서는 위치 서비스가 필요합니다.
            위치 설정을 수정하실래요?
            """.trimIndent()
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        })
        builder.setNegativeButton("취소",
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("currentResult", "0")
        Log.d("requestCode", requestCode.toString())
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }



    override fun onDestroy() {
        super.onDestroy()

    }





}
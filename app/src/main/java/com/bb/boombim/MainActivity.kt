package com.bb.boombim
//
//import android.R

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.MenuItemCompat
import com.bb.boombim.data.LikeLocation
import com.bb.boombim.data.LocationSearch
import com.bb.boombim.popup.LikePopup
import com.bb.boombim.ui.login.LoginActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imangazaliev.circlemenu.CircleMenu
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.circlie_menu.*
import net.daum.mf.map.api.CalloutBalloonAdapter
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), MapView.CurrentLocationEventListener {
    lateinit var locationManager : LocationManager
    private val eventListener = MarkerEventListener(this)

    var REQUIRED_PERMISSIONS = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    lateinit var mapView : MapView
    lateinit var loadingDialog: LoadingDialog
    var currentSearchMode : Int = 0 // 0
    var imm : InputMethodManager? = null


    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5370b9816cfb27b331eefc35e6b66bf1"  // REST API ???
        val db = Firebase.firestore
        lateinit var authStateListener : FirebaseAuth.AuthStateListener
        lateinit var mContext: Context
        var mAuth : FirebaseAuth = FirebaseAuth.getInstance()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init(applicationContext)
        //toolBar
        //Toasty.error(this, "This is an error toast.", Toast.LENGTH_SHORT, true).show();
        mContext = this@MainActivity

        Log.d("mautnMain", mAuth.currentUser.toString())

        authStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            Log.d("mautnMain", "")

            if (user != null) {
            } else {
            }
        }




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
                    currentMode.text = " T O P " // ??? 10
                }
                1 -> {
                    currentMode.text = " ???????????? "
                    try {
                        loadingDialog.show()
//                LoadingDialog(this).show()
                        // GPS ???????????? ????????? ????????? ??????????????? ????????? ????????????~!!!
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,  // ????????? ???????????????
                                100,  // ??????????????? ?????? ???????????? (miliSecond)
                                1.0f,  // ??????????????? ?????? ???????????? (m)
                                mLocationListener
                        )
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,  // ????????? ???????????????
                                100,  // ??????????????? ?????? ???????????? (miliSecond)
                                1.0f,  // ??????????????? ?????? ???????????? (m)
                                mLocationListener
                        )

                        var latitude = 0.0
                        var longitude = 0.0
                        var userLocation: Location = getLatLng()
                        if (userLocation != null) {
                            latitude = userLocation.latitude
                            longitude = userLocation.longitude
                            Log.d("CheckCurrentLocation", "?????? ??? ?????? ???: ${latitude}, ${longitude}")

                            var mGeoCoder = Geocoder(applicationContext, Locale.KOREAN)
                            var mResultList: List<Address>? = null
                            try {
                                mResultList = mGeoCoder.getFromLocation(
                                        latitude!!, longitude!!, 1
                                )
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            if (mResultList != null) {
                                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
                            }
                        }

                        //txtCurrentPositionInfo.setText("???????????? ????????????");
                        //lm.removeUpdates(mLocationListener);  //  ?????????????????? ????????? ??????????????? ???????????? ??????.
                    } catch (ex: SecurityException) {
                    }

                }
                2 -> {
                    currentMode.text = " ????????? "
                }
            }
            Log.d("circleMenu", menuButton.toString())
        }
    }

    override fun onStart() {
        super.onStart()

        mAuth.addAuthStateListener(authStateListener)
        curvedBottomNavigationView.menu.getItem(0).setChecked(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(applicationContext: Context) {
        //mapView
        mapView = MapView(this)
        val mapViewContainer = findViewById<View>(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapView)
        mapView.setCurrentLocationEventListener(this)
        mapView.setCalloutBalloonAdapter(CustomBalloonAdapter(layoutInflater))
        mapView.setPOIItemEventListener(eventListener)
        val toolbar: Toolbar = findViewById(R.id.mainToolBar)
        this.setSupportActionBar(toolbar)

        imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //location Manager
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        loadingDialog = LoadingDialog(this@MainActivity)

        //Map Mode
        currentMode.text = " ?????? "
        val currentLocationBtn : FloatingActionButton = findViewById(R.id.floatingCurrentLocation)
        currentLocationBtn.setOnClickListener {
            try {
                loadingDialog.show()
//                LoadingDialog(this).show()
                // GPS ???????????? ????????? ????????? ??????????????? ????????? ????????????~!!!
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,  // ????????? ???????????????
                        100,  // ??????????????? ?????? ???????????? (miliSecond)
                        1.0f,  // ??????????????? ?????? ???????????? (m)
                        mLocationListener
                )
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,  // ????????? ???????????????
                        100,  // ??????????????? ?????? ???????????? (miliSecond)
                        1.0f,  // ??????????????? ?????? ???????????? (m)
                        mLocationListener
                )

                var latitude = 0.0
                var longitude = 0.0
                var userLocation: Location = getLatLng()
                if(userLocation != null){
                    latitude = userLocation.latitude
                    longitude = userLocation.longitude
                    Log.d("CheckCurrentLocation", "?????? ??? ?????? ???: ${latitude}, ${longitude}")

                    var mGeoCoder =  Geocoder(applicationContext, Locale.KOREAN)
                    var mResultList: List<Address>? = null
                    try{
                        mResultList = mGeoCoder.getFromLocation(
                                latitude!!, longitude!!, 1
                        )
                    }catch (e: IOException){
                        e.printStackTrace()
                    }
                    if(mResultList != null){
                        Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
                    }
                }

                //txtCurrentPositionInfo.setText("???????????? ????????????");
                //lm.removeUpdates(mLocationListener);  //  ?????????????????? ????????? ??????????????? ???????????? ??????.
            } catch (ex: SecurityException) {
            }

        }

        floatingBb.setOnClickListener {
            loadingDialog.show()
        }



        //?????? ??? ?????? ???
        searchTitle_main.setOnTouchListener { v, event ->
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.d("click search", "click Search")
                val intent = Intent(this, SearchActivity::class.java)
//                startActivityForResult(intent, R.string.NORMAL_RESULT)
                requestActivity.launch(intent)

//                startForResult.launch(Intent(this, SearchActivity::class.java))
            }
            true
        }

        val fdb : FirebaseFirestore = FirebaseFirestore.getInstance()
        fdb.collection("user")
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

            //?????? ??????
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_two -> {
                //?????????
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
                Toast.makeText(this@MainActivity, "??????", Toast.LENGTH_SHORT).show()
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

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            // 3.  ?????? ?????? ????????? ??? ??????
//            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
//            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            REQUIRED_PERMISSIONS.get(0)
                    )
            ) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(this@MainActivity, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG)
                    .show()
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(
                        this@MainActivity, REQUIRED_PERMISSIONS,
                        R.string.PERMISSIONS_REQUEST_CODE
                )
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(
                        this@MainActivity, REQUIRED_PERMISSIONS,
                        R.string.PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    //??????????????? GPS ???????????? ?????? ????????????
    private fun showDialogForLocationServiceSetting() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this@MainActivity)
        builder.setTitle("?????? ????????? ????????????")
        builder.setMessage(
                """
            ?????? ???????????? ???????????? ?????? ???????????? ???????????????.
            ?????? ????????? ???????????????????
            """.trimIndent()
        )
        builder.setCancelable(true)
        builder.setPositiveButton("??????", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivityForResult(callGPSSettingIntent, R.string.PERMISSIONS_REQUEST_CODE)
            requestActivity.launch(callGPSSettingIntent)

        })
        builder.setNegativeButton("??????",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        builder.create().show()
    }


    private fun getLatLng(): Location{
            var currentLatLng: Location? = null
            var hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
            var hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                    hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
                val locatioNProvider = LocationManager.GPS_PROVIDER
                currentLatLng = locationManager.getLastKnownLocation(locatioNProvider)
                if ( currentLatLng == null){
                    val locatioNProvider = LocationManager.NETWORK_PROVIDER
                    currentLatLng = locationManager.getLastKnownLocation(locatioNProvider)
                }
            }
            else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                REQUIRED_PERMISSIONS[0]
                        )){
                    Toast.makeText(this, "?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(
                            this,
                            REQUIRED_PERMISSIONS,
                            R.string.PERMISSIONS_REQUEST_CODE
                    )
                }else{
                    ActivityCompat.requestPermissions(
                            this,
                            REQUIRED_PERMISSIONS,
                            R.string.PERMISSIONS_REQUEST_CODE
                    )
                }
                currentLatLng = getLatLng()
            }
            return currentLatLng!!
        //null?
        }

    private var mResultCode = 0






    private val requestActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
            StartActivityForResult() // ??? StartActivityForResult ????????? ??????
    ) { activityResult ->
            // action to do something

            Log.d("code", activityResult.resultCode.toString())
            when (activityResult.resultCode){
                R.string.PERMISSIONS_REQUEST_CODE -> {
                    //???????????? GPS ?????? ???????????? ??????
                    if (checkLocationServicesStatus()) {
                        if (checkLocationServicesStatus()) {
                            Log.d("@@@", "onActivityResult : GPS ????????? ?????????")
                            checkRunTimePermission()
                        }
                    }
                }
                RESULT_OK -> {
                    val lcList =
                            activityResult.data?.getParcelableArrayListExtra<LocationSearch>("data")
                    Log.d("lcList", lcList?.size.toString())
                    if (lcList != null) {
                        val point = MapPOIItem()
                        point.apply {
                            itemName = lcList[0].name + "$$&$$" + lcList[0].address
                            mapPoint = MapPoint.mapPointWithGeoCoord(lcList[0].y, lcList[0].x)
                            markerType = MapPOIItem.MarkerType.BluePin
                            selectedMarkerType = MapPOIItem.MarkerType.RedPin
                        }
                        mapView.addPOIItem(point)
                        mapView.setMapCenterPoint(
                                MapPoint.mapPointWithGeoCoord(
                                        lcList[0].y,
                                        lcList[0].x
                                ), true
                        )

                    } else {

                    }
                }

                //
            }

        }

    // ????????? ????????? ?????????
    class CustomBalloonAdapter(inflater: LayoutInflater): CalloutBalloonAdapter {
        val mCalloutBalloon: View = inflater.inflate(R.layout.balloon_layout, null)
        val name: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_name)
        val address: TextView = mCalloutBalloon.findViewById(R.id.ball_tv_address)

        override fun getCalloutBalloon(poiItem: MapPOIItem?): View {
            val str = poiItem?.itemName
            val arr = str?.split("\$\$&\$\$")
            name.text = arr?.get(0).toString()
            address.text = arr?.get(1).toString()
            return mCalloutBalloon
        }

        override fun getPressedCalloutBalloon(poiItem: MapPOIItem?): View {
            return mCalloutBalloon
        }



    }



    // ?????? ?????? ????????? ?????????
    class MarkerEventListener(val context: Context): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // ?????? ?????? ???
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
            // ????????? ?????? ??? (Deprecated)
            // ??? ????????? ??????????????? ?????? ?????? ?????? ????????? ????????????
        }

        override fun onCalloutBalloonOfPOIItemTouched(
                mapView: MapView?,
                poiItem: MapPOIItem?,
                buttonType: MapPOIItem.CalloutBalloonButtonType?
        ) {
            // ????????? ?????? ???
            val bottomSheetDialog = BottomSheetDialog(mContext)
            bottomSheetDialog.setContentView(R.layout.bottom_menu)
            val likebtn = bottomSheetDialog.findViewById<LinearLayout>(R.id.uploadLinearLayout)
            val copybtn = bottomSheetDialog.findViewById<LinearLayout>(R.id.copyLinearLayout)
            val datebtn = bottomSheetDialog.findViewById<LinearLayout>(R.id.delete)

            likebtn?.setOnClickListener {
                bottomSheetDialog.dismiss()
                val mHandler = Handler(Looper.getMainLooper())
                mHandler.postDelayed({
                    val str = poiItem?.itemName
                    val arr = str?.split("\$\$&\$\$")

                    var lk = poiItem?.mapPoint?.mapPointCONGCoord?.x?.let { it1 -> poiItem?.mapPoint?.mapPointCONGCoord?.y?.let { it2 -> LikeLocation("", arr?.get(0).toString(), "", arr?.get(1).toString(), it1, it2) } }
                    val intent = Intent(mContext, LikePopup::class.java)
                    intent.putExtra("like", lk)
                    startActivity(mContext, intent, null)
                }, 0)
            }

            copybtn?.setOnClickListener {
                bottomSheetDialog.dismiss()
                val mHandler = Handler(Looper.getMainLooper())
                mHandler.postDelayed({
                    val intent = Intent(mContext, LikePopup::class.java)
                    startActivity(mContext, intent, null)
                }, 0)
            }
            datebtn?.setOnClickListener {
                bottomSheetDialog.dismiss()
                val mHandler = Handler(Looper.getMainLooper())
                mHandler.postDelayed({
                    val intent = Intent(mContext, LikePopup::class.java)
                    startActivity(mContext, intent, null)
                }, 0)
            }

            bottomSheetDialog.show()
//            val builder = AlertDialog.Builder(context)
//            val itemList = arrayOf("?????????", "?????? ??????", "??????")
//            builder.setTitle("${poiItem?.itemName}")
//            builder.setItems(itemList) { dialog, which ->
//                when(which) {
//                    0 -> Toast.makeText(context, "?????????", Toast.LENGTH_SHORT).show()  // ?????????
//                    1 -> mapView?.removePOIItem(poiItem)    // ?????? ??????
//                    2 -> dialog.dismiss()   // ???????????? ??????
//                }
//            }
//            builder.show()
        }

        override fun onDraggablePOIItemMoved(
                mapView: MapView?,
                poiItem: MapPOIItem?,
                mapPoint: MapPoint?
        ) {
            // ????????? ?????? ??? isDraggable = true ??? ??? ????????? ??????????????? ??????
        }
    }


    override fun onDestroy() {
            super.onDestroy()
        }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(authStateListener)
    }


}



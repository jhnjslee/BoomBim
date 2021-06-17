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
        const val API_KEY = "KakaoAK 5370b9816cfb27b331eefc35e6b66bf1"  // REST API 키
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
                    currentMode.text = " T O P " // 탑 10
                }
                1 -> {
                    currentMode.text = " 현재위치 "
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

                        var latitude = 0.0
                        var longitude = 0.0
                        var userLocation: Location = getLatLng()
                        if (userLocation != null) {
                            latitude = userLocation.latitude
                            longitude = userLocation.longitude
                            Log.d("CheckCurrentLocation", "현재 내 위치 값: ${latitude}, ${longitude}")

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

                        //txtCurrentPositionInfo.setText("위치정보 미수신중");
                        //lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    } catch (ex: SecurityException) {
                    }

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

                var latitude = 0.0
                var longitude = 0.0
                var userLocation: Location = getLatLng()
                if(userLocation != null){
                    latitude = userLocation.latitude
                    longitude = userLocation.longitude
                    Log.d("CheckCurrentLocation", "현재 내 위치 값: ${latitude}, ${longitude}")

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

                //txtCurrentPositionInfo.setText("위치정보 미수신중");
                //lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
            } catch (ex: SecurityException) {
            }

        }

        floatingBb.setOnClickListener {
            loadingDialog.show()
        }



        //검색 창 클릭 시
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

            //계정 클릭
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_two -> {
                //도움말
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
//            mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
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
                        R.string.PERMISSIONS_REQUEST_CODE
                )
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                        this@MainActivity, REQUIRED_PERMISSIONS,
                        R.string.PERMISSIONS_REQUEST_CODE
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
//            startActivityForResult(callGPSSettingIntent, R.string.PERMISSIONS_REQUEST_CODE)
            requestActivity.launch(callGPSSettingIntent)

        })
        builder.setNegativeButton("취소",
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
                    Toast.makeText(this, "앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
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
            StartActivityForResult() // ◀ StartActivityForResult 처리를 담당
    ) { activityResult ->
            // action to do something

            Log.d("code", activityResult.resultCode.toString())
            when (activityResult.resultCode){
                R.string.PERMISSIONS_REQUEST_CODE -> {
                    //사용자가 GPS 활성 시켰는지 검사
                    if (checkLocationServicesStatus()) {
                        if (checkLocationServicesStatus()) {
                            Log.d("@@@", "onActivityResult : GPS 활성화 되있음")
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

    // 커스텀 말풍선 클래스
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



    // 마커 클릭 이벤트 리스너
    class MarkerEventListener(val context: Context): MapView.POIItemEventListener {
        override fun onPOIItemSelected(mapView: MapView?, poiItem: MapPOIItem?) {
            // 마커 클릭 시
        }

        override fun onCalloutBalloonOfPOIItemTouched(mapView: MapView?, poiItem: MapPOIItem?) {
            // 말풍선 클릭 시 (Deprecated)
            // 이 함수도 작동하지만 그냥 아래 있는 함수에 작성하자
        }

        override fun onCalloutBalloonOfPOIItemTouched(
                mapView: MapView?,
                poiItem: MapPOIItem?,
                buttonType: MapPOIItem.CalloutBalloonButtonType?
        ) {
            // 말풍선 클릭 시
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
//            val itemList = arrayOf("토스트", "마커 삭제", "취소")
//            builder.setTitle("${poiItem?.itemName}")
//            builder.setItems(itemList) { dialog, which ->
//                when(which) {
//                    0 -> Toast.makeText(context, "토스트", Toast.LENGTH_SHORT).show()  // 토스트
//                    1 -> mapView?.removePOIItem(poiItem)    // 마커 삭제
//                    2 -> dialog.dismiss()   // 대화상자 닫기
//                }
//            }
//            builder.show()
        }

        override fun onDraggablePOIItemMoved(
                mapView: MapView?,
                poiItem: MapPOIItem?,
                mapPoint: MapPoint?
        ) {
            // 마커의 속성 중 isDraggable = true 일 때 마커를 이동시켰을 경우
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



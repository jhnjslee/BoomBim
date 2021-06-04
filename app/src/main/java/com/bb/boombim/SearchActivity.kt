package com.bb.boombim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bb.boombim.data.ListLayout
import com.bb.boombim.data.LocationSearch
import com.bb.boombim.data.ResultSearchKeyword
import com.bb.boombim.ui.login.LoginActivity
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.searchTitle_main
import kotlinx.android.synthetic.main.activity_search.*
import net.daum.mf.map.api.MapPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable

class SearchActivity() : AppCompatActivity(), Parcelable, Serializable{

    val RESULT_SEARCH_CODE = 200

    var imm : InputMethodManager? = null
    var currentSearchMode : Int = 0 // 0 = 키워드
    var doubleBackToExitPressedOnce = false
    var datas : ResultSearchKeyword? = null

    private val listItems = arrayListOf<ListLayout>()   // 리사이클러 뷰 아이템
    private val listAdapter = SearchListAdapter(listItems, this)    // 리사이클러 뷰 어댑터
    private var pageNumber = 1      // 검색 페이지 번호
    private var keyword = ""

    private var mResultCode = 0
    private var mData: Intent? = null

    constructor(parcel: Parcel) : this() {
        Log.d(" ", parcel.toString())
        currentSearchMode = parcel.readInt()
        doubleBackToExitPressedOnce = parcel.readByte() != 0.toByte()
        this@SearchActivity.mResultCode = parcel.readInt()
        this@SearchActivity.mData = parcel.readParcelable(Intent::class.java.classLoader)
        pageNumber = parcel.readInt()
        keyword = parcel.readString().toString()
    }

    companion object CREATOR : Parcelable.Creator<SearchActivity> {
        override fun createFromParcel(parcel: Parcel): SearchActivity {
            return SearchActivity(parcel)
        }

        override fun newArray(size: Int): Array<SearchActivity> {
            TODO("Not yet implemented")
        }
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5370b9816cfb27b331eefc35e6b66bf1"  // REST API 키

    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        Log.d("onCreate", "SearchActivity")
        init(applicationContext)
        initRecycler()
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // Handle the Intent
                //do stuff here
            }
        }
    }
    private fun initRecycler() {
        // 리사이클러 뷰
        // 구분선 넣기
        val dividerItemDecoration =
                DividerItemDecoration(result_recycler.context, LinearLayoutManager(this).orientation)
        val spaceDecoration = VerticalSpaceItemDecoration(10)
        result_recycler.addItemDecoration(spaceDecoration)
        result_recycler.addItemDecoration(dividerItemDecoration)
        result_recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        result_recycler.adapter = listAdapter
        // 리스트 아이템 클릭 시 해당 위치로 이동
        listAdapter.setItemClickListener(object : SearchListAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                val mapPoint = MapPoint.mapPointWithGeoCoord(listItems[position].y, listItems[position].x)
//                mapView.setMapCenterPointAndZoomLevel(mapPoint, 1, true)
            }
        })
    }

    private fun init(applicationContext: Context){
        val toolbar: Toolbar = findViewById(R.id.mainToolBar)
        this.setSupportActionBar(toolbar)
        imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //검색 창 엔터 enter
        searchTitle_main.setOnKeyListener { v, keyCode, event ->

            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                when (currentSearchMode) {
                    //키워드 검색
                    0 -> {
                        searchKeyword(searchTitle_main.text.toString())
                    }
                    //주소 검색
                    1 -> {
                        searchKeyword(searchTitle_main.text.toString())
                    }
                    //
                    2 -> {
                        searchKeyword(searchTitle_main.text.toString())
                    }
                }
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                searchTitle_main.text.clear()
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val menuItem: MenuItem = menu.findItem(R.id.menu_one)
        val view = MenuItemCompat.getActionView(menuItem)

        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_one -> {
                when (currentSearchMode) {
                    0 -> {
                        searchTitle_main.text.clear()
                        searchTitle_main.hint = "주소 검색"
                        currentSearchMode = 1
                    }
                    1 -> {
                        searchTitle_main.text.clear()
                        searchTitle_main.hint = "좌표 검색"
                        currentSearchMode = 2
                    }
                    2 -> {
                        searchTitle_main.text.clear()
                        searchTitle_main.hint = "키워드 검색"
                        currentSearchMode = 0
                    }
                }

            }
            R.id.menu_two -> {
                //도움말
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_three -> Toast.makeText(
                    this@SearchActivity,
                    "Menu Three Clicked",
                    Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    // 키워드 검색 함수
    private fun searchKeyword(keyword: String) {
        val retrofit = Retrofit.Builder()   // Retrofit 구성
            .baseUrl(SearchActivity.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java)   // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(SearchActivity.API_KEY, keyword)   // 검색 조건 입력
        var count = 0
        // API 서버에 요청
        call.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                    call: Call<ResultSearchKeyword>,
                    response: Response<ResultSearchKeyword>
            ) {

                addItems(response.body())
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Toasty.error(this@SearchActivity, "검색 실패", Toast.LENGTH_SHORT).show()

            }
        })
    }
    // 검색 결과 처리 함수
    private fun addItems(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
            // 검색 결과 있음
            listItems.clear() // 리스트 초기화
//           mapView.removeAllPOIItems() // 지도의 마커 모두 제거
            for (document in searchResult!!.documents) {
                // 결과를 리사이클러 뷰에 추가

                val item = ListLayout(document.place_name,
                        document.road_address_name,
                        document.address_name,
                        document.distance,
                        document.x.toDouble(),
                        document.y.toDouble())
                listItems.add(item)
            }
            listAdapter.notifyDataSetChanged()


        } else {
            // 검색 결과 없음
            Toasty.error(this, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true

        Toast.makeText(this, "두번 눌러 뒤로 가십시오", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("requestCode", requestCode.toString())
        when (requestCode) {
            RESULT_SEARCH_CODE -> {
            }
        }
    }

    fun clickLocation(item: ListLayout) {
        val intent : Intent  = Intent()
//        intent.flags = Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
//        val array = arrayOf(item.name, item.address, item.road, item.x, item.y)
        val lc = LocationSearch(item.name,item.address,item.road,item.x,item.y)
        val lcl : ArrayList<LocationSearch> = arrayListOf(lc)
        Log.d("item",item.address +" " + item.address)
        intent.putParcelableArrayListExtra("data",lcl)
        setResult(RESULT_OK, intent)
        finish()


    }


    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
            RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView,
                state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(currentSearchMode)
        parcel.writeByte(if (doubleBackToExitPressedOnce) 1 else 0)
        parcel.writeInt(mResultCode)
        parcel.writeParcelable(mData, flags)
        parcel.writeInt(pageNumber)
        parcel.writeString(keyword)
    }

    override fun describeContents(): Int {
        return 0
    }


}



class LinearLayoutManagerWrapper: LinearLayoutManager {
    constructor(context: Context) : super(context) {

    } constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {

    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {

    }
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}
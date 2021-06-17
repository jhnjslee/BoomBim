package com.bb.boombim

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import kotlinx.android.synthetic.main.activity_setting.*


class SettingActivity : AppCompatActivity() {

    lateinit var gridItem : GridView
    lateinit var gridAdapter: GridListAdapter

    companion object{
        private var mAuthListener: AuthStateListener? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(toolbarSetting)
        var loginStatus = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)  // 왼쪽 버튼 사용 여부 true

        supportActionBar!!.setHomeAsUpIndicator(R.drawable.abc_vector_test)  // 왼쪽 버튼 이미지 설정
        supportActionBar!!.setDisplayShowTitleEnabled(false) // 타이틀 안보이게 하기

        if (!loginStatus){
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingFrameTop, LoginYetFragment())
                .commit()
        }
        gridAdapter = GridListAdapter(this)
//      gridItemView.
        getDrawable(R.drawable.ic_download)?.let { gridAdapter.addItem("Top10", it) }
        getDrawable(R.drawable.ic_heart)?.let { gridAdapter.addItem("좋아요", it) }
        getDrawable(R.drawable.ic_date)?.let { gridAdapter.addItem("일정관리", it) }
        getDrawable(R.drawable.ic_location)?.let { gridAdapter.addItem("공지", it) }
        getDrawable(R.drawable.ic_map)?.let { gridAdapter.addItem("설정", it) }


        gridItem = findViewById(R.id.gridView)

        gridItem.setAdapter(gridAdapter)


        gridItem.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val item: String = gridAdapter.getItem(position) as String

            }


//        val myLayoutManager = GridLayoutManager(this, 2)
//        myRecyclerView.layoutManager = myLayoutManager




    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu)       // main_menu 메뉴를 toolbar 메뉴 버튼으로 설정
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 클릭된 메뉴 아이템의 아이디 마다 when 구절로 클릭시 동작을 설정한다.
        when(item!!.itemId){
            android.R.id.home -> { // 메뉴 버튼
                Log.d("1", "1")
                finish()
            }
            R.id.menu_search -> { // 검색 버튼
                Log.d("2", "2")
            }
            R.id.menu_account -> { // 계정 버튼
                Log.d("3", "3")
            }
            R.id.menu_logout -> { // 로그아웃 버튼
                Log.d("4", "4")
            }
        }
        return super.onOptionsItemSelected(item)

    }
}
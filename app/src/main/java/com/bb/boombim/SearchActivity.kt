package com.bb.boombim

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import com.bb.boombim.data.ResultSearchKeyword
import com.bb.boombim.ui.login.LoginActivity
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    var imm : InputMethodManager? = null
    var currentSearchMode : Int = 0 // 0 = 키워드

    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 5370b9816cfb27b331eefc35e6b66bf1"  // REST API 키
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)




    }

    private fun init(applicationContext: Context){
        val toolbar: Toolbar = findViewById(R.id.mainToolBar)
        this.setSupportActionBar(toolbar)
        imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //검색 창 엔터 enter
        searchTitle.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                when (currentSearchMode) {
                    //키워드 검색
                    0->{
                        searchKeyword(searchTitle.text.toString())
                    }
                    //주소 검색
                    1->{
                        searchKeyword(searchTitle.text.toString())
                    }
                    //
                    2->{
                        searchKeyword(searchTitle.text.toString())
                    }
                }



                imm?.hideSoftInputFromWindow(v.windowToken,0)
                searchTitle.text.clear()
                Toasty.error(this,"message", Toasty.LENGTH_SHORT).show()   // 엔터 눌렀을때 행동
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
                when (currentSearchMode){
                    0-> {
                        searchTitle.text.clear()
                        searchTitle.hint = "주소 검색"
                        currentSearchMode = 1
                    }
                    1-> {
                        searchTitle.text.clear()
                        searchTitle.hint = "좌표 검색"
                        currentSearchMode = 2
                    }
                    2-> {
                        searchTitle.text.clear()
                        searchTitle.hint = "키워드 검색"
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

        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
                // 통신 성공 (검색 결과는 response.body()에 담겨있음)
                Log.d("Test", "Raw: ${response.raw()}")
                Log.d("Test", "Body: ${response.body()}")
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                // 통신 실패
                Log.w("SearchActivity", "통신 실패: ${t.message}")
            }
        })
    }
}
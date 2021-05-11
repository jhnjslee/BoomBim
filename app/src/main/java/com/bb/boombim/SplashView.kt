package com.bb.boombim


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

import java.util.*


class SplashView : AppCompatActivity() {
    private val SPLASH_TIME_OUT:Long = 4000 // sec
    var currprog = 0 //멤버변수
    private var prog : ProgressBar? = null
    private var timerTask :TimerTask? = null
    private var timer : Timer? = null
    private var isTaskCompleted : Boolean? = null
    private val MY_PERMISSION_STORAGE = 1111

    companion object{
        const val REQUEST_ACCESS_FINE_LOCATION = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_view)
        Log.i("AppVersion","1.1.81")
//        prog = findViewById(R.id.progressBar)
        initProg()
        startTimerThread()
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSION_STORAGE -> {

                var i = 0
                while (i < grantResults.size) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
//                        Toast.makeText(this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT) .show()
                        return
                    }
                    i++
                }
            }
        }
    }

    fun initProg() {
        prog?.max = 100 // 최대값 지정
        prog?.setProgress(0) // 현재값 지정
    } // initprog


    fun startTimerThread() {
        var timerTask = object : TimerTask() {
            // timerTask는 timer가 일할 내용을 기록하는 객체
            override fun run() {
                // 이곳에 timer가 동작할 task를 작성
//                increaseBar() // timer가 동작할 내용을 갖는 함수 호출
                isTaskCompleted = true
            }
        }
        timer = Timer() // timer생성
        timer?.schedule(timerTask, 0, 35) // timerTask라는 일을 갖는 timer를 0초딜레이로 1000ms마다 실행
    } // startthread


//    fun increaseBar() {
//        runOnUiThread { // run을 해준다. 그러나 일반 thread처럼 .start()를 해줄 필요는 없다
//            currprog = prog?.getProgress()!!
//            val maxprog: Int = prog?.getMax()!!
//            if (currprog in 0 until maxprog) {
//                currprog += 1 // 프로그래스바 1씩 증가
//            } else if (currprog == maxprog) {
//                stopTimer()
//            }
//            prog?.progress = currprog
//        }
//    } // progress

    fun stopTimer() {
        if (timerTask != null) {
            timerTask?.cancel() // 타이머task를 timer 큐에서 지워버린다
            timerTask = null
        }
        if (timer != null) {
            timer?.cancel() // 스케쥴task과 타이머를 취소한다.
            timer?.purge() // task큐의 모든 task를 제거한다.
            timer = null
        }
    } // stoptimer




}

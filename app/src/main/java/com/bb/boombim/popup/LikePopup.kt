package com.bb.boombim.popup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bb.boombim.R
import com.bb.boombim.data.LikeLocation
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot


class LikePopup : AppCompatActivity() {

    lateinit var positiveButton : Button
    lateinit var message : TextView
    lateinit var negativeButton : Button



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_like_popup)

        message = findViewById(R.id.editTextLikeName)

        val fb : FirebaseFirestore = FirebaseFirestore.getInstance()
        var lk = LikeLocation("", "", "", "", 0.0, 0.0)
        val intent = intent


        val lottie = findViewById<View>(R.id.my_location) as LottieAnimationView
        lottie.playAnimation()
        lottie.loop(true)
        positiveButton = findViewById(R.id.positiveButton)
        message = findViewById(R.id.messageTextView)
        negativeButton = findViewById(R.id.negativeButton)
        positiveButton.setOnClickListener {
            val lkPosition = intent.getParcelableExtra<LikeLocation>("like")
            lkPosition?.settingName = message.text.toString()
            if (lkPosition != null) {
                fb.collection("TEST").document("location").set(lkPosition).addOnSuccessListener {
                    Log.d("fbsetSuccess","fbsetSuccess")
                }.addOnFailureListener {
                    Log.d("fbsetSuccess","fail")
                }
            }
//            okay()
        }
        negativeButton.setOnClickListener {
            fb.collection("TEST").get().addOnCompleteListener {
               Log.d("fbOnClick",it.toString())
            }
//            cancel()

        }
    }
    fun okay(){
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed({

        }, 1000)
        finish()
    }
    fun cancel(){
        finish()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_OUTSIDE){
            return false
        }
        return true
    }
}
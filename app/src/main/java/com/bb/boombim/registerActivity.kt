package com.bb.boombim

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val fb : FirebaseFirestore = FirebaseFirestore.getInstance()

        val lottie = findViewById<View>(R.id.register_lottie) as LottieAnimationView
        lottie.playAnimation()
        lottie.loop(true)

        val email = findViewById<EditText>(R.id.editTextEmail)

        val password = findViewById<EditText>(R.id.editTextPassword)
        val password2 = findViewById<EditText>(R.id.editTextPassword2)

        val name = findViewById<EditText>(R.id.editTextName)
        val register_btn = findViewById<ImageButton>(R.id.register_btn2)

        val errorText = findViewById<TextView>(R.id.errorText)


        register_btn.setOnClickListener {
            registerUser(email.text.toString(), password.text.toString())

        }

    }
    fun registerUser(email: String, password: String){
        MainActivity.mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "등록 성공", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@RegisterActivity, "등록 에러", Toast.LENGTH_SHORT).show()
                        return@OnCompleteListener
                    }
                })

    }
}
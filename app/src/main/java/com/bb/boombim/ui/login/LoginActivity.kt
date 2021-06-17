package com.bb.boombim.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.bb.boombim.MainActivity.Companion.mAuth
import com.bb.boombim.R
import com.bb.boombim.RegisterActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    companion object {

        private const val RC_SIGN_IN = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val lottie = findViewById<View>(R.id.login_lottie) as LottieAnimationView
        lottie.playAnimation()
        lottie.loop(true)

        val email = findViewById<EditText>(R.id.editTextEmail)
        val password = findViewById<EditText>(R.id.editTextPassword)

        val login_btn = findViewById<ImageButton>(R.id.login_btn)

        val register_btn = findViewById<Button>(R.id.register_btn)
        val btn_facebookSignIn = findViewById<ImageView>(R.id.btn_facebookSignIn)

        val btn_googleSignIn = findViewById<ImageView>(R.id.btn_googleSignIn)
//
        btn_googleSignIn.setOnClickListener {

        }
        btn_facebookSignIn.setOnClickListener {

        }

        login_btn.setOnClickListener {
            Log.d("test", "testLogin")
            val email: String = email.getText().toString().trim()
            val pwd: String = password.getText().toString().trim()

            mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this@LoginActivity,
                    OnCompleteListener<AuthResult?> { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "로긴 성공", Toast.LENGTH_SHORT)
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 오류", Toast.LENGTH_SHORT).show()
                        }
                    })
        }
        register_btn.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun buildActionCodeSettings() {
        // [START auth_build_action_code_settings]
        val actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://www.example.com/finishSignUp?cartId=1234"
            // This must be true
            handleCodeInApp = true
            setIOSBundleId("com.bb.bombim")
            setAndroidPackageName(
                "com.bb.bombim",
                true, /* installIfNotAvailable */
                "12" /* minimumVersion */
            )
        }
        sendSignInLink("jhnjslee@naver.com", actionCodeSettings)
        // [END auth_build_action_code_settings]
    }
    private fun sendSignInLink(email: String, actionCodeSettings: ActionCodeSettings) {
        // [START auth_send_sign_in_link]
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Email sent.")
                }
            }
        // [END auth_send_sign_in_link]
    }

    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_create_intent]
    }
    private fun signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
        // [END auth_fui_signout]
    }

    private fun delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
            .delete(this)
            .addOnCompleteListener {
                // ...
            }
        // [END auth_fui_delete]
    }

    private fun themeAndLogo() {
        val providers = emptyList<AuthUI.IdpConfig>()

        // [START auth_fui_theme_logo]
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_heart) // Set logo drawable
                .setTheme(R.style.AppTheme) // Set theme
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_theme_logo]
    }

    private fun privacyAndTerms() {
        val providers = emptyList<AuthUI.IdpConfig>()
        // [START auth_fui_pp_tos]
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                    "https://example.com/terms.html",
                    "https://example.com/privacy.html"
                )
                .build(),
            RC_SIGN_IN
        )
        // [END auth_fui_pp_tos]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
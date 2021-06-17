package com.bb.boombim

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bb.boombim.ui.login.LoginActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login_already.view.*
import kotlinx.android.synthetic.main.fragment_login_yet.view.*


class LoginYetFragment : Fragment() {
    // TODO: Rename and change types of parameters


    companion object {
        private const val RC_SIGN_IN = 123
        private var mAuthListener: AuthStateListener? = null
        lateinit var mContext: Context
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginYetFragment().apply {
                arguments = Bundle().apply {
                }
            }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val user = MainActivity.mAuth.currentUser
        if (user != null){
            Log.d("userUser", user.toString())
            val view : View = inflater.inflate(R.layout.fragment_login_already, container, false)
            // Inflate the layout for this fragment

            Log.d("onCreateView", "onCreateView")

            view.idText.text = user?.email
            view.logout_btn.setOnClickListener {
                //새로고침
                signOut()
                val ft : FragmentTransaction? = fragmentManager?.beginTransaction()
                ft?.detach(this)?.attach(this)?.commit()
//                getFragmentManager()?.let { it1 -> refreshFragment(this, it1) }

            }
            return view
        }else {
            Log.d("userUser", user.toString())
            val view : View = inflater.inflate(R.layout.fragment_login_yet, container, false)
            // Inflate the layout for this fragment

            Log.d("onCreateView", "onCreateView")

            view.nouserlayout.setOnClickListener {

//            createSignInIntent()
//            buildActionCodeSettings()
//            signOut()
//            delete()
//            themeAndLogo()
//            privacyAndTerms()
              val intent = Intent(activity, LoginActivity::class.java)
              startActivity(intent)
            }

            return view
        }
        //

    }


    private fun buildActionCodeSettings() {
        // [START auth_build_action_code_settings]

        Log.d("Login", "buildActionCodeSettings")
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
        Log.d("Login", "sendSignInLink")
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

        Log.d("Login", "createSignInIntent")
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
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

        Log.d("Login", "signOut")
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(mContext)
            .addOnCompleteListener {
                // ...
            }
        // [END auth_fui_signout]
    }

    private fun delete() {
        // [START auth_fui_delete]
        AuthUI.getInstance()
            .delete(mContext)
            .addOnCompleteListener {
                // ...
            }
        // [END auth_fui_delete]
    }

    private fun themeAndLogo() {
        Log.d("Login", "themeAndLogo")
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

        Log.d("Login", "privacyAndTerms")
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

        Log.d("Login", "onActivityResult ${requestCode}, ")
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            Log.d("Login", "RC_SIGN_IN ${response}, ")
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                Log.d("Login", "RESULT_OK ${user}")
                // ...
            } else {
                Log.d("Login", "${resultCode} ")

                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }
    fun refreshFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        var ft: FragmentTransaction = fragmentManager.beginTransaction()
        ft.detach(fragment).attach(fragment).commit()
    }


}
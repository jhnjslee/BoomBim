package com.bb.boombim

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bb.boombim.ui.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_login_yet.view.*


class LoginYetFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view : View = inflater.inflate(R.layout.fragment_login_yet, container, false)
        // Inflate the layout for this fragment
        view.emptyProfile.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                LoginYetFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}
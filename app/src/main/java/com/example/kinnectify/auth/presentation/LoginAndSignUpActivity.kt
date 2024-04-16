package com.example.kinnectify.auth.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kinnectify.R
import com.example.kinnectify.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginAndSignUpActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_and_sign_up)
        checkUserState()


    }


    private fun checkUserState(){
        if (auth.currentUser != null ){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
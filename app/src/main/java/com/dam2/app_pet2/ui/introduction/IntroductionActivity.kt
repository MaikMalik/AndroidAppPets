package com.dam2.app_pet2.ui.introduction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.ActivityIntroductionBinding
import com.dam2.app_pet2.ui.login.LoginActivity
import com.dam2.app_pet2.ui.main.MainActivity
import com.dam2.app_pet2.ui.singup.SignupActivity
import com.google.firebase.auth.FirebaseAuth

class IntroductionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroductionBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        goLogin()
        goSignUp()
    }

    fun goSignUp() {
        binding.btnSingUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    fun goLogin() {
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser!=null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
package com.dam2.app_pet2.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.ActivityLoginBinding
import com.dam2.app_pet2.ui.main.MainActivity
import com.dam2.app_pet2.ui.recoverypass.ForgotPasswordActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginUser()
        forgotPass()
    }

    fun forgotPass() {
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun loginUser() {
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val emailVerified = user?.isEmailVerified
                        binding.pbLoading.visibility = View.VISIBLE

                        if (emailVerified == true) {
                            // El correo electrónico ya ha sido verificado, el usuario puede ingresar a MainActivity
                            Handler().postDelayed({
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }, 2000) // 2000 milisegundos = 2 segundos

                        } else {
                            // El correo electrónico aún no ha sido verificado, mostrar la actividad de verificación
                            Toast.makeText(
                                this, "Email no verificado, revisa tu correo", Toast.LENGTH_SHORT).show()
                            // Ocultar ProgressBar en caso de error
                            binding.pbLoading.visibility = View.INVISIBLE
                        }
                    } else {
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidUserException) {
                            // El usuario no existe
                            Toast.makeText(
                                this, "Usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show()
                        } else {
                            // Otro error de inicio de sesión
                            Toast.makeText(
                                this, "Usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show()
                        }
                        // Ocultar ProgressBar en caso de error
                        binding.pbLoading.visibility = View.INVISIBLE
                    }
                }
            } else {
                Toast.makeText(this, "Campos vacíos no permitidos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
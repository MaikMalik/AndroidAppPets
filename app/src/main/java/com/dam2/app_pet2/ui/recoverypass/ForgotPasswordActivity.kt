package com.dam2.app_pet2.ui.recoverypass

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.ActivityForgotPasswordBinding
import com.dam2.app_pet2.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var binding: ActivityForgotPasswordBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sendEmailRecoverPassword()
    }

    fun sendEmailRecoverPassword() {
        auth = FirebaseAuth.getInstance()

        binding.btnRecoverPass.setOnClickListener {
            val email = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty()) { // Validar que el email no esté vacío
                // Verificar si existe una cuenta asociada con el correo electrónico
                auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                // No hay cuenta asociada con el correo electrónico proporcionado
                                Toast.makeText(this, "Email no encontrado", Toast.LENGTH_SHORT).show()
                            } else {
                                // Se encontró una cuenta asociada con el correo electrónico, enviar el correo de restablecimiento de contraseña
                                auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Por favor, revisa tu correo", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Error al enviar el correo de restablecimiento de contraseña: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            // Error al verificar el correo electrónico
                            Toast.makeText(this, "Error al verificar el correo electrónico: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
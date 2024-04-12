package com.dam2.app_pet2.ui.singup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import com.dam2.app_pet2.databinding.ActivitySignupBinding
import com.dam2.app_pet2.ui.login.LoginActivity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instanceFirebase()
    }


    fun instanceFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnCreateAccount.setOnClickListener {
            val realname = binding.etRealName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val passwd = binding.etPassword.text.toString().trim()
            val repasswd = binding.etRepeatPassword.text.toString().trim()

            if (realname.isNotEmpty() && email.isNotEmpty() && passwd.isNotEmpty() && repasswd.isNotEmpty()) {
                if (isNameValid(realname)) {
                    if (isEmailValid(email)) {
                        if (isPasswordValid(passwd)) {
                            if (passwd == repasswd) {
                                binding.pbLoading.visibility = View.VISIBLE
                                createAccount(email, passwd)
                            } else {
                                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "contraseña debe ser 6 caracteres alfanúmerica con caracter especial", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "El formato de correo electrónico es incorrecto", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "El nombre debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Los campos vacíos no están permitidos", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun createAccount(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val userId = user?.uid
                user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                    if (verificationTask.isSuccessful) {

                        binding.pbLoading.visibility = View.VISIBLE
                        // Llamar a la función saveUserData para guardar la información del usuario en Firestore
                        saveUserData(userId!!, binding.etRealName.text.toString(), email)
                        Handler().postDelayed({
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000) // 2000 milisegundos = 2 segundos

                    } else {
                        Toast.makeText(this, "No se pudo enviar el correo de verificación", Toast.LENGTH_SHORT).show()
                        binding.pbLoading.visibility = View.INVISIBLE // Ocultar ProgressBar en caso de error
                    }
                }
            } else {
                Toast.makeText(this, "Usuario ya existe".toString(), Toast.LENGTH_SHORT).show()
                binding.pbLoading.visibility = View.INVISIBLE // Ocultar ProgressBar en caso de error
            }
        }
    }

    fun saveUserData(userId: String, realName: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        val userData = hashMapOf(
            "realName" to realName,
            "email" to email
            // Agrega otros campos de información que desees guardar
        )
        userRef.set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Información de usuario guardada", Toast.LENGTH_SHORT).show()
                // Continúa con las siguientes acciones después de guardar los datos del usuario en Firestore
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar la información del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                // Maneja cualquier error de guardado de datos aquí
            }
    }


    //para comprobar campos
    fun isNameValid(name: String): Boolean {
        return name.length >= 4
    }

    fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    fun isPasswordValid(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*()-=_+\\[\\]{};':\"\\\\|,.<>/?]{6,}$"
        return password.matches(passwordPattern.toRegex())
    }

}
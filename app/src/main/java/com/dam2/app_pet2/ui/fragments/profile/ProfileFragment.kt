package com.dam2.app_pet2.ui.fragments.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.FragmentPetsBinding
import com.dam2.app_pet2.databinding.FragmentProfileBinding
import com.dam2.app_pet2.network.models.Pet
import com.dam2.app_pet2.ui.introduction.IntroductionActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recuperaDatosdeUsuario()
        actionsUser()
    }

    private fun recuperaDatosdeUsuario() {

        if (currentUser != null) {
            val userRef = db.collection("users").document(currentUser.uid)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            // Actualiza la interfaz de usuario con los datos del usuario
                            updateUI(user)
                        }
                    } else {
                        // Manejo de la situación cuando el documento del usuario no existe
                        Toast.makeText(
                            requireContext(),
                            "El documento del usuario no existe",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejo de errores al obtener el documento del usuario
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener el documento del usuario: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateUI(user: User) {
        binding.profileNombre.setText(user.realName)
        binding.profileEmail.setText(user.email)

    }

    fun actionsUser(){

        // Configura el botón de cerrar sesión
        val btnCerrarSesion = binding.btnCerrarSesion
        btnCerrarSesion.setOnClickListener {
            // Cerrar sesión de Firebase Auth
            auth.signOut()

            // Redirigir al usuario a la clase IntroduccionActivity
            val intent = Intent(requireContext(), IntroductionActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Opcional: finaliza la actividad actual para evitar que el usuario vuelva atrás
        }

        // Configura el botón de borrar cuenta
        val btnBorrarCuenta = binding.btnBorrarCuenta
        btnBorrarCuenta.setOnClickListener {
            // Muestra un diálogo de confirmación antes de borrar la cuenta
            AlertDialog.Builder(requireContext())
                .setTitle("Borrar cuenta")
                .setMessage("¿Estás seguro de que deseas borrar tu cuenta?")
                .setPositiveButton("Sí") { _, _ ->
                    // Elimina la cuenta de Firebase Auth
                    val currentUser = auth.currentUser
                    currentUser?.delete()
                        ?.addOnSuccessListener {
                            // Elimina los datos del usuario de Firestore
                            firestore.collection("users").document(currentUser.uid)
                                .delete()
                                .addOnSuccessListener {
                                    // Redirige al usuario a la clase IntroduccionActivity después de borrar la cuenta
                                    val intent = Intent(requireContext(), IntroductionActivity::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                                .addOnFailureListener { exception ->
                                    // Manejo de errores al borrar los datos del usuario de Firestore
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al borrar los datos del usuario: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        ?.addOnFailureListener { exception ->
                            // Manejo de errores al borrar la cuenta de Firebase Auth
                            Toast.makeText(
                                requireContext(),
                                "Error al borrar la cuenta: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

}
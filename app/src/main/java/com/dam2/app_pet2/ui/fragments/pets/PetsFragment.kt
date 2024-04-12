package com.dam2.app_pet2.ui.fragments.pets

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.FragmentAddPetBinding
import com.dam2.app_pet2.databinding.FragmentPetsBinding
import com.dam2.app_pet2.network.adapter.ArticleAdapter
import com.dam2.app_pet2.network.adapter.PetAdapter
import com.dam2.app_pet2.network.listaArticulos
import com.dam2.app_pet2.network.models.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class PetsFragment : Fragment(), PetAdapter.OnButtonClickListener {

    private var _binding: FragmentPetsBinding? = null
    private val binding get() = _binding!!
    private lateinit var petList: ArrayList<Pet>
    private var db = Firebase.firestore

    var petAdapter: PetAdapter? = null
    private lateinit var petRefs: ArrayList<DocumentReference> // Almacena las referencias a los documentos de las mascotas
    private var isRefreshing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView()
        binding.refreshSwipeLayout.setOnRefreshListener {
            if (!isRefreshing) {
                isRefreshing = true
                recyclerView()
            } else {
                binding.refreshSwipeLayout.isRefreshing = false
            }
        }
    }

    fun recyclerView() {

        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        if (usuarioId != null) {
            binding.myRecycler2.layoutManager = LinearLayoutManager(requireContext())
            petList = arrayListOf()
            petRefs = arrayListOf() // Inicializa la lista de referencias a los documentos

            db.collection("users").document(usuarioId).collection("mascotas").get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val pet: Pet? = document.toObject(Pet::class.java)
                        if (pet != null) {
                            petList.add(pet)
                            petRefs.add(document.reference) // Agrega la referencia del documento a la lista
                        }
                    }
                    petAdapter = PetAdapter(petList, this)
                    binding.myRecycler2.adapter = petAdapter

                    // Restablecer el estado de isRefreshing y detener la animación de actualización
                    isRefreshing = false
                    binding.refreshSwipeLayout.isRefreshing = false
                }.addOnFailureListener { exception ->
                    // Manejo de errores al obtener la colección de mascotas
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener las mascotas: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Manejo de la situación cuando el usuarioId es nulo
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshRecyclerView() {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        if (usuarioId != null) {
            petList.clear()
            petRefs.clear()

            db.collection("users").document(usuarioId).collection("mascotas").get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val pet: Pet? = document.toObject(Pet::class.java)
                        if (pet != null) {
                            petList.add(pet)
                            petRefs.add(document.reference)
                        }
                    }
                    petAdapter?.notifyDataSetChanged() // Notificar al adaptador sobre los cambios en la lista de mascotas
                }.addOnFailureListener { exception ->
                    // Manejo de errores al obtener la colección de mascotas
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener las mascotas: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Manejo de la situación cuando el usuarioId es nulo
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onButtonExpandClickListener(pet: Pet, position: Int) {
        val previousExpandedPosition = petAdapter?.expandedPosition ?: RecyclerView.NO_POSITION
        val isExpanded = position != previousExpandedPosition

        petAdapter?.expandedPosition = if (isExpanded) position else RecyclerView.NO_POSITION
        petAdapter?.notifyItemChanged(previousExpandedPosition)
        petAdapter?.notifyItemChanged(position)
    }


    override fun onButtonBorrarMascotaClickListener(pet: Pet, position: Int) {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        if (usuarioId != null) {
            val mascotaRef =
                petRefs[position] // Obtiene la referencia del documento según la posición en la lista

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Confirmación")
            alertDialogBuilder.setMessage("¿Estás seguro de que quieres eliminar esta mascota?")
            alertDialogBuilder.setPositiveButton("Sí") { dialog, _ ->
                mascotaRef.delete().addOnSuccessListener {
                    // La mascota se ha eliminado correctamente de la base de datos
                    // Puedes realizar acciones adicionales si es necesario
                    Toast.makeText(requireContext(), "Mascota eliminada", Toast.LENGTH_SHORT).show()
                    // También puedes actualizar la lista localmente
                    petList.removeAt(position)
                    petAdapter?.notifyItemRemoved(position)
                }.addOnFailureListener { exception ->
                    // Manejo de errores al eliminar la mascota
                    Toast.makeText(
                        requireContext(),
                        "Error al eliminar la mascota: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                // El usuario ha cancelado la eliminación de la mascota
                dialog.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        } else {
            // Manejo de la situación cuando el usuarioId es nulo
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onButtonEditarMascotaClickListener(pet: Pet, position: Int) {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid
        if (usuarioId != null) {
            val mascotaRef = petRefs[position]
            mascotaRef.get().addOnSuccessListener { documentSnapshot ->
                val petId = documentSnapshot.id
                val intent = Intent(requireContext(), UpdatePetActivity::class.java)
                intent.putExtra("petId", petId)
                startActivity(intent)
            }.addOnFailureListener { exception ->
                // Manejo de errores al obtener el documento de la mascota
                Toast.makeText(
                    requireContext(),
                    "Error al obtener el documento de la mascota: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Manejo de la situación cuando el usuarioId es nulo
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView()
    }

}
package com.dam2.app_pet2.ui.fragments.addpet

import android.app.Activity.RESULT_OK
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.FragmentAddPetBinding
import com.dam2.app_pet2.databinding.FragmentNewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.UUID


class AddPetFragment : Fragment() {

    private var _binding: FragmentAddPetBinding?=null
    private val binding get() = _binding!!

    var sImage:String?=""
    // Variable para almacenar la URI de la imagen seleccionada
    private var imageUriSeleccionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddPetBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etDatepet.setOnClickListener { showDatePickerDialog() }
        listItemsGenre()
        btnUpload()
        btnSave()
        btnCancelar()
    }

    fun btnCancelar(){
        binding.btnCancelar.setOnClickListener{
            binding.petPhoto.setImageResource(R.drawable.ic_addpet2)
            binding.etEspecie.text?.clear()
            binding.etPetName.text?.clear()
            binding.etDatepet.text?.clear()
            binding.etRaza.text?.clear()
            binding.etSobretumascota.text?.clear()
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            try{
                val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
                val bytes = stream.toByteArray()
                sImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                binding.petPhoto.setImageBitmap(myBitmap)
                inputStream?.close()
                imageUriSeleccionada = imageUri
                Toast.makeText(requireContext(),"Imagen Seleccionada", Toast.LENGTH_SHORT).show()
            }catch (ex: Exception){
                Toast.makeText(requireContext(),ex.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun btnSave() {
        binding.btnGuardar.setOnClickListener {
            val especie = binding.etEspecie.text.toString().trim()
            val nombre = binding.etPetName.text.toString().trim()
            val sexo = binding.tvautoComplete.text.toString().trim()

            // Verificar si los campos obligatorios están vacíos
            if (especie.isNotEmpty() && nombre.isNotEmpty() && sexo.isNotEmpty()) {
                saveInfoPets(imageUriSeleccionada)
            } else {
                Toast.makeText(requireContext(), "Por favor, completa los campos obligatorios: tipo, nombre y sexo", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun btnUpload(){
        binding.btnPhoto.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }
    }

    fun saveInfoPets(imageUri: Uri?) {
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid

        val especie = binding.etEspecie.text.toString()
        val nombre = binding.etPetName.text.toString()
        val fecha = binding.etDatepet.text.toString()
        val sexo = binding.tvautoComplete.text.toString()
        val raza = binding.etRaza.text.toString()
        val descripcion = binding.etSobretumascota.text.toString()

        // Verificar si el ID del usuario está disponible
        if (usuarioId != null) {
            // Subir la imagen a Firebase Storage
            if (imageUri != null) {
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/$usuarioId/${UUID.randomUUID()}")

                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        // La imagen se cargó correctamente
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Obtener la URL de descarga de la imagen
                            val imageUrl = downloadUri.toString()

                            // Guardar los datos de la mascota junto con la URL de la imagen
                            val coleccionMascotas =
                                FirebaseFirestore.getInstance().collection("users").document(usuarioId)
                                    .collection("mascotas")
                            val nuevaMascota = coleccionMascotas.document()

                            val mascota = Pets(
                                tipo = especie,
                                nombre = nombre,
                                fecha = fecha,
                                sexo = sexo,
                                raza = raza,
                                descripcion = descripcion,
                                imageUrl = imageUrl
                            )

                            nuevaMascota.set(mascota)
                                .addOnSuccessListener {
                                    binding.etEspecie.text?.clear()
                                    binding.etPetName.text?.clear()
                                    binding.etDatepet.text?.clear()
                                    binding.etRaza.text?.clear()
                                    binding.etSobretumascota.text?.clear()
                                    binding.petPhoto.setImageResource(R.drawable.ic_addpet2)

                                    Toast.makeText(requireContext(), "Mascota guardada correctamente", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Error al guardar la mascota: ${error.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { error ->
                        // Error al cargar la imagen en Firebase Storage
                        Toast.makeText(requireContext(), "Error al subir la imagen: ${error.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                // No se seleccionó ninguna imagen, guardar los datos de la mascota sin imagen
                val coleccionMascotas =
                    FirebaseFirestore.getInstance().collection("users").document(usuarioId)
                        .collection("mascotas")
                val nuevaMascota = coleccionMascotas.document()

                val defaultImageUrl = "https://cdn.pixabay.com/photo/2017/03/25/14/26/animals-2173635_1280.jpg"
                val mascota = Pets(
                    tipo = especie,
                    nombre = nombre,
                    fecha = fecha,
                    sexo = sexo,
                    raza = raza,
                    descripcion = descripcion,
                    imageUrl = defaultImageUrl
                )

                nuevaMascota.set(mascota)
                    .addOnSuccessListener {
                        binding.etEspecie.text?.clear()
                        binding.etPetName.text?.clear()
                        binding.etDatepet.text?.clear()
                        binding.etRaza.text?.clear()
                        binding.etSobretumascota.text?.clear()

                        Toast.makeText(requireContext(), "Mascota guardada correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            requireContext(),
                            "Error al guardar la mascota: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            // El ID del usuario no está disponible
            Toast.makeText(requireContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment{day, month, year -> onDateSelected(day,month,year)}
        datePicker.show(childFragmentManager, "datepicker")
    }

    fun onDateSelected(day:Int, month:Int, year:Int){
        binding.etDatepet.setText("$day-$month-$year")
    }

    fun listItemsGenre(){
        val items = listOf("Macho","Hembra")
        val adapter = ArrayAdapter(requireContext(),R.layout.list_item_genre_pet, items)
        binding.tvautoComplete.clearFocus()
        binding.tvautoComplete.setAdapter(adapter)
    }

    override fun onResume() {
        super.onResume()
        listItemsGenre()
    }


}
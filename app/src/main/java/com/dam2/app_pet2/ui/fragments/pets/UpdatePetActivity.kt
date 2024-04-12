package com.dam2.app_pet2.ui.fragments.pets

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.ActivityUpdatePetBinding
import com.dam2.app_pet2.network.models.Pet
import com.dam2.app_pet2.ui.fragments.addpet.DatePickerFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.UUID

class UpdatePetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatePetBinding

    private var db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var petId: String? = null
    // Variable para almacenar la URI de la imagen seleccionada
    private var imageUriSeleccionada: Uri? = null
    var sImage:String?=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        petId = intent.getStringExtra("petId")
        if (petId != null && currentUser != null) {
            recuperaDatos()
        }

        binding.updateEtDatepet.setOnClickListener { showDatePickerDialog() }
        listItemsGenre()
        btnSave()
        btnCancel()
        updatePhotoPet()
    }

    private fun recuperaDatos() {
        if (petId != null && currentUser != null) {
            // Obtén el documento de la mascota desde Firebase Firestore
            val petRef = db.collection("users")
                .document(currentUser.uid)
                .collection("mascotas")
                .document(petId!!)

            petRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val pet = document.toObject(Pet::class.java)
                        if (pet != null) {
                            // Actualiza la interfaz de usuario con los datos de la mascota
                            updateUI(pet)
                        }
                    } else {
                        // Manejo de la situación cuando el documento de la mascota no existe
                        Toast.makeText(this, "El documento de la mascota no existe", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejo de errores al obtener el documento de la mascota
                    Toast.makeText(
                        this,
                        "Error al obtener el documento de la mascota: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun updateUI(pet: Pet) {
        // Actualiza la interfaz de usuario con los datos de la mascota
        Picasso.get().load(pet.imageUrl).into(binding.updatePetPhoto)
        binding.updateEtEspecie.setText(pet.tipo)
        binding.updateEtPetName.setText(pet.nombre)
        binding.updateEtDatepet.setText(pet.fecha)
        binding.updateTvautoComplete.setText(pet.sexo)
        binding.updateEtRaza.setText(pet.raza)
        binding.updateEtSobretumascota.setText(pet.descripcion)

        // Actualiza la variable sImage con la URL de la imagen actual
        val imageUrl = pet.imageUrl
        sImage = imageUrl

        // Actualiza el texto del drop-down menu y deshabilita la entrada libre
        val genreAdapter = ArrayAdapter(this, R.layout.list_item_genre_pet, listOf("Macho", "Hembra"))
        binding.updateTvautoComplete.setAdapter(genreAdapter)
        binding.updateTvautoComplete.setText(pet.sexo, false)
        binding.updateTvautoComplete.keyListener = null

    }



    private fun updatePhotoPet() {
        binding.updateBtnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            pickImageLauncher.launch(intent)
        }
    }

    private fun btnSave() {
        binding.updateBtnUpdate.setOnClickListener {
            guardarCambios(imageUriSeleccionada)
        }
    }

    private fun btnCancel(){
        binding.updateBtnCancelar.setOnClickListener {
            onBackPressed()
        }
    }

    private fun obtenerDatosActualizados(): Pet {
        val tipo = binding.updateEtEspecie.text.toString().trim()
        val nombre = binding.updateEtPetName.text.toString().trim()
        val fecha = binding.updateEtDatepet.text.toString().trim()
        val sexo = binding.updateTvautoComplete.text.toString().trim()
        val raza = binding.updateEtRaza.text.toString().trim()
        val descripcion = binding.updateEtSobretumascota.text.toString().trim()

        // Crea un nuevo objeto Pet con los datos actualizados
        return Pet(nombre, tipo, raza, fecha, sexo, descripcion, sImage)
    }


    private fun guardarCambios(imageUri: Uri?) {
        val updatedPet = obtenerDatosActualizados() // Pasa la URL de la imagen actual

        if (petId != null && currentUser != null) {
            val petRef = db.collection("users").document(currentUser.uid).collection("mascotas").document(petId!!)

            // Verifica si se seleccionó una nueva imagen
            if (imageUri != null) {
                // Subir la nueva imagen a Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("images/${currentUser.uid}/${UUID.randomUUID()}")

                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        // La imagen se cargó correctamente
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            // Obtener la URL de descarga de la imagen
                            val imageUrl = downloadUri.toString()

                            // Actualizar los datos de la mascota, incluyendo la nueva URL de la imagen
                            updatedPet.imageUrl = imageUrl

                            petRef.set(updatedPet)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Datos de la mascota actualizados", Toast.LENGTH_SHORT).show()
                                    onBackPressed()
                                }
                                .addOnFailureListener { exception ->
                                    // Manejo de errores al actualizar los datos de la mascota
                                    Toast.makeText(
                                        this,
                                        "Error al actualizar los datos de la mascota: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Manejo de errores al cargar la nueva imagen en Firebase Storage
                        Toast.makeText(
                            this,
                            "Error al subir la nueva imagen: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                // No se seleccionó una nueva imagen, simplemente actualiza los datos de la mascota sin modificar la URL de la imagen
                petRef.set(updatedPet)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos de la mascota actualizados", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    .addOnFailureListener { exception ->
                        // Manejo de errores al actualizar los datos de la mascota
                        Toast.makeText(
                            this,
                            "Error al actualizar los datos de la mascota: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            try{
                val inputStream = this.contentResolver.openInputStream(imageUri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG,100,stream)
                val bytes = stream.toByteArray()
                sImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                binding.updatePetPhoto.setImageBitmap(myBitmap)
                inputStream?.close()
                imageUriSeleccionada = imageUri
                Toast.makeText(this,"Imagen Seleccionada", Toast.LENGTH_SHORT).show()
            }catch (ex: Exception){
                Toast.makeText(this,ex.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun listItemsGenre(){
        val items = listOf("Macho","Hembra")
        val adapter = ArrayAdapter(this,R.layout.list_item_genre_pet, items)
        binding.updateTvautoComplete.clearFocus()
        binding.updateTvautoComplete.setAdapter(adapter)
    }

    private fun showDatePickerDialog(){
        val datePicker = DatePickerFragment{day, month, year -> onDateSelected(day,month,year)}
        datePicker.show(supportFragmentManager, "datepicker")
    }
    fun onDateSelected(day:Int, month:Int, year:Int){
        binding.updateEtDatepet.setText("$day-$month-$year")
    }

}

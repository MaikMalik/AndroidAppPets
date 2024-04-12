package com.dam2.app_pet2.network.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dam2.app_pet2.R
import com.dam2.app_pet2.databinding.LayoutViewPetSingleBinding

import com.dam2.app_pet2.network.models.Pet
import com.squareup.picasso.Picasso


class PetAdapter(private val petList: List<Pet>, private val buttonClick: OnButtonClickListener) :
    RecyclerView.Adapter<PetAdapter.MyViewHolder>() {

    var expandedPosition = RecyclerView.NO_POSITION

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = LayoutViewPetSingleBinding.bind(itemView)

        fun bind(pet: Pet, onButtonClickListener: OnButtonClickListener, isExpanded: Boolean) {
            with(binding) {
                if (!pet.imageUrl.isNullOrEmpty()) {
                    Picasso.get().load(pet.imageUrl).into(photo)
                } else {
                    // Si la URL de la imagen está vacía, puedes mostrar una imagen de placeholder o tomar alguna otra acción adecuada
                    Picasso.get().load(R.drawable.ic_addpet2).into(photo)
                }

                viewNamepet.text = pet.nombre
                viewTipopet.text = pet.tipo
                sexoPet.text = pet.sexo

                // Expandible
                expandedLayoutRecycler.visibility = if (isExpanded) View.VISIBLE else View.GONE
                viewRazapet.text = pet.raza
                viewAgepet.text = pet.fecha
                viewDescripcion.text = pet.descripcion

                cardViewLayout.setOnClickListener {
                    onButtonClickListener.onButtonExpandClickListener(pet, adapterPosition)
                }

                // Botones
                btnEditar.setOnClickListener {
                    onButtonClickListener.onButtonEditarMascotaClickListener(pet, adapterPosition)
                }
                btnEliminar.setOnClickListener {
                    onButtonClickListener.onButtonBorrarMascotaClickListener(pet, adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_view_pet_single, parent, false)
        )

    override fun getItemCount(): Int = petList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pet = petList[position]
        val isExpanded = position == expandedPosition

        // Configurar la visibilidad de los campos adicionales según si está expandido o no
        holder.bind(pet, buttonClick, isExpanded)
        holder.itemView.isActivated = isExpanded

        holder.itemView.setOnClickListener {
            expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else position
            notifyItemChanged(position)
        }
    }

    interface OnButtonClickListener {
        fun onButtonEditarMascotaClickListener(pet: Pet, position: Int)
        fun onButtonBorrarMascotaClickListener(pet: Pet, position: Int)
        fun onButtonExpandClickListener(pet: Pet, position: Int)
    }
}




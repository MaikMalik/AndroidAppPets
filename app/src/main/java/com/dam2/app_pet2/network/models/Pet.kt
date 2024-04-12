package com.dam2.app_pet2.network.models

data class Pet(
    val nombre: String?=null,
    val tipo: String?=null,
    val raza: String?=null,
    val fecha: String?=null,
    val sexo: String?=null,
    val descripcion: String?=null,
    var imageUrl: String?=null,
)

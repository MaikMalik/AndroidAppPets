package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("location" ) var location : Location? = Location(),
    @SerializedName("viewport" ) var viewport : Viewport? = Viewport()
)
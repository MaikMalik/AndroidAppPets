package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName

data class Southwest(
    @SerializedName("lat" ) var lat : Double? = null,
    @SerializedName("lng" ) var lng : Double? = null
)
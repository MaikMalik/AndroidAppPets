package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName

data class Viewport(
    @SerializedName("northeast" ) var northeast : Northeast? = Northeast(),
    @SerializedName("southwest" ) var southwest : Southwest? = Southwest()
)
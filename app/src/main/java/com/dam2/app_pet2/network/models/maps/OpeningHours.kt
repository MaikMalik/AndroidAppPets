package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName

data class OpeningHours(
    @SerializedName("open_now" ) var openNow : Boolean? = null
)
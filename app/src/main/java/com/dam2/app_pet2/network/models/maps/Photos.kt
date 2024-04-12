package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName


data class Photos (

    @SerializedName("height"            ) var height           : Int?              = null,
    @SerializedName("html_attributions" ) var htmlAttributions : ArrayList<String> = arrayListOf(),
    @SerializedName("photo_reference"   ) var photoReference   : String?           = null,
    @SerializedName("width"             ) var width            : Int?              = null

)
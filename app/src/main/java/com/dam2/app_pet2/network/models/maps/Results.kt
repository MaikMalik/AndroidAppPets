package com.dam2.app_pet2.network.models.maps

import com.google.gson.annotations.SerializedName

data class Results(
    @SerializedName("business_status"       ) var businessStatus      : String?           = null,
    @SerializedName("geometry"              ) var geometry            : Geometry?         = Geometry(),
    @SerializedName("icon"                  ) var icon                : String?           = null,
    @SerializedName("icon_background_color" ) var iconBackgroundColor : String?           = null,
    @SerializedName("icon_mask_base_uri"    ) var iconMaskBaseUri     : String?           = null,
    @SerializedName("name"                  ) var name                : String?           = null,
    @SerializedName("opening_hours"         ) var openingHours        : OpeningHours?     = OpeningHours(),
    @SerializedName("photos"                ) var photos              : ArrayList<Photos> = arrayListOf(),
    @SerializedName("place_id"              ) var placeId             : String?           = null,
    @SerializedName("plus_code"             ) var plusCode            : PlusCode?         = PlusCode(),
    @SerializedName("price_level"           ) var priceLevel          : Int?              = null,
    @SerializedName("rating"                ) var rating              : Double?           = null,
    @SerializedName("reference"             ) var reference           : String?           = null,
    @SerializedName("scope"                 ) var scope               : String?           = null,
    @SerializedName("types"                 ) var types               : ArrayList<String> = arrayListOf(),
    @SerializedName("user_ratings_total"    ) var userRatingsTotal    : Int?              = null,
    @SerializedName("vicinity"              ) var vicinity            : String?           = null
)
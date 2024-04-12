package com.dam2.app_pet2.network.remote

object Common {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"
    val googleApiService: IGoogleAPIService
        get()=RefrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}
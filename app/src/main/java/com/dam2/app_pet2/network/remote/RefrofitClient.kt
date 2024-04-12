package com.dam2.app_pet2.network.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RefrofitClient {
    private var retrofit: Retrofit?=null

    fun getClient(baseUrl:String): Retrofit{
        if (retrofit==null){
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
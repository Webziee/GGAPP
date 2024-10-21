package com.example.goergesgraceapp

import Bookings
import retrofit2.Call
import retrofit2.http.*

interface SupabaseApi {

        @GET("Bookings")
        fun getBookings(
            @Header("apikey") apiKey: String,
            @Header("Authorization") authToken: String,
            @Header("Content-Type") contentType: String = "application/json"
        ): Call<List<Bookings>>

    @POST("Bookings")
    fun addBookings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body newBooking: Bookings
    ): Call<Bookings>
}

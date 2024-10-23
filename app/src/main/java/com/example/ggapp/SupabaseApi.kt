package com.example.goergesgraceapp

import BookedRequest
import BookedResponse
import Bookings
import retrofit2.Call
import retrofit2.http.*

interface SupabaseApi {

    // Method to get bookings from the "Bookings" table
    @GET("Bookings")
    fun getBookings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<List<Bookings>>

    // Method to add a new booking to the "Bookings" table
    @POST("Bookings")
    fun addBookings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body newBooking: Bookings
    ): Call<Bookings>

    // Method to get unavailable dates from the "Booked" table with proper operator formatting
    @GET("Booked")
    fun getBookedDates(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String,
        @Query("unit_number") unitNumber: String // dynamically pass the unit number
    ): Call<List<BookedResponse>>


    // Method to insert a new booked entry into the "Booked" table
    @POST("Booked")
    fun addBookedDates(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body newBooked: BookedRequest
    ): Call<Void>

    @GET("Booked")
    fun getPaidBookings(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authToken: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<List<BookedResponse>>


}

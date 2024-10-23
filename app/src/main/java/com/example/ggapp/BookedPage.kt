package com.example.ggapp

import BookedResponse
import Bookings
import BookingsCardAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BookedPage : Fragment() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookedCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booked_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView inside onViewCreated, after the view has been created
        bookingsRecyclerView = view.findViewById(R.id.BookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list
        bookingsAdapter = BookedCardAdapter(emptyList()) { booking ->
            // Handle booking click
            Log.d("BookedPage", "Clicked booking: ${booking.unit_number}")
        }
        bookingsRecyclerView.adapter = bookingsAdapter

        // Fetch paid bookings from the Booked table in Supabase
        fetchPaidBookings()
    }

    private fun fetchPaidBookings() {
        SupabaseClient.api.getPaidBookings(
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}"
        ).enqueue(object : Callback<List<BookedResponse>> {
            override fun onResponse(call: Call<List<BookedResponse>>, response: Response<List<BookedResponse>>) {
                if (response.isSuccessful) {
                    val paidBookingsList = response.body() ?: emptyList()

                    // Fetch the bookings data to get images
                    fetchImagesForBookings(paidBookingsList)
                } else {
                    Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<BookedResponse>>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
            }
        })
    }

    private fun fetchImagesForBookings(bookedList: List<BookedResponse>) {
        SupabaseClient.api.getBookings(
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}"
        ).enqueue(object : Callback<List<Bookings>> {
            override fun onResponse(call: Call<List<Bookings>>, response: Response<List<Bookings>>) {
                if (response.isSuccessful) {
                    val bookingsList = response.body() ?: emptyList()

                    // Map unit number to corresponding booking image
                    val updatedBookedList = bookedList.map { booking ->
                        val matchingBooking = bookingsList.find { it.unitNumber == booking.unit_number }
                        booking.copy(unitImages = matchingBooking?.unitImages ?: "")
                    }

                    bookingsAdapter.updateData(updatedBookedList)
                } else {
                    Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Bookings>>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
            }
        })
    }

}

package com.example.ggapp

import BookedResponse
import Bookings
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookedPage : Fragment() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookedCardAdapter
    private lateinit var progressBar: ProgressBar

    // Track bookings removed by the user
    private val removedBookings = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_booked_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        progressBar = view.findViewById(R.id.progressBar)
        bookingsRecyclerView = view.findViewById(R.id.BookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        bookingsAdapter = BookedCardAdapter(
            mutableListOf(),
            onBookingClick = { booking -> Log.d("BookedPage", "Clicked booking: ${booking.unit_number}") },
            onBookingCancelled = { booking -> cancelBooking(booking) },
            onBookingRemoved = { booking -> removedBookings.add(booking.id.toString()) } // Add removed booking ID
        )

        bookingsRecyclerView.adapter = bookingsAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.email?.let { fetchPaidBookings(it) }
    }

    private fun fetchPaidBookings(userEmail: String) {

        progressBar.visibility = View.VISIBLE

        SupabaseClient.api.getPaidBookings(
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}"
        ).enqueue(object : Callback<List<BookedResponse>> {
            override fun onResponse(call: Call<List<BookedResponse>>, response: Response<List<BookedResponse>>) {
                if (response.isSuccessful) {
                    val paidBookingsList = response.body() ?: emptyList()

                    // Filter out bookings that have been marked as removed
                    val userBookings = paidBookingsList.filter {
                        it.user_email == userEmail && it.removed == false
                    }

                    fetchImagesForBookings(userBookings)
                } else {
                    Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<BookedResponse>>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
                progressBar.visibility = View.GONE
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

                    val updatedBookedList = bookedList.map { booking ->
                        val matchingBooking = bookingsList.find { it.unitNumber == booking.unit_number }
                        booking.copy(unitImages = matchingBooking?.unitImages ?: "")
                    }

                    progressBar.visibility = View.GONE
                    bookingsAdapter.updateData(updatedBookedList)
                } else {
                    Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<List<Bookings>>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun cancelBooking(booking: BookedResponse) {
        SupabaseClient.api.updateBookingStatus(
            id = "eq.${booking.id}",
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}",
            bookingStatus = mapOf("payment_status" to "cancelled")
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    bookingsAdapter.moveBookingToBottom(booking)
                    showRemoveBookingDialog(booking)
                } else {
                    Log.e("SupabaseError", "Failed to update booking: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
            }
        })
    }

    private fun showRemoveBookingDialog(booking: BookedResponse) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Remove Booking")
            .setMessage("This booking has already been cancelled. Do you want to remove it from the list?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Mark the booking as removed by calling the remove method on the adapter
                bookingsAdapter.removeBooking(booking)  // This calls the adapter method
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Do nothing, keep the booking visible
            }
            .create()

        dialog.show()
    }


}

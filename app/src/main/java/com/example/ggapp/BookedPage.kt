package com.example.ggapp

import BookedRequest
import BookedResponse
import Bookings
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
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

        // Check if user is logged in
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // User is not logged in, show an error message
            Toast.makeText(context, "Please sign in", Toast.LENGTH_SHORT).show()
        } else {
            fetchPaidBookings()  // Fetch bookings only if the user is logged in
        }

        // Set up RecyclerView
        bookingsRecyclerView = view.findViewById(R.id.BookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookingsAdapter = BookedCardAdapter(emptyList()) { booking ->
            Log.d("BookedPage", "Clicked booking: ${booking.unit_number}")
        }
        bookingsRecyclerView.adapter = bookingsAdapter
    }

    // Fetch only bookings for the logged-in user using the current user's email
    private fun fetchPaidBookings() {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        Log.d("EmailCheck", "Logged in user email: $userEmail")  // Log the email for debugging

        if (userEmail != null) {
            // This is where you will update your Supabase query call.
            SupabaseClient.api.getPaidBookingsForUser(
                apiKey = SupabaseClient.getApiKey(),
                authToken = "Bearer ${SupabaseClient.getApiKey()}",
                userEmail = "eq.${userEmail}" // Apply 'eq.' to the email for Supabase filtering
            ).enqueue(object : Callback<List<BookedResponse>> {
                override fun onResponse(call: Call<List<BookedResponse>>, response: Response<List<BookedResponse>>) {
                    if (response.isSuccessful) {
                        val paidBookingsList = response.body() ?: emptyList()
                        // Fetch the images for the bookings
                        fetchImagesForBookings(paidBookingsList)
                    } else {
                        Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<BookedResponse>>, t: Throwable) {
                    Log.e("SupabaseError", "API call failed: ${t.message}")
                }
            })
        } else {
            Toast.makeText(context, "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }


    // Fetch additional details (like images) for the booked rooms
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

    // Method to create a booking using the logged-in user's email
    fun createBooking(unitNumber: String, startDate: String, endDate: String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        Log.d("BookingEmailCheck", "Email being used for booking: $userEmail")  // Log the email for debugging

        if (userEmail != null) {
            val newBooking = BookedRequest(
                unit_number = unitNumber,
                start_date = startDate,
                end_date = endDate,
                user_email = userEmail ?: "" // Ensure this email is coming from FirebaseAuth
            )

            SupabaseClient.api.addBookedDates(
                apiKey = SupabaseClient.getApiKey(),
                authToken = "Bearer ${SupabaseClient.getApiKey()}",
                newBooked = newBooking
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Booking created successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("SupabaseError", "API call failed: ${t.message}")
                }
            })
        } else {
            Toast.makeText(context, "Error: User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}

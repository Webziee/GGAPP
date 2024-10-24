package com.example.ggapp

import BookedResponse
import Bookings
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import java.util.concurrent.TimeUnit
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
import java.text.SimpleDateFormat
import java.util.Locale
import android.provider.Settings

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

        // Check and request exact alarm permission before fetching bookings
        checkAndRequestExactAlarmPermission()

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.email?.let { fetchPaidBookings(it) }
    }

    private fun checkAndRequestExactAlarmPermission() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if we have the permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // If not, request the permission
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)  // Redirect to the app settings for user to grant permission
        } else {
            // If permission is already granted, you can proceed with scheduling alarms
            scheduleAlarmsIfNeeded()
        }
    }

    private fun scheduleAlarmsIfNeeded() {
        setRemindersForBookings()  // Schedule reminders after ensuring permission is granted
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
                        it.user_email == userEmail && !it.removed
                    }

                    fetchImagesForBookings(userBookings)
                    setRemindersForBookings()  // Set reminders after bookings are fetched
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
                bookingsAdapter.removeBooking(booking)  // Remove booking from adapter
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()  // Do nothing, keep the booking visible
            }
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)
    }

    private fun scheduleBookingReminder(context: Context, bookingTime: Long, bookingId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("booking_id", bookingId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, bookingId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val currentTime = System.currentTimeMillis()
        val triggerTime = bookingTime - TimeUnit.HOURS.toMillis(24)

        Log.d("Reminder", "Current time: $currentTime, Booking time: $bookingTime, Reminder set for: $triggerTime")

        if (triggerTime > currentTime) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("Reminder", "Reminder successfully scheduled for booking ID: $bookingId")
        } else {
            Log.d("Reminder", "Booking time has already passed for booking ID: $bookingId")
        }
    }

    private fun setRemindersForBookings() {
        // Loop through bookings and schedule reminders
        for (booking in bookingsAdapter.bookedList) {
            val bookingTime = getTimeFromBooking(booking)
            scheduleBookingReminder(requireContext(), bookingTime, booking.id.toString())
        }
    }

    private fun getTimeFromBooking(booking: BookedResponse): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            val date = dateFormat.parse(booking.start_date)
            val time = date?.time ?: System.currentTimeMillis()  // Convert to milliseconds
            Log.d("Reminder", "Parsed booking time for booking ID ${booking.id}: $time")
            time
        } catch (e: Exception) {
            Log.e("Reminder", "Failed to parse booking time for booking ID ${booking.id}: ${e.message}")
            System.currentTimeMillis()  // Use the current time as fallback
        }
    }
}

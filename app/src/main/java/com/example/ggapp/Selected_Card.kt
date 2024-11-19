package com.example.ggapp

import BookedRequest
import BookedResponse
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Selected_Card : AppCompatActivity() {

    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val pricePerNight = 2500
    private var totalPrice: Long = 0
    private val unavailableDates: MutableList<Pair<Long, Long>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_card)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val selectedDatesTextView = findViewById<TextView>(R.id.selectedDates)
        val numberOfDaysTextView = findViewById<TextView>(R.id.numberOfDays)
        val totalPriceTextView = findViewById<TextView>(R.id.totalPrice)
        val bookButton = findViewById<Button>(R.id.BookNowButton)

        // Fetch extra data passed from the previous activity
        val unitNumber = intent.getStringExtra("unitNumber") ?: "Unknown"
        val unitPrice = intent.getIntExtra("price", pricePerNight)
        val unitSleeper = intent.getIntExtra("sleeper", 0)
        val unitImageUrl = intent.getStringExtra("imageUrl")

        // Display unit details
        val unitImageView = findViewById<ImageView>(R.id.SelectedSheetImage)
        val unitPriceTextView = findViewById<TextView>(R.id.SelectedSheetPrice)
        val unitSleeperTextView = findViewById<TextView>(R.id.SelectedSheetSleeper)

        Picasso.get().load(unitImageUrl).placeholder(R.drawable.placeholderimage).into(unitImageView)
        unitPriceTextView.text = "Price R$unitPrice"
        unitSleeperTextView.text = "Sleeper $unitSleeper"

        // Initially, disable the book button
        bookButton.isEnabled = false

        // Fetch unavailable dates from Supabase for the current unit
        fetchUnavailableDates(unitNumber)

        /*The following code makes use of the calendar view to set the date (used for user date selection)
        * and was inspired by the following video:
        * TechinalCoding, 2023. Youtube, How to use Date and Time Picker Dialogs in android || Android Studio Tutorial. [Online]
        Available at: https://www.youtube.com/watch?v=guTycx3L9I4&t=237s
        [Accessed 04 October 2024].*/
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            when {
                startDate == null -> {
                    if (isDateRangeAvailable(selectedDate, selectedDate)) {
                        startDate = selectedDate
                        selectedDatesTextView.text = dateFormatter.format(selectedDate)
                    } else {
                        Toast.makeText(this, "This date is not available.", Toast.LENGTH_SHORT).show()
                    }
                }
                endDate == null && selectedDate >= startDate!! -> {
                    // Check every single day between the start date and the selected date for availability
                    var allDaysAvailable = true
                    var checkDate = startDate!!
                    while (checkDate <= selectedDate) {
                        if (!isDateRangeAvailable(checkDate, checkDate)) {
                            allDaysAvailable = false
                            break
                        }
                        checkDate += TimeUnit.DAYS.toMillis(1) // Increment day by day
                    }

                    if (allDaysAvailable) {
                        endDate = selectedDate
                        val numDays = TimeUnit.MILLISECONDS.toDays(endDate!! - startDate!! + 1)
                        totalPrice = numDays * pricePerNight
                        selectedDatesTextView.text = "${dateFormatter.format(startDate)} to ${dateFormatter.format(endDate)}"
                        numberOfDaysTextView.text = "$numDays night(s)"
                        totalPriceTextView.text = "Total Price: R$totalPrice"
                        bookButton.isEnabled = true
                    } else {
                        Toast.makeText(this, "Part of this date range is not available.", Toast.LENGTH_LONG).show()
                        resetDateSelection()
                    }
                }
                else -> {
                    Toast.makeText(this, "Invalid date selection. Please select the dates again.", Toast.LENGTH_SHORT).show()
                    resetDateSelection()
                }
            }
        }

        bookButton.setOnClickListener {
            // Navigate to Payment Page, passing booking details and unit info
            val intent = Intent(this, PaymentPage::class.java).apply {
                putExtra("startDate", startDate)
                putExtra("endDate", endDate)
                putExtra("totalPrice", totalPrice)
                putExtra("unitNumber", unitNumber)
                putExtra("unitPrice", unitPrice)
                putExtra("unitSleeper", unitSleeper)
                putExtra("unitImageUrl", unitImageUrl)
            }
            startActivity(intent)
        }
    }

    private fun resetDateSelection() {
        startDate = null
        endDate = null
        findViewById<TextView>(R.id.selectedDates).text = ""
        findViewById<TextView>(R.id.numberOfDays).text = ""
        findViewById<TextView>(R.id.totalPrice).text = ""
        findViewById<Button>(R.id.BookNowButton).isEnabled = false
    }

    private fun isDateRangeAvailable(start: Long, end: Long): Boolean {
        unavailableDates.forEach { (bookedStart, bookedEnd) ->
            // Check if selected date range overlaps with any booked date range
            if (start <= bookedEnd && end >= bookedStart) {
                return false // Overlap found, so the date range is not available
            }
        }
        return true // No overlap found, date range is available
    }

    /*The following code fetches dates from supabase. The method of fetching the data
* was inspired by the following video:
* Shukert, T., 2023. Youtube, Getting started with Android and Supabase. [Online]
Available at: https://www.youtube.com/watch?v=_iXUVJ6HTHU
[Accessed 02 October 2024].*/
    private fun fetchUnavailableDates(unitNumber: String) {
        SupabaseClient.api.getBookedDates(
            SupabaseClient.getApiKey(), // API key
            "Bearer ${SupabaseClient.getApiKey()}", // Bearer token
            "application/json",
            "eq.$unitNumber" // Pass the unit number with the equality operator
        ).enqueue(object : Callback<List<BookedResponse>> {
            override fun onResponse(call: Call<List<BookedResponse>>, response: Response<List<BookedResponse>>) {
                if (response.isSuccessful) {
                    val bookings = response.body() ?: emptyList()
                    updateUnavailableDates(bookings)
                } else {
                    Log.e("Supabase", "Failed to fetch booked dates: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<BookedResponse>>, t: Throwable) {
                Log.e("Supabase", "Error fetching booked dates", t)
            }
        })
    }

    private fun updateUnavailableDates(bookedDates: List<BookedResponse>) {
        unavailableDates.clear()
        bookedDates.forEach { booking ->
            val startMillis = convertDateToMillis(booking.start_date)
            val endMillis = convertDateToMillis(booking.end_date)
            unavailableDates.add(Pair(startMillis, endMillis))
        }
    }

    /*The following code converts date to millis
* and was inspired by the following video:
* TechinalCoding, 2023. Youtube, How to use Date and Time Picker Dialogs in android || Android Studio Tutorial. [Online]
Available at: https://www.youtube.com/watch?v=guTycx3L9I4&t=237s
[Accessed 04 October 2024].*/
    private fun convertDateToMillis(date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            sdf.parse(date)?.time ?: 0
        } catch (e: ParseException) {
            Log.e("Selected_Card", "Error parsing date: $date", e)
            0
        }
    }

}

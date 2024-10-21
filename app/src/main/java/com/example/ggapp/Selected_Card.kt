package com.example.ggapp

import ImageAdapter
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class Selected_Card : AppCompatActivity() {

    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val pricePerNight = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_card)

        // Set the content to fullscreen, hiding the status bar
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val selectedDatesTextView = findViewById<TextView>(R.id.selectedDates)
        val numberOfDaysTextView = findViewById<TextView>(R.id.numberOfDays)
        val totalPriceTextView = findViewById<TextView>(R.id.totalPrice)

        // Set minimum and maximum dates if needed
        calendarView.minDate = System.currentTimeMillis() // current date as min date

        // Listener for date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis

            if (startDate == null) {
                startDate = selectedDate
                // Update the selected dates text
                selectedDatesTextView.text = "${dateFormatter.format(selectedDate)}"
            } else if (endDate == null && selectedDate > startDate!!) {
                // Select end date if it’s after the start date
                endDate = selectedDate
                val numDays = TimeUnit.MILLISECONDS.toDays(endDate!! - startDate!!)

                // Calculate the total price
                val totalPrice = numDays * pricePerNight

                // Display the selected range in the TextView
                selectedDatesTextView.text = "${dateFormatter.format(startDate!!)} to ${dateFormatter.format(endDate!!)}"
                numberOfDaysTextView.text = "$numDays"
                totalPriceTextView.text = "R$totalPrice"

                // Reset selection after picking a range
                startDate = null
                endDate = null
            } else {
                // If user selects invalid end date or reselects start date, reset
                Toast.makeText(this, "Invalid end date selected. Try again.", Toast.LENGTH_SHORT).show()
                startDate = selectedDate
                endDate = null
                selectedDatesTextView.text = "${dateFormatter.format(selectedDate)}"
                numberOfDaysTextView.text = ""
                totalPriceTextView.text = ""
            }
        }


        // Initialize RecyclerView for horizontal image scrolling
        val recyclerView = findViewById<RecyclerView>(R.id.imagerecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // List of image URLs
        val imageUrls = listOf(
            "https://www.artefect.co.za/wp-content/uploads/2018/04/Constantia-Guest-House-3.jpg",
            "https://www.artefect.co.za/wp-content/uploads/2018/04/Southern-Suburbs-Apartment-4.jpg",
            "https://www.artefect.co.za/wp-content/uploads/2018/04/Southern-Suburbs-Apartment-5.jpg",
            "https://www.artefect.co.za/wp-content/uploads/2018/04/St-James-Guest-House-2.jpg",
            "https://www.artefect.co.za/wp-content/uploads/2018/04/Constantia-Guest-House-6.jpg"
        )

        // Set the adapter with a click listener
        recyclerView.adapter = ImageAdapter(imageUrls) { imageUrl ->
            // Launch a new activity or fragment with the selected image URL
            val intent = Intent(this, fullscreen::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        // Retrieve data passed from the previous activity/fragment
        val unitNumber = intent.getStringExtra("unitNumber")
        val price = intent.getIntExtra("price", 0)
        val sleeper = intent.getIntExtra("sleeper", 0)
        val imageUrl = intent.getStringExtra("imageUrl")

        // Find views in the layout
        val unitNumberTextView = findViewById<TextView>(R.id.SelectedSheetUnit)
        val priceTextView = findViewById<TextView>(R.id.SelectedSheetPrice)
        val sleeperTextView = findViewById<TextView>(R.id.SelectedSheetSleeper)
        val unitImageView = findViewById<ImageView>(R.id.SelectedSheetImage)

        // Set data to the views
        unitNumberTextView.text = "Unit $unitNumber"
        priceTextView.text = "Price R$price"
        sleeperTextView.text = "Sleeper $sleeper"

        // Debug: Check for nulls and log appropriately
        if (unitNumber != null) {
            Log.d("SelectedCard", "Unit Number received: $unitNumber")
        } else {
            Log.e("SelectedCard", "Unit Number is null")
        }

        // Load image using Picasso, add a placeholder for safety
        Picasso.get().load(imageUrl).placeholder(R.drawable.placeholderimage).into(unitImageView)


        val bookButton = findViewById<Button>(R.id.BookNowButton)

        bookButton.setOnClickListener {
            val intent = Intent(this, PaymentPage::class.java)
            startActivity(intent)
        }

        // Apply window insets if necessary
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

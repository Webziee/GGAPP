package com.example.ggapp

import BookedRequest
import BookedResponse
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PaymentPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_page)

        val startDate = intent.getLongExtra("startDate", 0)
        val endDate = intent.getLongExtra("endDate", 0)
        val totalPrice = intent.getLongExtra("totalPrice", 0)
        val unitNumber = intent.getStringExtra("unitNumber") ?: "Unknown"
        val unitImages = intent.getStringExtra("unitImages") ?: ""


        val payButton = findViewById<Button>(R.id.pay_button)

        // Set onClickListener for payment button
        payButton.setOnClickListener {
            // Save booking to Supabase when the user confirms payment
            saveBookingToSupabase(unitNumber, startDate, endDate, "tristanfrancis.singh2003@gmail.com", unitImages) // Pass the image URL here
        }
    }

    private fun saveBookingToSupabase(unitNumber: String, startDate: Long, endDate: Long, userEmail: String, unitImages: String) {
        val bookingRequest = BookedRequest(
            unit_number = unitNumber,
            start_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)),
            end_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)),
            user_email = userEmail,

        )

        val apiKey = SupabaseClient.getApiKey()
        val authToken = "Bearer $apiKey"

        SupabaseClient.api.addBookedDates(apiKey, authToken, "application/json", bookingRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PaymentPage, "Payment successful! Booking saved.", Toast.LENGTH_SHORT).show()

                        // Redirect to BookedPage after successful booking
                        val intent = Intent(this@PaymentPage, BookedPage::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this@PaymentPage, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PaymentPage, "Failed to save booking.", Toast.LENGTH_SHORT).show()
                }
            })
    }

}

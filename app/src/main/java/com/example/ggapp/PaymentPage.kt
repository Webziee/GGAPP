package com.example.ggapp

import BookedRequest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class PaymentPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_page)

        // Get the data passed from the Selected_Card activity
        val startDate = intent.getLongExtra("startDate", 0)
        val endDate = intent.getLongExtra("endDate", 0)
        val totalPrice = intent.getLongExtra("totalPrice", 0)
        val unitNumber = intent.getStringExtra("unitNumber") ?: "Unknown"
        val unitImages = intent.getStringExtra("unitImageUrl") ?: ""

        // Now you can use this data to display it or process it further
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        totalPriceTextView.text = "Total Price: R$totalPrice"

        // Display start date and end date if needed
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateTextView.text = "Booking: ${dateFormatter.format(Date(startDate))} to ${dateFormatter.format(Date(endDate))}"

        // Setup the payment logic...
        val cardholderName = findViewById<EditText>(R.id.cardholder_name)
        val cardNumber = findViewById<EditText>(R.id.card_number)
        val cardExpiry = findViewById<EditText>(R.id.card_expiry)
        val cardCVC = findViewById<EditText>(R.id.card_cvc)
        val payButton = findViewById<Button>(R.id.pay_button)

        // Add slash automatically when typing expiry date
        addExpiryDateTextWatcher(cardExpiry)

        payButton.setOnClickListener {
            // Get the current Firebase user
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                // Retrieve the user's email
                val userEmail = currentUser.email

                if (validatePaymentDetails(cardholderName, cardNumber, cardExpiry, cardCVC)) {
                    Toast.makeText(this, "Payment details are valid.", Toast.LENGTH_SHORT).show()

                    // Now use the logged-in user's email
                    if (userEmail != null) {
                        saveBookingToSupabase(unitNumber, startDate, endDate, userEmail, unitImages)
                    } else {
                        Toast.makeText(this, "User email not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No user is logged in.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun validatePaymentDetails(
        cardholderName: EditText,
        cardNumber: EditText,
        cardExpiry: EditText,
        cardCVC: EditText
    ): Boolean {
        // Validate cardholder's name (non-empty)
        if (cardholderName.text.toString().isEmpty()) {
            cardholderName.error = "Cardholder name is required"
            return false
        }

        // Validate card number (should be 16 digits)
        val cardNumberPattern = Pattern.compile("\\d{16}")
        if (!cardNumberPattern.matcher(cardNumber.text.toString()).matches()) {
            cardNumber.error = "Card number must be 16 digits"
            return false
        }

        // Validate expiry date (should be in MM/YY format and not expired)
        val expiryPattern = Pattern.compile("(0[1-9]|1[0-2])/([0-9]{2})")
        if (!expiryPattern.matcher(cardExpiry.text.toString()).matches()) {
            cardExpiry.error = "Invalid expiry date format. Use MM/YY"
            return false
        }

        if (!isValidExpiryDate(cardExpiry.text.toString())) {
            cardExpiry.error = "Card has expired"
            return false
        }

        // Validate CVC (3 or 4 digits)
        val cvcPattern = Pattern.compile("\\d{3,4}")
        if (!cvcPattern.matcher(cardCVC.text.toString()).matches()) {
            cardCVC.error = "CVC must be 3 or 4 digits"
            return false
        }

        // All fields are valid
        return true
    }

    private fun isValidExpiryDate(expiryDate: String): Boolean {
        val parts = expiryDate.split("/")
        val expMonth = parts[0].toInt()
        val expYear = "20${parts[1]}".toInt()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar months are zero-based
        val currentYear = calendar.get(Calendar.YEAR)

        return if (expYear > currentYear) {
            true
        } else expYear == currentYear && expMonth >= currentMonth
    }

    private fun saveBookingToSupabase(unitNumber: String, startDate: Long, endDate: Long, userEmail: String, unitImages: String) {
        val bookingRequest = BookedRequest(
            unit_number = unitNumber,
            start_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startDate)),
            end_date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endDate)),
            user_email = userEmail
        )

        val apiKey = SupabaseClient.getApiKey()
        val authToken = "Bearer $apiKey"

        SupabaseClient.api.addBookedDates(apiKey, authToken, "application/json", bookingRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PaymentPage, "Payment successful! Booking saved.", Toast.LENGTH_SHORT).show()

                        // Close the PaymentPage activity
                        finish()

                        // Navigate to BookedPage Fragment
                        val bookedPageFragment = ProfilePage()
                        replaceFragment(bookedPageFragment)
                    }
                    else {
                        Toast.makeText(this@PaymentPage, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@PaymentPage, "Failed to save booking.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun addExpiryDateTextWatcher(expiryEditText: EditText) {
        expiryEditText.addTextChangedListener(object : TextWatcher {
            var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true

                val input = s.toString().replace(Regex("[^\\d]"), "")

                val newInput = when {
                    input.length > 2 -> input.substring(0, 2) + "/" + input.substring(2)
                    else -> input
                }

                expiryEditText.setText(newInput)
                expiryEditText.setSelection(newInput.length)

                isEditing = false
            }
        })
    }

    // Function to replace the current fragment with the BookedPage fragment
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}

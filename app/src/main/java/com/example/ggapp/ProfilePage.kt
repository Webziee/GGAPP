package com.example.ggapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilePage : Fragment() {

    private lateinit var userEmailTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var editPhoneEditText: EditText
    private lateinit var editProfileButton: TextView
    private lateinit var logoutButton: LinearLayout
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_page, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Views
        userEmailTextView = view.findViewById(R.id.email_value)
        userNameTextView = view.findViewById(R.id.user_name)
        phoneTextView = view.findViewById(R.id.phone_value)
        editPhoneEditText = view.findViewById(R.id.edit_phone_value)
        editProfileButton = view.findViewById(R.id.edit_profile)
        logoutButton = view.findViewById(R.id.log_out_button)
        saveButton = view.findViewById(R.id.save_button) // Save Button for phone editing

        // Populate Email and Name
        populateUserDetails()

        // Set onClickListener for editing phone number
        editProfileButton.setOnClickListener {
            enablePhoneEditing()
        }

        // Set onClickListener for saving phone number
        saveButton.setOnClickListener {
            savePhoneNumber()
        }

        // Set onClickListener for Logout
        logoutButton.setOnClickListener {
            logOutUser()
        }

        return view
    }

    private fun populateUserDetails() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email
            userEmailTextView.text = email

            // Extract name from email before '@'
            val name = email?.substringBefore('@')?.replaceFirstChar { it.uppercase() }
            userNameTextView.text = name ?: "User Name"

            // Fetch phone number from Firestore and populate the TextView
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val phoneNumber = document.getString("phone")
                    phoneTextView.text = phoneNumber ?: "Not Available"
                }
        } else {
            Toast.makeText(context, "No user signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enablePhoneEditing() {
        // Hide the TextView and show the EditText to allow editing
        phoneTextView.visibility = View.GONE
        editPhoneEditText.visibility = View.VISIBLE
        saveButton.visibility = View.VISIBLE

        // Populate the EditText with the current phone number
        editPhoneEditText.setText(phoneTextView.text.toString())
    }

    private fun savePhoneNumber() {
        val currentUser = auth.currentUser ?: return
        val newPhoneNumber = editPhoneEditText.text.toString()

        if (newPhoneNumber.isEmpty()) {
            Toast.makeText(context, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Save the new phone number to Firestore
        db.collection("users").document(currentUser.uid)
            .update("phone", newPhoneNumber)
            .addOnSuccessListener {
                Toast.makeText(context, "Phone number updated successfully", Toast.LENGTH_SHORT).show()

                // Update the TextView with the new phone number and hide the EditText
                phoneTextView.text = newPhoneNumber
                phoneTextView.visibility = View.VISIBLE
                editPhoneEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update phone number", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logOutUser() {
        auth.signOut()
        // Redirect to login or welcome screen
        // val intent = Intent(activity, LoginActivity::class.java)
        // startActivity(intent)
        activity?.finish() // Finish current activity
    }
}

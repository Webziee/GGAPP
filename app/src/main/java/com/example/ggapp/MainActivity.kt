package com.example.ggapp

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.ggapp.databinding.ActivityMainBinding
import com.example.goergesgraceapp.ExplorePage
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        //push notifications permission, (SNATech,2022)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // Inflate the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view using the binding root
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in and handle accordingly
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user is signed in, redirect to SignUp/SignIn page
            redirectToSignUp()
        }

        // Enable edge-to-edge after setting content view
        enableEdgeToEdge()

        // Set initial fragment
        replaceFragment(ExplorePage())

        // Set the listener for bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.explore -> replaceFragment(ExplorePage())
                R.id.booked -> replaceFragment(BookedPage())
                R.id.profile -> replaceFragment(ProfilePage())
                else -> false
            }
            true
        }

        // Apply window insets to adjust layout for system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }
    }

    // Function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun redirectToSignUp() {
        // Implement the logic to redirect to SignUp or SignIn screen
        // You could use Intent to start the activity
    }
}

package com.example.ggapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.ggapp.databinding.ActivityMainBinding
import com.example.ggapp.BookedPage
import com.example.ggapp.ProfilePage
import com.example.goergesgraceapp.ExplorePage
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view using the binding root
        setContentView(binding.root)

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
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    // Function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragment)
        fragmentTransaction.commit()
    }
}

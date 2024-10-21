package com.example.ggapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.ggapp.databinding.ActivityMainBinding
import com.example.goergesgraceapp.ExplorePage
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private  lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        replaceFragment(ExplorePage())
        binding.bottomNavigationView.setOnItemSelectedListener{
            when(it.itemId){
                R.id.explore -> replaceFragment(ExplorePage())
                R.id.booked -> replaceFragment(BookedPage())
                R.id.profile -> replaceFragment(ProfilePage())

                else -> {

                }
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Set padding to the main layout to avoid clipping under the system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            // Apply padding to the BottomNavigationView to cover the navigation bar
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.updatePadding(bottom = systemBars.bottom)

            insets
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()
    }
}
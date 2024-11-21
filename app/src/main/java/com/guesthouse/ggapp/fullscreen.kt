package com.guesthouse.ggapp

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso

class Fullscreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Ensure this is correctly implemented
        setContentView(R.layout.activity_fullscreen)

        val imageView = findViewById<ImageView>(R.id.fullScreenImageView)
        val imageUrl = intent.getStringExtra("imageUrl")

        // Load the image using Picasso with a placeholder or error handling
        Picasso.get()
            .load(imageUrl)
            .placeholder(R.drawable.placeholderimage) // Optional placeholder image
            .error(R.drawable.placeholderimage)       // Optional error image
            .into(imageView)

        // Adjust for system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

package com.example.goergesgraceapp

import Bookings
import BookingsCardAdapter
import ImageAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ggapp.R
import com.example.ggapp.Selected_Card
import com.example.ggapp.fullscreen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExplorePage : Fragment(), OnMapReadyCallback {

    // Google Maps
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    // Room bookings
    private lateinit var availablerooms: Button
    private lateinit var allrooms: Button
    private lateinit var bookingsrecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore_page, container, false)

        // Initialize mapView
        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Set up RecyclerView
        bookingsrecyclerView = view.findViewById(R.id.BookingsRecyclerView)
        bookingsrecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize BookingsAdapter with the click listener to show the bottom sheet
        bookingsAdapter = BookingsCardAdapter(emptyList()) { booking ->
            showBottomSheet(booking)  // Show the bottom sheet when a booking is clicked
        }
        bookingsrecyclerView.adapter = bookingsAdapter

        // Fetch bookings data from Supabase
        fetchBookings()


        // Handle insets
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }


    // Fetch bookings from Supabase
    private fun fetchBookings() {
        SupabaseClient.api.getBookings(
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}"
        ).enqueue(object : Callback<List<Bookings>> {
            override fun onResponse(call: Call<List<Bookings>>, response: Response<List<Bookings>>) {
                if (response.isSuccessful) {
                    val bookingsList = response.body() ?: emptyList()
                    Log.d("SupabaseResponse", "Bookings List: $bookingsList")
                    bookingsAdapter.updateData(bookingsList)
                } else {
                    Log.e("SupabaseError", "API call failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Bookings>>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
            }
        })
    }

    fun showBottomSheet(booking: Bookings) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_slide_up, null)

        // Find views inside the bottom sheet layout
        val bottomSheetunit = bottomSheetView.findViewById<TextView>(R.id.BottomSheetUnit)
        val bottomSheetprice = bottomSheetView.findViewById<TextView>(R.id.BottomSheetPrice)
        val bottomSheetsleeper = bottomSheetView.findViewById<TextView>(R.id.BottomSheetSleeper)
        val bottomSheetImage = bottomSheetView.findViewById<ImageView>(R.id.BottomSheetImage)
        val bookNowButton = bottomSheetView.findViewById<Button>(R.id.bookingButton)

        // Initialize RecyclerView for horizontal image scrolling from bottomSheetView, not from activity
        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.imagerecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

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
            val intent = Intent(requireContext(), fullscreen::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        // Set data for the specific booking item
        bottomSheetunit.text = "Unit ${booking.unitNumber}"
        bottomSheetprice.text = "R${booking.price} (off peak)\nper night"
        bottomSheetsleeper.text = "${booking.sleeper} Sleeper"

        // Load the main image using Picasso
        if (!booking.unitImages.isNullOrEmpty()) {
            Picasso.get().load(booking.unitImages).into(bottomSheetImage)
        } else {
            bottomSheetImage.setImageResource(R.drawable.placeholderimage)
        }

        bookNowButton.setOnClickListener {
            Log.d("BookNowButton", "Book Now button clicked")

            // Start the next activity
            val intent = Intent(requireContext(), Selected_Card::class.java).apply {
                putExtra("unitNumber", booking.unitNumber)
                putExtra("price", booking.price)
                putExtra("sleeper", booking.sleeper)
                putExtra("imageUrl", booking.unitImages)
            }
            startActivity(intent)
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    // Google Maps
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Set the location of the marker
        val belaBela = LatLng(-24.8850, 28.2950) // Coordinates of Wambaths, Bela-Bela
        googleMap.addMarker(MarkerOptions().position(belaBela).title("Wambaths, Bela-Bela"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(belaBela, 10f)) // Zoom level
    }

    // MapView lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}

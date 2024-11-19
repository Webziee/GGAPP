package com.example.ggapp

import Bookings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso

class BookingBottomSheet(private val booking: Bookings) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.bottom_slide_up, container, false)

        // Find views inside the bottom sheet layout
        val bottomSheetunit = view.findViewById<TextView>(R.id.BottomSheetUnit)
        val bottomSheetprice = view.findViewById<TextView>(R.id.BottomSheetPrice)
        val bottomSheetsleeper = view.findViewById<TextView>(R.id.BottomSheetSleeper)
        val bottomSheetImage = view.findViewById<ImageView>(R.id.BottomSheetImage)

        // Set data for the specific booking item
        bottomSheetunit.text = "Unit ${booking.unitNumber}"
        bottomSheetprice.text = "R${booking.price} (off peak)\nper night"
        bottomSheetsleeper.text = "${booking.sleeper} Sleeper"


        /*Load the image using Picasso or a placeholder if no image exists. This code was
        * inspired by the following video:
        * CodexCreator, 2023. Youtube, How to Load Images From URL Using Picasso in Android Studio. [Online]
          Available at: https://www.youtube.com/watch?v=4o6MGyrKBgg
          [Accessed 03 October 2024].*/
        if (!booking.unitImages.isNullOrEmpty()) {
            Picasso.get().load(booking.unitImages).into(bottomSheetImage)
        } else {
            Picasso.get().load(R.drawable.placeholderimage).into(bottomSheetImage)
        }

        return view
    }
}

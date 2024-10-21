package com.example.ggapp

import BookedResponse
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ggapp.R
import com.squareup.picasso.Picasso

class BookedCardAdapter(
    private var bookedList: List<BookedResponse>, // List of BookedResponse objects
    private val onBookingClick: (BookedResponse) -> Unit // Lambda function to handle clicks
) : RecyclerView.Adapter<BookedCardAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unitImage: ImageView = itemView.findViewById(R.id.UnitImage)
        val unitNumber: TextView = itemView.findViewById(R.id.UnitNumber)
        val unitPrice: TextView = itemView.findViewById(R.id.UnitPrice)
        val unitSleepers: TextView = itemView.findViewById(R.id.UnitSleepers)
        val unitAvailability: TextView = itemView.findViewById(R.id.UnitAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_cards, parent, false)
        return BookedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookedViewHolder, position: Int) {
        val currentItem = bookedList[position]

        // Bind data to the views
        if (!currentItem.unitImages.isNullOrEmpty()) {
            Picasso.get().load(currentItem.unitImages).into(holder.unitImage)
        } else {
            Picasso.get().load(R.drawable.placeholderimage).into(holder.unitImage)
        }

        holder.unitNumber.text = "Unit ${currentItem.unit_number}"
        holder.unitPrice.text = "R2500 per night" // Adjust price if needed
        holder.unitSleepers.text = "6 Sleepers" // Adjust sleepers if needed
        holder.unitAvailability.text = "Paid"

        // Handle click event
        holder.itemView.setOnClickListener {
            onBookingClick(currentItem)
        }
    }


    override fun getItemCount(): Int {
        return bookedList.size
    }

    fun updateData(newBookedList: List<BookedResponse>) {
        bookedList = newBookedList
        notifyDataSetChanged()  // Notify the RecyclerView to refresh
    }

}

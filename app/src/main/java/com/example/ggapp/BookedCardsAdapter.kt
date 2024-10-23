package com.example.ggapp

import BookedResponse
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response

class BookedCardAdapter(
    private var bookedList: MutableList<BookedResponse>, // MutableList to allow updates
    private val onBookingClick: (BookedResponse) -> Unit // Lambda function to handle clicks
) : RecyclerView.Adapter<BookedCardAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unitImage: ImageView = itemView.findViewById(R.id.UnitImage)
        val unitNumber: TextView = itemView.findViewById(R.id.UnitNumber)
        val unitSleepers: TextView = itemView.findViewById(R.id.UnitSleepers)
        val unitAvailability: TextView = itemView.findViewById(R.id.UnitAvailability)
        val date: TextView = itemView.findViewById(R.id.date)
        val cancelButton: Button = itemView.findViewById(R.id.cancel_booking_button) // Adding cancel button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booked_cards, parent, false)
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
        holder.unitSleepers.text = "6 Sleeper"
        holder.date.text = "${currentItem.start_date} - ${currentItem.end_date}"
        holder.unitAvailability.text = currentItem.payment_status ?: "Paid"

        // Set color and button visibility based on payment status
        if (currentItem.payment_status == "Paid") {
            holder.unitAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))  // Set color to green for Paid
            holder.cancelButton.visibility = View.VISIBLE  // Show cancel button for active bookings
        } else if (currentItem.payment_status == "Cancelled") {
            holder.unitAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))    // Set color to red for Cancelled
            holder.cancelButton.visibility = View.GONE  // Hide cancel button for cancelled bookings
        } else {
            holder.unitAvailability.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))  // Default color for other statuses
            holder.cancelButton.visibility = View.VISIBLE
        }

        // Handle Cancel Booking Button Click
        holder.cancelButton.setOnClickListener {
            updateBookingStatusInDatabase(currentItem, position)
        }

        // Handle click event for the whole item
        holder.itemView.setOnClickListener {
            onBookingClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return bookedList.size
    }

    fun updateData(newBookedList: List<BookedResponse>) {
        bookedList.clear()
        bookedList.addAll(newBookedList)
        notifyDataSetChanged()  // Notify the RecyclerView to refresh
    }

    private fun updateBookingStatusInDatabase(booking: BookedResponse, position: Int) {
        val updatedBookingData = mapOf("payment_status" to "Cancelled")

        SupabaseClient.api.updateBookingStatus(
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}",
            bookingId = "eq.${booking.id}",  // Use the correct filter for the booking ID
            updatedBooking = updatedBookingData
        ).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("UpdateBooking", "Booking status updated successfully.")

                    // Update the local booking data
                    booking.payment_status = "Cancelled"

                    // Remove the item from its current position and add it to the end of the list
                    bookedList.removeAt(position)
                    bookedList.add(booking)

                    // Notify the adapter that the list has changed
                    sortListByStatus()

                } else {
                    Log.e("UpdateBooking", "Failed to update booking: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("UpdateBooking", "API call failed: ${t.message}")
            }
        })
    }

    // Function to sort bookings so that only "Cancelled" bookings are moved to the bottom
    private fun sortListByStatus() {
        // Filter the list into active and cancelled bookings
        val activeBookings = bookedList.filter { it.payment_status != "Cancelled" }
        val cancelledBookings = bookedList.filter { it.payment_status == "Cancelled" }

        // Clear the list and add active bookings followed by cancelled bookings
        bookedList.clear()
        bookedList.addAll(activeBookings)
        bookedList.addAll(cancelledBookings)

        // Notify the adapter that the data has changed
        notifyDataSetChanged()
    }
}


package com.example.ggapp

import BookedResponse
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.ggapp.R
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

/*The following code makes use of a Mutable list for modifying data, this code was inspired
* by the following video:
    DevLanding, 2017. Youtube, Android Kotlin MutableList VS List. [Online]
    Available at: https://www.youtube.com/watch?v=_SgmgA7Kz2g
    [Accessed 01 October 2024].
*/
class BookedCardAdapter(
    var bookedList: MutableList<BookedResponse>, // Use MutableList for modifying data
    private val onBookingClick: (BookedResponse) -> Unit, // Lambda function to handle clicks
    private val onBookingCancelled: (BookedResponse) -> Unit, // Lambda for cancelling bookings
    private val onBookingRemoved: (BookedResponse) -> Unit // Lambda for removing bookings
) : RecyclerView.Adapter<BookedCardAdapter.BookedViewHolder>() {

    inner class BookedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unitImage: ImageView = itemView.findViewById(R.id.UnitImage)
        val unitNumber: TextView = itemView.findViewById(R.id.UnitNumber)
        val unitSleepers: TextView = itemView.findViewById(R.id.UnitSleepers)
        val unitAvailability: TextView = itemView.findViewById(R.id.UnitAvailability)
        val date: TextView = itemView.findViewById(R.id.date)
        val cancelBookingButton: TextView = itemView.findViewById(R.id.cancel_booking_button)
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
        holder.unitSleepers.text = "6 Sleepers"
        holder.unitAvailability.text = currentItem.payment_status?.capitalize() ?: "Paid"

        if (currentItem.start_date != null && currentItem.end_date != null) {
            holder.date.text = "${currentItem.start_date} - ${currentItem.end_date}"
        } else {
            holder.date.text = "Dates not available"
        }

        if (currentItem.payment_status == "Paid") {
            holder.unitAvailability.setTextColor(Color.GREEN)
            holder.cancelBookingButton.text = "Cancel Booking"
        } else if (currentItem.payment_status == "cancelled") {
            holder.unitAvailability.setTextColor(Color.RED)
            holder.cancelBookingButton.text = "Remove Booking"
            holder.cancelBookingButton.setTextColor(Color.GRAY)
        }

        // Handle the cancel or remove action
        holder.cancelBookingButton.setOnClickListener {
            if (currentItem.payment_status == "cancelled") {
                // If the booking is already canceled, show the remove booking dialog
                showRemoveBookingDialog(currentItem, holder)
            } else {
                // Show the cancel confirmation dialog
                val dialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel your booking?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Call the cancellation logic only if the user clicks "Yes"
                        onBookingCancelled(currentItem)

                        // Change button appearance after cancellation
                        holder.cancelBookingButton.setTextColor(Color.RED)
                        holder.cancelBookingButton.text = "Remove Booking"
                        holder.cancelBookingButton.isEnabled = true

                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                // Show the dialog before customizing the button colors
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)    // "Yes" button in red
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)   // "No" button in gray
            }
        }

        // Handle card click event
        holder.itemView.setOnClickListener {
            onBookingClick(currentItem) // Handle card click
        }
    }

    fun moveBookingToBottom(booking: BookedResponse) {
        val index = bookedList.indexOf(booking)
        if (index != -1) {
            // Remove the booking from its current position
            bookedList.removeAt(index)
        }

        // Update the status to "cancelled"
        booking.payment_status = "cancelled"

        // Add the cancelled booking to the bottom of the list
        bookedList.add(booking)

        // Notify the adapter to refresh the list
        notifyItemMoved(index, bookedList.size - 1)  // Move item visually
        notifyItemChanged(bookedList.size - 1)  // Ensure it's updated
    }

    // Update the dataset with new bookings
    fun updateData(newBookings: List<BookedResponse>) {
        bookedList.clear()
        bookedList.addAll(newBookings)
        notifyDataSetChanged()
    }

    private fun showRemoveBookingDialog(booking: BookedResponse, holder: BookedViewHolder) {
        val dialog = AlertDialog.Builder(holder.itemView.context)
            .setTitle("Remove Booking")
            .setMessage("This booking has already been cancelled. Do you want to remove it from the list?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Call the remove booking logic
                removeBooking(booking)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {
            // Ensure button colors are set when the dialog is shown
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)    // "Yes" button in red
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.GRAY)   // "No" button in gray
        }

        // Show the dialog
        dialog.show()
    }


    fun removeBooking(booking: BookedResponse) {
        SupabaseClient.api.updateRemovedStatus(
            id = "eq.${booking.id}",
            apiKey = SupabaseClient.getApiKey(),
            authToken = "Bearer ${SupabaseClient.getApiKey()}",
            removedStatus = mapOf("removed" to true)  // Set the removed field to true in the database
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Visually remove the booking from the RecyclerView by updating this adapter's data
                    bookedList.remove(booking)  // Remove the booking from the list
                    notifyDataSetChanged()  // Notify the adapter that the data has changed
                    Log.d("Booking", "Booking marked as removed")
                } else {
                    Log.e("SupabaseError", "Failed to mark booking as removed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SupabaseError", "API call failed: ${t.message}")
            }
        })
    }

    override fun getItemCount(): Int {
        return bookedList.size
    }
}

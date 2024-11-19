import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.guesthouse.ggapp.R
import com.guesthouse.goergesgraceapp.ExplorePage
import com.squareup.picasso.Picasso
/*The following code for using Lambda function to handle clicks was inspired by the following video:
Kumar, J., 2017. Youtube, Lambda Function in Android for Click Listeners. [Online]
Available at: https://www.youtube.com/watch?v=saZhriuOTqk
[Accessed 04 October 2024].
*/
class BookingsCardAdapter(
    private var bookingsList: List<Bookings>,
    private val onBookingClick: (Bookings) -> Unit // Lambda function to handle clicks
) : RecyclerView.Adapter<BookingsCardAdapter.BookingsViewHolder>() {

    inner class BookingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unitImage: ImageView = itemView.findViewById(R.id.UnitImage)
        val unitNumber: TextView = itemView.findViewById(R.id.UnitNumber)
        val unitPrice: TextView = itemView.findViewById(R.id.UnitPrice)
        val unitSleepers: TextView = itemView.findViewById(R.id.UnitSleepers)
        val unitAvailability: TextView = itemView.findViewById(R.id.UnitAvailability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_cards, parent, false)
        return BookingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingsViewHolder, position: Int) {
        val currentItem = bookingsList[position]

        // Bind data to the views
        if (!currentItem.unitImages.isNullOrEmpty()) {
            Picasso.get().load(currentItem.unitImages).into(holder.unitImage)
        } else {
            Picasso.get().load(R.drawable.placeholderimage).into(holder.unitImage)
        }

        holder.unitNumber.text = "Unit ${currentItem.unitNumber}"
        holder.unitPrice.text = "R${currentItem.price} per night"
        holder.unitSleepers.text = "${currentItem.sleeper} Sleepers"
        holder.unitAvailability.text = currentItem.status
        Log.d("BookingsCardAdapter", "Unit Number in adapter: ${currentItem.unitNumber}")

        // Invoke the click listener passed from the fragment
        holder.itemView.setOnClickListener {
            onBookingClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return bookingsList.size
    }

    // This function will update the list and notify the adapter
    fun updateData(newBookingsList: List<Bookings>) {
        bookingsList = newBookingsList
        notifyDataSetChanged()  // Notify the RecyclerView to refresh
    }
}

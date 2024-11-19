import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.guesthouse.ggapp.R
import com.squareup.picasso.Picasso

class ImageAdapter(
    private val imageUrls: List<String>,
    private val onImageClick: (String) -> Unit // Pass the click callback
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Picasso.get().load(imageUrl).into(holder.imageView)

        // Set click listener on the image
        holder.imageView.setOnClickListener {
            onImageClick(imageUrl) // Pass the image URL when clicked
        }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}


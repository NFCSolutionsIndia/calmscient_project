import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.calmscient.R
import com.calmscient.di.remote.CardItemDataClass

class GridItemAdapter(private val cardItems: List<CardItemDataClass>) :
    RecyclerView.Adapter<GridItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.videoThumbnailImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardItem = cardItems[position]
        Glide.with(holder.itemView)
            .load(cardItem.contentIcons.first()) // Assuming contentIcons has at least one item
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return cardItems.size
    }
}

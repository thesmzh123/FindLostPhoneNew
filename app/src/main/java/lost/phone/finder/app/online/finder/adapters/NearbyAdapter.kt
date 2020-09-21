package lost.phone.finder.app.online.finder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.home_recyclerview_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.models.Nearby

class NearbyAdapter(val context: Context, val nearbyList: ArrayList<Nearby>) :
    RecyclerView.Adapter<NearbyAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recyclerview_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return nearbyList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val nearby = nearbyList[position]
        holder.itemView.webIconText.text = nearby.text
        Glide.with(context).load(nearby.icon).into(holder.itemView.webIcon)
    }
}
package lost.phone.finder.app.online.finder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.bookmark_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.models.Bookmarks


class BookmarkAdapter(val context: Context, private val bookmarksList: ArrayList<Bookmarks>) :
    RecyclerView.Adapter<BookmarkAdapter.MyViewHolder>() {
    private val mColorGenerator = ColorGenerator.DEFAULT
    private var mDrawableBuilder: TextDrawable? = null

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return bookmarksList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val websites = bookmarksList[position]
        var letter: String? = null
        holder.itemView.recycle_title.text = websites.title
        if (websites.title.isNotEmpty()) {
            letter = websites.title.substring(0, 1)
        }

        val color = mColorGenerator.randomColor

        // Create a circular icon consisting of  a random background colour and first letter of title
        mDrawableBuilder = TextDrawable.builder()
            .buildRound(letter, color)
        holder.itemView.thumbnail_image.setImageDrawable(mDrawableBuilder)

        if (position >= 2) {
            if (position % 2 == 0) {
                adView(holder.itemView.adView)
            } else {
                holder.itemView.adView.visibility = View.GONE
            }
        } else {
            holder.itemView.adView.visibility = View.GONE
        }
    }

    private fun adView(adView: AdView) {
//        adView.visibility = View.GONE
        try {
            if (!SharedPrefUtils.getBooleanData(context, "hideAds")) {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
                adView.adListener = object : AdListener() {

                    override fun onAdLoaded() {
                        adView.visibility = View.VISIBLE
                    }

                    override fun onAdFailedToLoad(error: Int) {
                        adView.visibility = View.GONE
                    }

                }
            } else {
                adView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
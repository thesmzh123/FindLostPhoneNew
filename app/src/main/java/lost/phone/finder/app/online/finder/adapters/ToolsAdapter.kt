package lost.phone.finder.app.online.finder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.custom_tool_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.models.Tools

class ToolsAdapter(val toolList: ArrayList<Tools>, val context: Context) :
    RecyclerView.Adapter<ToolsAdapter.MyHolder>() {
    class MyHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_tool_layout, parent, false)

        return MyHolder(
            itemView
        )
    }

    override fun getItemCount(): Int {
        return toolList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val tool = toolList[position]
        holder.itemView.toolText.text = tool.toolText
        Glide.with(context).load(tool.toolIcon).into(holder.itemView.toolIcon)
    }
}
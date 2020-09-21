package lost.phone.finder.app.online.finder.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.device_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.BaseActivity
import lost.phone.finder.app.online.finder.models.Devices


class DevicesAdapter(
    val context: Context,
    private val deviceList: ArrayList<Devices>,
    val mainUrl: String
) :
    RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {
    constructor(
        context: Context,
        deviceList: ArrayList<Devices>,
        mainUrl: String,
        customNum: TextInputEditText,
        customMsg: TextInputEditText
    ) : this(context, deviceList, mainUrl) {
        this.customNum = customNum
        this.customMsg = customMsg
        (context as BaseActivity).loadInterstial()

    }


    private var customNum: TextInputEditText? = null
    private var customMsg: TextInputEditText? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.device_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val devices = deviceList[position]
        holder.bindItem(deviceList[position])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(devices: Devices) {
            itemView.deviceName.text = devices.deviceName
            itemView.deviceModel.text = devices.model
       /*     itemView.isChecked.visibility =
                if (devices.isChecked) View.VISIBLE else View.GONE*/

            itemView.clickItem.setOnClickListener {
                devices.isChecked = !devices.isChecked
                itemView.isChecked.isChecked =
                    if (devices.isChecked) true else false

            }
        }

    }

    fun getMultiSelectionDevice(): ArrayList<Devices> {
        val selected = ArrayList<Devices>()
        for (i in deviceList.indices) {
            if (deviceList[i].isChecked) {
                selected.add(deviceList[i])
            }
        }
        return selected
    }
}
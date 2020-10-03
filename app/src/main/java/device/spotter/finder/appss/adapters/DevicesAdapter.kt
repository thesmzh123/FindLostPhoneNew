@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package device.spotter.finder.appss.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_layout.view.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.BaseActivity
import device.spotter.finder.appss.models.Devices


class DevicesAdapter(
    val context: Context,
    private val deviceList: ArrayList<Devices>
) :
    RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {
    init {
        (context as BaseActivity).loadInterstial()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.device_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(deviceList[position])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(devices: Devices) {
            itemView.deviceName.text = devices.deviceName
            itemView.deviceModel.text = devices.model

            itemView.clickItem.setOnClickListener {
                devices.isChecked = !devices.isChecked
                itemView.isChecked.isChecked =
                    devices.isChecked

            }
            itemView.isChecked.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    devices.isChecked = b
                    itemView.isChecked.isChecked = b
                } else {
                    devices.isChecked = false
                    itemView.isChecked.isChecked = false
                }
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
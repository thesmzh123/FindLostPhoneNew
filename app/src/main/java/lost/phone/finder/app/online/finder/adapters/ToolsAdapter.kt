@file:Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")

package lost.phone.finder.app.online.finder.adapters

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.custom_tool_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.BaseActivity
import lost.phone.finder.app.online.finder.activities.PrivateBrowserActivity
import lost.phone.finder.app.online.finder.models.Tools

class ToolsAdapter(private val toolList: ArrayList<Tools>, val context: Context) :
    RecyclerView.Adapter<ToolsAdapter.MyHolder>() {
    var isTorchOn: Boolean? = null
    var level: Int = 0
    private var camera: Camera? = null
    private var parameters: Camera.Parameters? = null
    private var mCameraManager: CameraManager? = null
    private var mCameraId: String? = null
    private var myHolder: MyHolder? = null

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
        myHolder = holder
        torchInit()
        val tool = toolList[position]
        holder.itemView.toolText.text = tool.toolText
        Glide.with(context).load(tool.toolIcon).into(holder.itemView.toolIcon)
        holder.itemView.setOnClickListener {
            performClick(position, holder)
        }
    }

    private fun torchInit() {
        isTorchOn = false
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            mCameraId = mCameraManager!!.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        getBatteryPercentage()

    }

    //Battery Percentage
    private fun getBatteryPercentage() {
        val batteryLevelReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context, intent: Intent) {
                context.unregisterReceiver(this)
                val currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level = -1
                if (currentLevel >= 0 && scale > 0) {
                    level = currentLevel * 100 / scale
                }
            }
        }
        val batteryLevelFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryLevelReceiver, batteryLevelFilter)
    }

    private fun performClick(
        position: Int,
        holder: MyHolder
    ) {
        if (position == 0) {
            (context as BaseActivity).navigateFragmentByAds(R.id.compassFragment)

        } else if (position == 1) {
            checkFlash()
            if (level <= 15) {
                (context as BaseActivity).showToast(context.getString(R.string.battery_low_flash))
            } else {
                openFlash(holder)
            }
        } else if (position == 2) {
            context.startActivity(
                Intent(
                    context,
                    PrivateBrowserActivity::class.java
                )
            )
        }
    }

    private fun openFlash(holder: MyHolder) {
        isTorchOn = if (isTorchOn!!) {
            Glide.with(context).load(R.drawable.torch)
                .into(holder.itemView.toolIcon)
            switchOffTorch()
            false

        } else {
            Glide.with(context).load(R.drawable.torchoff)
                .into(holder.itemView.toolIcon)
            switchOnTorch()
            true

        }
    }

    private fun switchOnTorch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCameraId?.let { mCameraManager?.setTorchMode(it, true) }
        } else {
            try {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera?.parameters = parameters
                camera?.startPreview()


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    fun switchOffTorch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCameraId?.let { mCameraManager?.setTorchMode(it, false) }
        } else {
            try {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera?.parameters = parameters
                camera?.stopPreview()
                Glide.with(context).load(R.drawable.torch)
                    .into(myHolder!!.itemView.toolIcon)
            } catch (e: Exception) {
                e.printStackTrace()

            }

        }
    }

    private fun checkFlash() {
        val isFlashAvailable = context.packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FLASH
        )
        if (!isFlashAvailable) {

            val alert = MaterialAlertDialogBuilder(
                context,
                R.style.MaterialAlertDialogTheme
            ).create()
            alert.setTitle(context.getString(R.string.error))
            alert.setMessage(context.getString(R.string.no_flash_light))
            alert.setButton(
                DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok)
            ) { dialog, which ->
                // closing the application
                dialog.dismiss()

            }
            alert.show()
            return
        }
    }

}
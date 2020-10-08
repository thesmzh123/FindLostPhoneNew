@file:Suppress("SameParameterValue")

package device.spotter.finder.appss.adapters

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.BaseActivity
import device.spotter.finder.appss.activities.FamilyLocatorActivity
import device.spotter.finder.appss.models.FamilyLocator
import device.spotter.finder.appss.receivers.ShareLocReceiver
import device.spotter.finder.appss.utils.Constants.TAGI
import device.spotter.finder.appss.utils.Constants.UNSELECTED
import device.spotter.finder.appss.utils.RegisterAPI
import kotlinx.android.synthetic.main.activity_family_locator.*
import kotlinx.android.synthetic.main.custom_message_layout.view.*
import kotlinx.android.synthetic.main.enter_phone_number_layout.view.cancel
import kotlinx.android.synthetic.main.enter_phone_number_layout.view.send
import kotlinx.android.synthetic.main.family_locator_list_layout.view.*
import net.cachapa.expandablelayout.ExpandableLayout
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader


class FamilyLocatorAdapter(
    val context: Context,
    private val familyLocatorList: ArrayList<FamilyLocator>
) :
    RecyclerView.Adapter<FamilyLocatorAdapter.MyHolder>(),
    ExpandableLayout.OnExpansionUpdateListener {
    init {
        (context as BaseActivity).loadInterstial()
    }

    private var selectedItem: Int = UNSELECTED
    private var deleteDialogView: View? = null

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.family_locator_list_layout, parent, false)
        return MyHolder(v)
    }

    override fun getItemCount(): Int {
        return familyLocatorList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val isSelected = position == selectedItem
        val familyLocator = familyLocatorList[position]
        holder.itemView.name.text = familyLocator.name

        holder.itemView.phoneNum.text = familyLocator.phoneNum

        holder.itemView.expandable_layout.setInterpolator(OvershootInterpolator())
        holder.itemView.expandable_layout.setOnExpansionUpdateListener(this)

        holder.itemView.expandable_layout.isSelected = isSelected
        holder.itemView.expandable_layout.setExpanded(isSelected, false)
        holder.itemView.arrow.setOnClickListener {
            (context as FamilyLocatorActivity).mapLayout.visibility = View.GONE
            if (holder.itemView.expandable_layout.isExpanded) {
                holder.itemView.expandable_layout.collapse()
                Glide.with(context).load(R.drawable.ic_baseline_expand_more_24)
                    .into(holder.itemView.arrow)

            } else {

                holder.itemView.expandable_layout.expand()
                Glide.with(context).load(R.drawable.ic_baseline_expand_less_24)
                    .into(holder.itemView.arrow)
                selectedItem = position
            }
        }
        holder.itemView.viewLayout.setOnClickListener {
            (context as FamilyLocatorActivity).mapLayout.visibility = View.GONE
            if (holder.itemView.expandable_layout.isExpanded) {
                holder.itemView.expandable_layout.collapse()
                Glide.with(context).load(R.drawable.ic_baseline_expand_more_24)
                    .into(holder.itemView.arrow)

            } else {

                holder.itemView.expandable_layout.expand()
                Glide.with(context).load(R.drawable.ic_baseline_expand_less_24)
                    .into(holder.itemView.arrow)
                selectedItem = position
            }
        }
        holder.itemView.expandable_layout.lockPhone.setOnClickListener {
            (context as FamilyLocatorActivity).mapLayout.visibility = View.GONE
            if (InternetConnection().checkConnection(context)) {
                sendLostPhoneRequest(
                    familyLocator.uid.toString(),
                    familyLocator.macAddress,
                    "lockPhone",
                    "1",
                    context.getString(R.string.locking_the_device)
                )
            } else {
                (context as BaseActivity).showToast(context.getString(R.string.no_internet))
            }
        }
        holder.itemView.expandable_layout.customMessage.setOnClickListener {
            (context as FamilyLocatorActivity).mapLayout.visibility = View.GONE

            if (!SharedPrefUtils.getBooleanData(context, "hideAds")) {
                if ((context as BaseActivity).interstitial.isLoaded) {
                    context.interstitial.show()
                } else {
                    if (InternetConnection().checkConnection(context)) {
                        showCustomMessage(familyLocator)

                    } else {
                        (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                    }
                }
                context.interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        context.requestNewInterstitial()
                        if (InternetConnection().checkConnection(context)) {
                            showCustomMessage(familyLocator)

                        } else {
                            (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                        }
                    }
                }
            } else {
                if (InternetConnection().checkConnection(context)) {
                    showCustomMessage(familyLocator)

                } else {
                    (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                }
            }
        }
        holder.itemView.expandable_layout.ringPhone.setOnClickListener {
            (context as FamilyLocatorActivity).mapLayout.visibility = View.GONE

            if (!SharedPrefUtils.getBooleanData(context, "hideAds")) {
                if ((context as BaseActivity).interstitial.isLoaded) {
                    context.interstitial.show()
                } else {
                    if (InternetConnection().checkConnection(context)) {
                        sendLostPhoneRequest(
                            familyLocator.uid.toString(),
                            familyLocator.macAddress,
                            "ringPlay",
                            "1",
                            context.getString(R.string.ringing_the_device)
                        )
                    } else {
                        (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                    }
                }
                context.interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        context.requestNewInterstitial()
                        if (InternetConnection().checkConnection(context)) {
                            sendLostPhoneRequest(
                                familyLocator.uid.toString(),
                                familyLocator.macAddress,
                                "ringPlay",
                                "1",
                                context.getString(R.string.ringing_the_device)
                            )
                        } else {
                            (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                        }
                    }
                }
            } else {
                if (InternetConnection().checkConnection(context)) {
                    sendLostPhoneRequest(
                        familyLocator.uid.toString(),
                        familyLocator.macAddress,
                        "ringPlay",
                        "1",
                        context.getString(R.string.ringing_the_device)
                    )
                } else {
                    (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                }
            }

        }
        if (familyLocator.latitude.isEmpty() || familyLocator.latitude.equals(
                "null",
                true
            ) || familyLocator.latitude == "0.0" && familyLocator.longitude.isEmpty() || familyLocator.longitude.equals(
                "null",
                true
            ) || familyLocator.longitude == "0.0"
        ) {
            holder.itemView.expandable_layout.lastUpdated.visibility = View.GONE
        } else {
            holder.itemView.expandable_layout.lastUpdated.visibility = View.VISIBLE

            holder.itemView.expandable_layout.lastUpdated.text =
                "Last Updated at: " + familyLocator.updateDate
        }
        holder.itemView.expandable_layout.locateDevice.setOnClickListener {
            if (InternetConnection().checkConnection(context)) {
                if (familyLocator.latitude.isEmpty() || familyLocator.latitude.equals(
                        "null",
                        true
                    ) || familyLocator.latitude == "0.0" && familyLocator.longitude.isEmpty() || familyLocator.longitude.equals(
                        "null",
                        true
                    ) || familyLocator.longitude == "0.0"
                ) {
                    (context as BaseActivity).showToast("This member location is off or not set properly")
                } else {
                    (context as FamilyLocatorActivity).initMap(
                        familyLocator.latitude,
                        familyLocator.longitude,
                        familyLocator.name
                    )
                }
            } else {
                (context as BaseActivity).showToast(context.getString(R.string.no_internet))
            }
        }
        holder.itemView.expandable_layout.shareLoc.isChecked =
            SharedPrefUtils.getBooleanData(context, "isTrackLoc1")
        holder.itemView.expandable_layout.shareLoc.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                showLocDialog(holder.itemView.expandable_layout.shareLoc)
            } else {
                cancelAlarm(holder.itemView.expandable_layout.shareLoc)
            }
        }
    }

    private fun showLocDialog(switchBtnLoc: SwitchMaterial) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        startReciver(switchBtnLoc)
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                        try {
                            switchBtnLoc.isChecked = false

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.MaterialAlertDialogTheme
        )
        builder.setTitle(context.getString(R.string.share_your_location))
            .setMessage(context.getString(R.string.share_location_in_background))
            .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
            .setNegativeButton(context.getString(R.string.no), dialogClickListener)
            .setCancelable(false)
            .show()
    }

    //TODO: start reciever
    private fun startReciver(switchBtnLoc: SwitchMaterial) {
        val intent = Intent(context, ShareLocReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1035, intent, 0)
        //60000 equal to 1 minute
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager
            .set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 60000,
                pendingIntent
            )//180000
        SharedPrefUtils.saveData(context, "isTrackLoc1", true)
        switchBtnLoc.isChecked = true
    }

    private fun cancelAlarm(switchBtnLoc: SwitchMaterial) {
        val intent = Intent(context, ShareLocReceiver::class.java)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent =
            PendingIntent.getBroadcast(
                context, 1035, intent,
                PendingIntent.FLAG_NO_CREATE
            )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            SharedPrefUtils.saveData(context, "isTrackLoc1", false)
            switchBtnLoc.isChecked = false
            Log.d(TAGI, "onReceive: Alarm cancel!")
        }
    }

    @SuppressLint("InflateParams")
    private fun showCustomMessage(familyLocator: FamilyLocator) {
        val factory = LayoutInflater.from(context)
        deleteDialogView =
            factory.inflate(R.layout.custom_message_layout, null)
        val deleteDialog: AlertDialog =
            MaterialAlertDialogBuilder(context).create()
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setView(deleteDialogView)
        deleteDialog.setCancelable(false)
        deleteDialogView!!.send.setOnClickListener {
            if (deleteDialogView!!.contactNum.text!!.isEmpty() ||
                deleteDialogView!!.customMessage1.text!!.isEmpty()
            ) {
                (context as BaseActivity).showToast(context.getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(context)) {
                    sendLastHopeRequest(
                        familyLocator.uid.toString(),
                        familyLocator.macAddress,
                        deleteDialogView!!.contactNum.text!!.toString(),
                        deleteDialogView!!.customMessage1.text!!.toString(),
                        context.getString(R.string.sending_notification)
                    )
                    deleteDialogView!!.contactNum.text!!.clear()
                    deleteDialogView!!.customMessage1.text!!.clear()
                    deleteDialog.dismiss()
                } else {
                    (context as BaseActivity).showToast(context.getString(R.string.no_internet))
                }
            }
        }
        deleteDialogView!!.cancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
        deleteDialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }

    //TODO: send fcm to other devices using last hope
    private fun sendLastHopeRequest(
        uid: String?,
        macAddress: String,
        title: String,
        message: String,
        loadingMessage: String
    ) {
        (context as BaseActivity).showDialog(loadingMessage)
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(context.mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.sendLastHope(
            uid.toString(),
            macAddress,
            title,
            message,
            object : Callback<Response> {
                override fun success(result: Response, response: Response) {
                    //On success we will read the server's output using bufferedreader
                    //Creating a bufferedreader object
                    val reader: BufferedReader?

                    //An string to store output from the server
                    val output: String

                    try {
                        //Initializing buffered reader
                        reader = BufferedReader(InputStreamReader(result.body.`in`()))

                        //Reading the output in the string
                        output = reader.readLine()
                        Log.d(TAGI, "msg: $output")
                        val msg = output.replace("\"", "")
                        context.showToast(msg)
                        context.hideDialog()
                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        context.hideDialog()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                    context.hideDialog()
                }
            }
        )
    }

    override fun onExpansionUpdate(expansionFraction: Float, state: Int) {
        Log.d("ExpandableLayout", "State: $state")
        /* if (state == ExpandableLayout.State.EXPANDING) {
             recyclerView.smoothScrollToPosition();

         }*/
    }

    //TODO: send fcm to other devices
    private fun sendLostPhoneRequest(
        uid: String?,
        macAddress: String,
        title: String,
        message: String,
        loadingMessage: String
    ) {
        (context as BaseActivity).showDialog(loadingMessage)
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(context.mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.sendNotification(
            uid.toString(),
            macAddress,
            title,
            message,
            object : Callback<Response> {
                override fun success(result: Response, response: Response) {
                    //On success we will read the server's output using bufferedreader
                    //Creating a https://www.youtube.com/watch?v=n6R-sc2QMVMbufferedreader object
                    val reader: BufferedReader?

                    //An string to store output from the server
                    val output: String

                    try {
                        //Initializing buffered reader
                        reader = BufferedReader(InputStreamReader(result.body.`in`()))

                        //Reading the output in the string
                        output = reader.readLine()
                        Log.d(TAGI, "msg: $output")
                        val msg = output.replace("\"", "")
                        context.showToast(msg)
                        context.hideDialog()
                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        context.hideDialog()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                    context.hideDialog()
                }
            }
        )
    }

}
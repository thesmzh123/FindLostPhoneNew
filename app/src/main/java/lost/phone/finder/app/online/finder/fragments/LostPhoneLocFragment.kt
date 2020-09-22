package lost.phone.finder.app.online.finder.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_lost_phone_loc.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.models.MapView
import lost.phone.finder.app.online.finder.receivers.UpdateReceiver
import lost.phone.finder.app.online.finder.utils.Constants.TAGI

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class LostPhoneLocFragment : BaseFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_lost_phone_loc, container, false)
        arrayList = ArrayList()
        root!!.titleText.text =
            getString(R.string.locate_my_phone)
        Glide.with(requireActivity()).load(R.drawable.loccell).into(root!!.imageView)
        root!!.mainBtn.text = getString(R.string.view_on_map)
        root!!.profile.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.nav_profile)
        }
        root!!.switchClick.visibility = View.VISIBLE
        root!!.switchText1.text = getString(R.string.track_your_loc)
        root!!.mainBtn.setOnClickListener {
            if (InternetConnection().checkConnection(requireActivity())) {
                if (adapter!!.getMultiSelectionDevice().size > 0) {
                    for (i in 0 until adapter!!.getMultiSelectionDevice().size) {
                        Log.d(TAGI, "name: " + adapter!!.getMultiSelectionDevice()[i].deviceName)
                        arrayList!!.add(
                            MapView(
                                adapter!!.getMultiSelectionDevice()[i].lat,
                                adapter!!.getMultiSelectionDevice()[i].longi,
                                adapter!!.getMultiSelectionDevice()[i].deviceName
                            )
                        )
                    }
                    openMapView()
                } else {
                    showToast(getString(R.string.please_select_option))
                }
            } else {
                showToast(getString(R.string.no_internet))
            }
        }
        root!!.switchBtnLoc.isChecked =
            SharedPrefUtils.getBooleanData(requireActivity(), "isTrackLoc")
        root!!.switchBtnLoc.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                showLocDialog()
            } else {
                cancelAlarm()
            }
        }
        return root!!
    }

    private fun showLocDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        startReciver()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                        try {
                            root!!.switchBtnLoc.isChecked = false

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MaterialAlertDialogTheme
        )
        builder.setTitle(getString(R.string.track_your_loc))
            .setMessage(getString(R.string.track_location_in_background))
            .setPositiveButton(getString(R.string.yes), dialogClickListener)
            .setNegativeButton(getString(R.string.no), dialogClickListener).setCancelable(false)
            .show()
    }

    //TODO: start reciever
    private fun startReciver() {
        val intent = Intent(requireActivity(), UpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireActivity(), 103, intent, 0)
        //60000 equal to 1 minute
        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager
            .set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 180000,
                pendingIntent
            )//180000
        SharedPrefUtils.saveData(requireActivity(), "isTrackLoc", true)
        root!!.switchBtnLoc.isChecked = true
    }

    private fun cancelAlarm() {
        val intent = Intent(requireActivity(), UpdateReceiver::class.java)
        val alarmManager =
            requireActivity().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val pendingIntent =
            PendingIntent.getBroadcast(
                requireActivity(), 103, intent,
                PendingIntent.FLAG_NO_CREATE
            )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            SharedPrefUtils.saveData(requireActivity(), "isTrackLoc", false)
            root!!.switchBtnLoc.isChecked = false
            Log.d(TAGI, "onReceive: Alarm cancel!")
        }
    }

    private fun init() {
        if (isLoggedIn()) {

            root?.recyclerView!!.visibility = View.VISIBLE
            root?.signInBlock!!.visibility = View.GONE
            root!!.mainBtn.visibility = View.VISIBLE
            loadAllData(
                root!!
            )
        } else {
            root?.signInBlock!!.visibility = View.VISIBLE
            root?.recyclerView!!.visibility = View.GONE
            root!!.mainBtn.visibility = View.GONE

        }
    }


    override fun onResume() {
        super.onResume()
        try {
            init()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openMapView() {
        val bundle = bundleOf("mapView" to arrayList)

        findNavController().navigate(R.id.mapViewFragment, bundle)
    }
}
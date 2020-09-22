@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package lost.phone.finder.app.online.finder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_lost_phone_loc.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import org.json.JSONArray
import org.json.JSONObject


class RingPhoneFragment : BaseFragment() {
    private var ringSilent: Boolean = false
    private var ringPhone: Boolean = false
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
        root!!.titleText.text =
            getString(R.string.alarm_my_phone)
        Glide.with(requireActivity()).load(R.drawable.ringcell).into(root!!.imageView)
        root!!.mainBtn.text = getString(R.string.ring_this_mobile)
        root!!.profile.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.nav_profile)
        }
        loadInterstial()
        root!!.ringBlock.visibility = View.VISIBLE
        root!!.mainBtn.setOnClickListener {
            if (!SharedPrefUtils.getBooleanData(requireActivity(), "hideAds")) {
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    ringMobile()

                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        ringMobile()
                    }
                }
            } else {
                ringMobile()


            }

        }
        root!!.ring_silent_phone.isChecked = true
        ringSilent = true
        root!!.radio.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.ring_silent_phone -> {
                    ringPhone = false
                    ringSilent = true
                }
                R.id.ring_phone -> {
                    ringPhone = true
                    ringSilent = false
                }

            }
        }
        return root
    }

    private fun ringMobile() {
        jsonArray = JSONArray()
        if (InternetConnection().checkConnection(requireActivity())) {
            if (adapter!!.getMultiSelectionDevice().size > 0) {
                if (ringPhone) {
                    sendRingNotification("ringSilent")
                } else if (ringSilent) {
                    sendRingNotification("ringPlay")

                }
            } else {
                showToast(getString(R.string.please_select_option))
            }
        } else {
            showToast(getString(R.string.no_internet))
        }
    }

    private fun sendRingNotification(ring: String) {
        for (i in 0 until adapter!!.getMultiSelectionDevice().size) {
            Log.d(TAGI, "name: " + adapter!!.getMultiSelectionDevice()[i].deviceName)
            jsonObject = JSONObject()
            jsonObject!!.put(
                "uid",
                SharedPrefUtils.getStringData(requireActivity(), "uid")
            )
            jsonObject!!.put(
                "token",
                adapter!!.getMultiSelectionDevice()[i].token
            )
            jsonObject!!.put("title", ring)
            jsonObject!!.put("message", "1")
            jsonArray?.put(jsonObject)
        }
        sendMultipleRequest(jsonArray, getString(R.string.ringing_the_device))
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
}
package lost.phone.finder.app.online.finder.fragments

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_lost_phone_loc.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.MainActivity
import lost.phone.finder.app.online.finder.utils.Constants
import org.json.JSONArray
import org.json.JSONObject


class HideAppFragment : BaseFragment() {
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
        switchLock = root!!.switchBtnLoc
        root!!.titleText.text =
            getString(R.string.with_this_feature_you_can_remotely_hide_your_phone)
        Glide.with(requireActivity()).load(R.drawable.hidecell).into(root!!.imageView)
        root!!.mainBtn.text = getString(R.string.hide_app)
        root!!.profile.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.nav_profile)
        }
        root!!.switchClick.visibility = View.VISIBLE
        root!!.switchText1.text = getString(R.string.stealth_mode)
        root!!.mainBtn.setOnClickListener {
            jsonArray = JSONArray()

            if (InternetConnection().checkConnection(requireActivity())) {
                if (adapter!!.getMultiSelectionDevice().size > 0) {
                    for (i in 0 until adapter!!.getMultiSelectionDevice().size) {
                        Log.d(
                            Constants.TAGI,
                            "name: " + adapter!!.getMultiSelectionDevice()[i].deviceName
                        )
                        jsonObject = JSONObject()
                        jsonObject!!.put(
                            "uid",
                            SharedPrefUtils.getStringData(requireActivity(), "uid")
                        )
                        jsonObject!!.put(
                            "token",
                            adapter!!.getMultiSelectionDevice()[i].token
                        )
                        jsonObject!!.put("title", "lockPhone")
                        jsonObject!!.put("message", "1")
                        jsonArray?.put(jsonObject)
                    }
                    sendMultipleRequest(jsonArray, getString(R.string.hiding_the_icon))
                } else {
                    showToast(getString(R.string.please_select_option))
                }
            } else {
                showToast(getString(R.string.no_internet))
            }

        }

        root!!.switchBtnLoc.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                val pkg: PackageManager = requireActivity().getPackageManager()
                pkg.setComponentEnabledSetting(
                    ComponentName(requireActivity(), MainActivity::class.java),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )

            } else {

            }
        }
        return root
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
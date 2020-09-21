package lost.phone.finder.app.online.finder.fragments

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
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import org.json.JSONArray
import org.json.JSONObject

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class LockPhoneFragment : BaseFragment() {

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
            getString(R.string.secure_my_phone)
        Glide.with(requireActivity()).load(R.drawable.lockcell).into(root!!.imageView)
        root!!.mainBtn.text = getString(R.string.lock_this_mobile)
        root!!.profile.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.nav_profile)
        }
        root!!.switchClick.visibility = View.VISIBLE
        root!!.switchText1.text = getString(R.string.lock_your_phone)
        root!!.mainBtn.setOnClickListener {
            jsonArray = JSONArray()

            if (InternetConnection().checkConnection(requireActivity())) {
                if (adapter!!.getMultiSelectionDevice().size > 0) {
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
                        jsonObject!!.put("title", "lockPhone")
                        jsonObject!!.put("message", "1")
                        jsonArray?.put(jsonObject)
                    }
                    sendMultipleRequest(jsonArray, getString(R.string.locking_the_device))
                } else {
                    showToast(getString(R.string.please_select_option))
                }
            } else {
                showToast(getString(R.string.no_internet))
            }

        }
        if (requireArguments().getBoolean("isActivate")) {
            if (isDeviceAdmin()) {


                root!!.switchBtnLoc.isChecked =
                    SharedPrefUtils.getBooleanData(requireActivity(), "isDevice")

            } else {
                root!!.switchBtnLoc.isChecked = false
            }
        }
        root!!.switchBtnLoc.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                activateAdmin(root!!.switchBtnLoc)
            } else {
                if (dpm.isAdminActive(mDeviceAdminSample)) {
                    dpm.removeActiveAdmin(mDeviceAdminSample)

                    root!!.switchBtnLoc.isChecked = false
                    SharedPrefUtils.saveData(requireActivity(), "isDevice", false)
                    showToast(getString(R.string.admin_is_deactive))
                }
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
                root!!,
                SharedPrefUtils.getStringData(requireActivity(), "base_url").toString()
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
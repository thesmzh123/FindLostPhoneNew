package device.spotter.finder.appss.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import device.spotter.finder.appss.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_lost_phone_loc.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.utils.Constants
import org.json.JSONArray
import org.json.JSONObject

class LastHopeFragment : BaseFragment() {
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
            getString(R.string.send_message)
        Glide.with(requireActivity()).load(R.drawable.messagecell).into(root!!.imageView)
        root!!.mainBtn.text = getString(R.string.send_message)
        root!!.profile.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.nav_profile)
        }
        root!!.textLayout.visibility = View.VISIBLE
        root!!.mainBtn.setOnClickListener {
            jsonArray = JSONArray()
                if (adapter!!.getMultiSelectionDevice().size > 0) {
                    if (TextUtils.isEmpty(root!!.contactNum.text) || TextUtils.isEmpty(root!!.customMessage.text)) {
                        showToast(getString(R.string.fill_the_field))
                    } else {
                        if (InternetConnection().checkConnection(requireActivity())) {
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
                            jsonObject!!.put("title", "hope")
                            jsonObject!!.put(
                                "message",
                                root!!.contactNum.text.toString() + "," + root!!.customMessage.text
                            )
                            jsonArray?.put(jsonObject)
                        }
                        sendMultipleRequest(jsonArray, getString(R.string.sending_message))
                        root!!.contactNum.text!!.clear()
                        root!!.customMessage.text!!.clear()
                    } else {
                        showToast(getString(R.string.no_internet))
                    }
                    }
                } else {
                    showToast(getString(R.string.please_select_option))
                }


        }
        baseContext!!.adView(root!!.adView)

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
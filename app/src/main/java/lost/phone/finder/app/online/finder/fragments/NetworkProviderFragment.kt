@file:Suppress("LocalVariableName", "UNUSED_ANONYMOUS_PARAMETER")

package lost.phone.finder.app.online.finder.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_network_provider.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import org.json.JSONObject


class NetworkProviderFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_network_provider, container, false)
        root!!.titleText.text = getString(R.string.newtork_provider)
        Glide.with(requireActivity()).load(R.drawable.networkcell).into(root!!.imageView)

        root!!.ccp.registerCarrierNumberEditText(root!!.editText_carrierNumber)

        root!!.mainBtn.setOnClickListener {
            checkNumber()
        }
        root!!.editText_carrierNumber!!.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                checkNumber()
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }
        baseContext!!.adView(root!!.adView)
        return root
    }

    private fun checkNumber() {
        if (TextUtils.isEmpty(root!!.editText_carrierNumber.text)) {
            showToast(getString(R.string.fill_the_field))
        } else {
            hideKeyboard()
            showDialog(getString(R.string.fetching))
            val countryName: String = root!!.ccp.selectedCountryName.toString()
            val code: String = root!!.ccp.selectedCountryCode
            val number = code + root!!.editText_carrierNumber.text
            fetchProvider(code, countryName, number)
        }
    }

    private fun hideKeyboard() {
        val imm =
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)!!
        imm.hideSoftInputFromWindow(root!!.editText_carrierNumber!!.windowToken, 0)
    }

    @SuppressLint("SetTextI18n")
    fun fetchProvider(code: String, sCountryName: String, number: String) {
        var operator = ""
        try {

            val obj = JSONObject(loadJSONFromAsset())
            val m_jArry = obj.getJSONArray("ref_operator_codes")

            for (i in 0 until m_jArry.length()) {
                val jo_inside = m_jArry.getJSONObject(i)
                val countryName = jo_inside.getString("CountryName").replace(" ", "")
                if (sCountryName.equals(countryName, true)) {

                    val getOperator = jo_inside.getString("OperatorCode")
                    val operatorForSearch: String =
                        number.replace(code, "").substring(0, 2)
                    if (getOperator.contains(operatorForSearch)) {
                        operator = jo_inside.getString("OperatorName")
                        root!!.networkProvider.text =
                            getString(R.string.network_p) + " " + operator
                        break
                    } else {
                        root!!.networkProvider.text = getString(R.string.no_network_provider)
                    }
                } else {
                    root!!.networkProvider.text = getString(R.string.no_network_provider)
                }
            }
            Log.d(TAGI, "fetchProvider: $operator")
            hideDialog()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
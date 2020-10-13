@file:Suppress("DEPRECATION")

package device.spotter.finder.appss.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.*
import android.text.Editable
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.iid.FirebaseInstanceId
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.BaseActivity
import device.spotter.finder.appss.activities.MainActivity
import device.spotter.finder.appss.adapters.DevicesAdapter
import device.spotter.finder.appss.models.Devices
import device.spotter.finder.appss.models.MapView
import device.spotter.finder.appss.receivers.DeviceAdminSample
import device.spotter.finder.appss.utils.Constants.DEVICE_ADMIN_CODE
import device.spotter.finder.appss.utils.Constants.REQUEST_CHECK_SETTINGS_GPS
import device.spotter.finder.appss.utils.Constants.REQUEST_ID_MULTIPLE_PERMISSIONS
import device.spotter.finder.appss.utils.Constants.SEND_MULTIPLE_REQUEST
import device.spotter.finder.appss.utils.Constants.TAGI
import device.spotter.finder.appss.utils.GPSTracker
import device.spotter.finder.appss.utils.RegisterAPI
import kotlinx.android.synthetic.main.ad_unified.view.*
import kotlinx.android.synthetic.main.enter_phone_num_layout.view.*
import kotlinx.android.synthetic.main.enter_phone_num_otp_layout.view.*
import kotlinx.android.synthetic.main.fragment_lost_phone_loc.view.*
import kotlinx.android.synthetic.main.fragment_network_provider.view.*
import kotlinx.android.synthetic.main.layout_loading_dialog.view.*
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER")
open class BaseFragment : Fragment(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener {
    var root: View? = null
    lateinit var auth: FirebaseAuth
    lateinit var auth1: FirebaseAuth
    var mainContext: MainActivity? = null
    var baseContext: BaseActivity? = null
    private var dialog: AlertDialog? = null
    var mainUrl: String? = null
    private var mylocation: Location? = null
    private var googleApiClient: GoogleApiClient? = null
    var adapter: DevicesAdapter? = null
    var arrayList: ArrayList<MapView>? = null
    lateinit var mDeviceAdminSample: ComponentName
    lateinit var dpm: DevicePolicyManager
    var switchLock: SwitchMaterial? = null

    private lateinit var alertDialogBuilderUserInput: AlertDialog.Builder
    private lateinit var alertDialog2: AlertDialog
    var jsonArray: JSONArray? = null
    var jsonObject: JSONObject? = null
    var gpsTracker: GPSTracker? = null

    lateinit var interstitial: InterstitialAd
    private var verificationId: String? = null
    var getNum: String? = null
    var deleteDialog: AlertDialog? = null
    var otpDialog: AlertDialog? = null
    var otpDialogView: View? = null
    private var isRecent: Boolean = false
    private var isFinish: Boolean = false
    private var cdt: CountDownTimer? = null
    var layoutNumber: LinearLayout? = null
    var num: MaterialTextView? = null

    //TODO: load interstial
    fun loadInterstial() {
        try {

            Log.d(TAGI, "load ads")
            if (!SharedPrefUtils.getBooleanData(requireActivity(), "hideAds")) {
                interstitial = InterstitialAd(requireActivity())
                interstitial.adUnitId = getString(R.string.interstitial)
                try {
                    if (!interstitial.isLoading && !interstitial.isLoaded) {
                        val adRequest = AdRequest.Builder().build()
                        interstitial.loadAd(adRequest)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Log.d(TAGI, "error: " + ex.message)
                }

                requestNewInterstitial()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: requestNewInterstitial
    fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder().build()
        interstitial.loadAd(adRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        super.onCreate(savedInstanceState)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)
        auth = FirebaseAuth.getInstance()
        auth1 = FirebaseAuth.getInstance()
        mainContext = (requireActivity() as MainActivity)
        baseContext = (requireActivity() as BaseActivity)
        gpsTracker = GPSTracker(requireActivity())
        try {
            mainUrl = SharedPrefUtils.getStringData(requireActivity(), "base_url")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAGI, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token.toString()

                // Log and toast
                Log.d(TAGI, token)
                if (isAdded) {
                    SharedPrefUtils.saveData(requireActivity(), "deviceToken", token)
                }
            })
        setUpGClient()
        mDeviceAdminSample = ComponentName(requireActivity(), DeviceAdminSample::class.java)
        dpm =
            requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        loadInterstial()
        if (isLoggedIn()) {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                FirebaseAuth.getInstance().signOut()
            }
        }

    }

    fun openProfileNUmDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        baseContext!!.navigateFragment(R.id.nav_profile)
                        dialog.dismiss()
                    }

                }
            }

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MaterialAlertDialogTheme
        )
        builder.setMessage(getString(R.string.not_enter_phone_num))
            .setPositiveButton(getString(R.string.ok), dialogClickListener).show()
    }

    //TODO: check if user is logged in
    fun isLoggedIn(): Boolean {
        return if (auth.currentUser != null) {
            Log.d(TAGI, "ok")
            true
        } else {
            Log.d(TAGI, "cancel")
            false
        }
    }

    fun showToast(toast: String) {
        Toast.makeText(requireActivity(), toast, Toast.LENGTH_LONG).show()

    }

    //TODO: show dialog
    fun showDialog(message: String) {
        dialog = setProgressDialog(message)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    //TODO: hide dialog
    fun hideDialog() {
        if (dialog?.isShowing!!) {
            dialog?.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    private fun setProgressDialog(message: String): AlertDialog {

        val builder = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MaterialAlertDialogTheme
        )
        builder.setCancelable(false)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.layout_loading_dialog, null)
        builder.setView(view)

        view.dialogText.text = message
        return builder.create()
    }

    //TODO: insert profile to db
    fun insertDataToDb(displayName: String?, email: String?) {
        try {
            val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
            val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
            api.insertUser(

                //Passing the values by getting it from editTexts

                displayName.toString(),
                email.toString(),
                baseContext!!.getMacAddres(),
                object : Callback<Response> {
                    @SuppressLint("SetTextI18n")
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
                            Log.d(TAGI, "user: $output")
                            val jsonObject = JSONObject(output)
                            val id = jsonObject.getString("id")
                            val phoneNum = jsonObject.getString("phone_num")
                            val isNumberFound = jsonObject.getBoolean("isNumberFound")
                            val messagePhone = jsonObject.getString("messagePhone")
                            var isDevice = false
                            if (isNumberFound) {
                                isDevice = jsonObject.getBoolean("isDevice")
                            }
                            SharedPrefUtils.saveData(requireActivity(), "uid", id)
                            SharedPrefUtils.saveData(requireActivity(), "phoneNum", phoneNum)
                            layoutNumber!!.visibility = View.VISIBLE
                            num!!.text = "Your number is $phoneNum"
                            if (isNumberFound) {

                                if (isDevice) {
                                    showNumAddDialog(messagePhone)
                                    baseContext!!.registerDevice(id)
                                    hideDialog()

                                }
                                /*    if (!SharedPrefUtils.getBooleanData(
                                            requireActivity(),
                                            "isInserted"
                                        )
                                    ) {
                                        hideDialog()
                                        replaceNumberDialog()
                                    }*/

                            } else {
                                hideDialog()
                                enterNumberDialog()

                            }
//                            registerDevice(id)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hideDialog()
                        }

                    }

                    override fun failure(error: RetrofitError) {
                        Log.d(TAGI, error.toString())
                        hideDialog()
                        showToast(getString(R.string.error_occured_try_again))
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            hideDialog()
        }

    }

    private fun showNumAddDialog(messagePhone: String) {
        val builder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialogTheme)
        builder.setTitle("Your mobile device is " + Build.BRAND + ", " + Build.MODEL)
        builder.setMessage(messagePhone)
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.ok)
        ) { dialog, which -> // Do do my action here
            dialog.dismiss()
        }


        val alert = builder.create()
        alert.show()
    }

    private fun replaceNumberDialog() {
        val builder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialogTheme)
        builder.setTitle("A device with this email is already registered with another phone number.")
        builder.setMessage("Do you want to replace your previous device with this device OR press No button to add a new device by adding a new phone number?\n\n Note: Adding the same will replace your old with new device")
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which -> // Do do my action here
            showDialog("Replacing device...")
            replaceDevice()
            dialog.dismiss()
        }

        builder.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> // I do not need any action here you might
            dialog.dismiss()
            enterNumberDialog()

        }
        builder.setNeutralButton(
            getString(R.string.not_now)
        ) { dialog, which -> // I do not need any action here you might
            dialog.dismiss()
            SharedPrefUtils.saveData(requireActivity(), "isInserted", true)

        }

        val alert = builder.create()
        alert.show()
    }

    private fun replaceDevice() {
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.replaceDevice(
            Build.BRAND,
            SharedPrefUtils.getStringData(requireActivity(), "uid").toString(),
            Build.MODEL,
            activity?.let {
                SharedPrefUtils.getStringData(it, "deviceToken").toString()
            }.toString(),
            SharedPrefUtils.getStringData(requireActivity(), "lati").toString(),
            SharedPrefUtils.getStringData(requireActivity(), "longi").toString(),
            baseContext!!.getMacAddres(),
            lastUpdatedDate(),
            object : Callback<Response> {
                @SuppressLint("SetTextI18n")
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
                        Log.d(TAGI, "device: $output")
//                        val jsonOb = JSONObject(output)
//                        val deviceD = jsonOb.getString("0")
                        SharedPrefUtils.saveData(
                            requireActivity(),
                            "devicedata",
                            output
                        )

                        SharedPrefUtils.saveData(requireActivity(), "isInserted", true)
//                        val phone_num = jsonOb.getString("phone_num")
                        val phone_num =
                            SharedPrefUtils.getStringData(requireActivity(), "temp_num").toString()
                        SharedPrefUtils.saveData(requireActivity(), "phoneNum", phone_num)

                        layoutNumber!!.visibility = View.VISIBLE
                        num!!.text = "Your number is $phone_num"
                        hideDialog()


                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        hideDialog()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                    hideDialog()
                }
            }
        )
    }

    //TODO: register device
    fun registerDevice(id: String, pid: String) {

        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.insertDeviceReg(
            Build.BRAND,
            id,
            Build.MODEL,
            activity?.let {
                SharedPrefUtils.getStringData(it, "deviceToken").toString()
            }.toString(),
            SharedPrefUtils.getStringData(requireActivity(), "lati").toString(),
            SharedPrefUtils.getStringData(requireActivity(), "longi").toString(),
            baseContext!!.getMacAddres(),
            lastUpdatedDate(),
            pid,
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
                        Log.d(TAGI, "device: $output")

                        SharedPrefUtils.saveData(requireActivity(), "devicedata", output)

                        hideDialog()


                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        hideDialog()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                    hideDialog()
                }
            }
        )
    }

    private fun enterNumberDialog() {
        val factory = LayoutInflater.from(requireActivity())
        @SuppressLint("InflateParams") val deleteDialogView: View =
            factory.inflate(R.layout.enter_phone_num_layout, null)
        deleteDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(requireActivity()).create()
        } else {
            AlertDialog.Builder(requireActivity()).create()
        }

        deleteDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog!!.setView(deleteDialogView)
        deleteDialog!!.setCancelable(false)
//        deleteDialogView.ccp1.registerCarrierNumberEditText(deleteDialogView.editText_carrierNumber1)

        deleteDialogView.mainBtnNUm.setOnClickListener {
            if (TextUtils.isEmpty(deleteDialogView.editText_carrierNumber1.text)) {
                showToast(getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(requireActivity())) {
//                    if (!SharedPrefUtils.getBooleanData(requireActivity(), "isInserted")) {
                    hideKeyboard(deleteDialogView.editText_carrierNumber1)
                    showDialog(getString(R.string.sending_you_verification_code))
                    getNum =
                        deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString()
                    SharedPrefUtils.saveData(requireActivity(), "temp_num", getNum!!)
//                        sendVerificationCode(deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString())

                    updatePhoneNumber(getNum.toString())
//                    }
                    deleteDialog!!.dismiss()
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }

        deleteDialog!!.show()
        deleteDialog!!.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }

    private fun hideKeyboard(text: TextInputEditText?) {
        val imm =
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)!!
        imm.hideSoftInputFromWindow(text!!.windowToken, 0)
    }

    fun sendVerificationCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+" + number,
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallBack
        )
    }

    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
                deleteDialog!!.dismiss()
                hideDialog()
                showOTPDialog()
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code: String = phoneAuthCredential.getSmsCode().toString()
                Log.d(TAGI, "onVerificationCompleted: " + code)
                otpDialogView!!.editText_carrierNumber2.setText(code)
                verifyCode(code)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d(TAGI, "onVerificationFailed: " + e.message)
//                otpDialog!!.dismiss()
                hideDialog()
                showOTpErrorDialog()

            }

        }

    private fun showOTpErrorDialog() {
        val builder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialogTheme)
        builder.setTitle("OTP Error!")
        builder.setMessage("Some error occured while generating OTP or Your Quota of OTP for this number has expired!\n\n Please try again by re-entering the number.")
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which -> // Do do my action here
            enterNumberDialog()
            dialog.dismiss()
        }

        builder.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> // I do not need any action here you might
            FirebaseAuth.getInstance().signOut()
            baseContext!!.navigateFragment(R.id.nav_profile)
            mainContext!!.updateNavView()
            baseContext!!.changeMenu()
            dialog.dismiss()

        }

        val alert = builder.create()
        alert.show()
    }

    @SuppressLint("InflateParams")
    private fun showOTPDialog() {
        isFinish = false
        isRecent = false
        val factory = LayoutInflater.from(requireActivity())
        otpDialogView = factory.inflate(R.layout.enter_phone_num_otp_layout, null)
        otpDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(requireActivity()).create()
        } else {
            AlertDialog.Builder(requireActivity()).create()
        }

        otpDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        otpDialog!!.setView(otpDialogView)
        otpDialog!!.setCancelable(false)
        cdt = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                Log.i(
                    TAGI,
                    "Countdown seconds remaining: " + millisUntilFinished / 1000
                )
                val min = millisUntilFinished / 60000
                val sec = millisUntilFinished % 60000 / 1000
                var timeInText: String?
                timeInText = "" + min
                timeInText += ":"
                if (sec < 10) {
                    timeInText += "0"
                }
                timeInText += sec
                otpDialogView!!.countTime.text = "( $timeInText )"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                isFinish = true
                otpDialogView!!.countTime.text = "( 0:00 )"
                Log.i(TAGI, "Timer finished")
                if (!isRecent) {
                    showToast("OTP Verification timed out! \n Please try again by clicking resend button!")
                }
            }
        }

        cdt!!.start()
//        deleteDialogView.ccp1.registerCarrierNumberEditText(deleteDialogView.editText_carrierNumber1)

        otpDialogView!!.mainBtn1.setOnClickListener {
            if (TextUtils.isEmpty(otpDialogView!!.editText_carrierNumber2.text)) {
                showToast(getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(requireActivity())) {
                    showDialog(getString(R.string.verifying_code))
                    val code = otpDialogView!!.editText_carrierNumber2.text.toString()
                    verifyCode(code)
//                        updatePhoneNumber(deleteDialogView.editText_carrierNumber1.text.toString())

//                    otpDialog!!.dismiss()
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }
        otpDialogView!!.mainBtn2.setOnClickListener {
            if (isFinish) {
                cdt!!.cancel()
                isRecent = true
                showToast("Please re-enter the number to get the verification code (OTP).")
                otpDialog!!.dismiss()
                enterNumberDialog()
            } else {
                showToast("Timer is already running.\n Can't resend the OTP Code.")
            }
        }
        otpDialog!!.show()
        otpDialog!!.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth1.signInWithCredential(credential)
            .addOnCompleteListener(object :
                OnCompleteListener<AuthResult?> {
                override fun onComplete(@NonNull task: Task<AuthResult?>) {
                    if (task.isSuccessful()) {
                        hideDialog()
                        cdt!!.cancel()
                        otpDialog!!.dismiss()
                        showDialog(getString(R.string.saving_number))
                        updatePhoneNumber(getNum.toString())
                    } else {
//                        otpDialog!!.dismiss()
                        showToast(task.exception!!.message.toString())
                        hideDialog()
                    }
                }
            })
    }

    //TODO: last updated
    @SuppressLint("SimpleDateFormat")
    fun lastUpdatedDate(): String {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return sdf.format(Date())
    }

    @Synchronized
    fun setUpGClient() {
        try {
            googleApiClient = GoogleApiClient.Builder(requireActivity())
                .enableAutoManage(requireActivity(), 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
            googleApiClient?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onConnected(p0: Bundle?) {
        checkPermissions()
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.d(TAGI, "onConnectionSuspended")
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAGI, "onConnectionFailed")
    }

    private fun getMyLocation() {
        try {
            if (isAdded) {
                if (googleApiClient != null) {
                    if (googleApiClient!!.isConnected) {
                        val permissionLocation = ContextCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                            mylocation =
                                LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                            val locationRequest = LocationRequest()
                            locationRequest.interval = 3000
                            locationRequest.fastestInterval = 3000
                            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            val builder = LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                            builder.setAlwaysShow(true)
                            LocationServices.FusedLocationApi
                                .requestLocationUpdates(googleApiClient, locationRequest, this)
                            val result = LocationServices.SettingsApi
                                .checkLocationSettings(googleApiClient, builder.build())
                            result.setResultCallback { result ->
                                val status = result.status
                                when (status.statusCode) {
                                    LocationSettingsStatusCodes.SUCCESS -> {
                                        // All location settings are satisfied.
                                        // You can initialize location requests here.
                                        if (isAdded) {
                                            val permissionLocation = ContextCompat
                                                .checkSelfPermission(
                                                    requireContext(),
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                )
                                            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                                mylocation = LocationServices.FusedLocationApi
                                                    .getLastLocation(googleApiClient)
                                            }
                                        }
                                    }
                                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                        // Location settings are not satisfied.
                                        // But could be fixed by showing the user a dialog.
                                        try {
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            // Ask to turn on GPS automatically
                                            status.startResolutionForResult(
                                                requireActivity(),
                                                REQUEST_CHECK_SETTINGS_GPS
                                            )
                                        } catch (e: IntentSender.SendIntentException) {
                                            // Ignore the error.
                                        }

                                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                    }
                                }// Location settings are not satisfied.
                                // However, we have no way
                                // to fix the
                                // settings so we won't show the dialog.
                                // finish();
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            when (requestCode) {
                REQUEST_CHECK_SETTINGS_GPS -> when (resultCode) {
                    Activity.RESULT_OK -> getMyLocation()
                    Activity.RESULT_CANCELED -> {
                        //findNavController().navigate(R.id.nav_offline)
                        Log.d(TAGI, "cancelled")
                        //     activity!!.finish()
                    }
                }

            }
            if (resultCode == Activity.RESULT_OK && requestCode == DEVICE_ADMIN_CODE) {
                Log.d(TAGI, "ok")
                SharedPrefUtils.saveData(requireActivity(), "isDevice", true)
                switchLock!!.isChecked = true
                showToast(getString(R.string.admin_is_active))
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAGI, "cancel")
                switchLock!!.isChecked = false


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermissions() {
        try {
            val permissionLocation = activity?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            val listPermissionsNeeded = ArrayList<String>()
            if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
                if (listPermissionsNeeded.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS
                    )
                }
            } else {
                getMyLocation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onLocationChanged(location: Location) {
        try {
            mylocation = location
            if (mylocation != null) {
                val latitude = mylocation!!.latitude
                val longitude = mylocation!!.longitude
                /*            Log.d(TAGI, "Latitude : $latitude")
                            Log.d(TAGI, "Longitude : $longitude")*/
                //Or Do whatever you want with your location

                if (isAdded) {
                    SharedPrefUtils.saveData(requireActivity(), "lati", latitude.toString())
                    SharedPrefUtils.saveData(requireActivity(), "longi", longitude.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: load all device data
    fun loadAllData(view1: View) {
        try {
            val deviceList = ArrayList<Devices>()
            val jsonArray =
                JSONArray(this.activity?.let { SharedPrefUtils.getStringData(it, "devicedata") })
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d(TAGI, jsonObject.getString("devicename"))
                deviceList.add(
                    Devices(
                        jsonObject.getString("devicename"),
                        jsonObject.getString("model")
                        ,
                        jsonObject.getString("token"),
                        jsonObject.getString("latitude")
                        ,
                        jsonObject.getString("longitude"),
                        jsonObject.getString("mac_address"),
                        jsonObject.getString("updateDate")
                        , false
                    )
                )
            }
            //creating our adapter
            adapter = activity?.let { DevicesAdapter(it, deviceList) }

            //now adding the adapter to recyclerview
            val horizontalLayoutManagaer =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            view1.recyclerView.layoutManager = horizontalLayoutManagaer
            view1.recyclerView.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: check if device admin active
    fun isDeviceAdmin(): Boolean {
        return if (dpm.isAdminActive(mDeviceAdminSample)) {
            Log.d(TAGI, "true")
            true
        } else {
            Log.d(TAGI, "false")
            false
        }
    }

    //TODO: devie admin dialog
    @SuppressLint("InflateParams")
    fun deviceAdminDialog() {
        val yesNoDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            R.style.MaterialAlertDialogTheme
        )
        //yes or no alert box
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.device_check_layout, null)
        yesNoDialog.setView(view)
        yesNoDialog.setCancelable(false)
            .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()

            }
            .setPositiveButton(getString(R.string.ok)) { dialogInterface, i ->
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(
                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mDeviceAdminSample
                )
                intent.putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "For Erase data,Lock Phone and Password Change"
                )
                startActivityForResult(intent, DEVICE_ADMIN_CODE)
                dialogInterface.dismiss()
            }

        val dialog = yesNoDialog.create()

        dialog.show()
    }

    //Todo: device admin activeG
    @SuppressLint("InflateParams")
    fun activateAdmin(switch1: SwitchMaterial) {
        try {

            if (!dpm.isAdminActive(mDeviceAdminSample)) {

                val layoutInflaterAndroid = LayoutInflater.from(requireActivity())
                val view1 = layoutInflaterAndroid.inflate(R.layout.device_admin_layout, null)
                alertDialogBuilderUserInput =
                    MaterialAlertDialogBuilder(
                        requireActivity(),
                        R.style.MaterialAlertDialogTheme
                    )
                alertDialogBuilderUserInput.setView(view1)
                alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok)) { dialogBox, id ->
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            mDeviceAdminSample
                        )
                        intent.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "For Erase data,Lock Phone and Password Change"
                        )
                        startActivityForResult(intent, DEVICE_ADMIN_CODE)
                        dialogBox.dismiss()

                    }
                    .setNegativeButton(
                        getString(R.string.cancel)
                    ) { dialog, which -> dialog.dismiss() }
                alertDialog2 = alertDialogBuilderUserInput.create()
                alertDialog2.show()

                alertDialog2.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        intent.putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            mDeviceAdminSample
                        )
                        intent.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            "For Erase data,Lock Phone and Password Change"
                        )
                        startActivityForResult(intent, 15)
                        alertDialog2.dismiss()
                    }
                alertDialog2.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setOnClickListener {
                        alertDialog2.dismiss()
                        switch1.isChecked = false
                    }
                alertDialog2.setOnCancelListener { dialogInterface ->
                    dialogInterface.dismiss()
                    switch1.isChecked = false
                }


            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //TODO: send multiple request
    @SuppressLint("StaticFieldLeak")
    fun sendMultipleRequest(jsonArray: JSONArray?, text: String) {

        object :
            AsyncTask<Any?, Any?, Any?>() {
            override fun onPreExecute() {
                showDialog(text)
                if (jsonArray != null) {
                    Log.d(TAGI, "json array size: " + jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        Log.d(TAGI, jsonObject.getString("token"))
                    }
                }

            }

            override fun onPostExecute(result: Any?) {
                hideDialog()
            }

            override fun doInBackground(vararg p0: Any?): Any? {
                //Create Array of Post Variabels
                val postVars: ArrayList<NameValuePair> =
                    ArrayList()

                //Add a 1st Post Value called JSON with String value of JSON inside
                //This is first and last post value sent because server side will decode the JSON and get other vars from it.
                postVars.add(BasicNameValuePair("JSON", jsonArray.toString()))

                //Declare and Initialize Http Clients and Http Posts
                val httpclient: HttpClient = DefaultHttpClient()
                val httppost = HttpPost(mainUrl + SEND_MULTIPLE_REQUEST)

                //Format it to be sent
                try {
                    httppost.entity = UrlEncodedFormEntity(postVars)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

                /* Send request and Get the Response Back */try {
                    val response: HttpResponse = httpclient.execute(httppost)
                    val responseBody: String = EntityUtils.toString(response.entity)
                    hideDialog()
                    Log.d(TAGI, "doInBackground: $responseBody")
                    val msg = responseBody.replace("\"", "")

                    (context as Activity).runOnUiThread {
                        showToast(msg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }
                return null
            }
        }.execute(null, null, null)
    }


    fun getScreenWidth(): Int {
        val wm: WindowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)


        return metrics.widthPixels
    }

    fun getScreenHeight(): Int {
        val wm: WindowManager =
            requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)


        return metrics.heightPixels
    }

    fun getNearbyPlcae(query: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri.toString()))
        intent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            try {
                val unrestrictedIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri.toString()))
                startActivity(unrestrictedIntent)
            } catch (innerEx: ActivityNotFoundException) {
                showToast(getString(R.string.install_map_app))
            }
        }


    }

    open fun loadJSONFromAsset(): String {
        val json: String?
        json = try {
            val `is`: InputStream = requireActivity().assets.open("mobile_operator_code.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close().toString()
            String(buffer, Charsets.UTF_8)

        } catch (ex: IOException) {
            ex.printStackTrace()
            return null.toString()
        }
        return json
    }

    private fun updatePhoneNumber(num1: String) {

        val restAdapter: RestAdapter =
            RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.updatePhone(
            SharedPrefUtils.getStringData(requireActivity(), "uid").toString(),
            num1,
            activity?.let {
                SharedPrefUtils.getStringData(it, "deviceToken").toString()
            }.toString(), baseContext!!.getMacAddres(),
            object : Callback<Response> {
                @SuppressLint("SetTextI18n")
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
                        val jsonObject = JSONObject(output)
                        val newJson = jsonObject.getJSONObject("data")
                        val pid = newJson.getString("pid")
                        val phoneNum = newJson.getString("phone_num")
                        val id = newJson.getString("uid")
                        val isInserted = jsonObject.getBoolean("isInserted")
                        val error = jsonObject.getBoolean("error")
                        val messagePhone = jsonObject.getString("message")

                        if (jsonObject.getBoolean("isInserted")) {
                            SharedPrefUtils.saveData(requireActivity(), "uid", id)
                            SharedPrefUtils.saveData(requireActivity(), "phoneNum", phoneNum)
                            SharedPrefUtils.saveData(requireActivity(), "pid", pid)
                            layoutNumber!!.visibility = View.VISIBLE
                            num!!.text = "Your number is $phoneNum"
                            SharedPrefUtils.saveData(context!!, "isInserted", isInserted)
                            registerDevice(id, pid)
                        } else if (error) {
                            alreadyAddDialog(messagePhone)
                            hideDialog()
                        } else {
                            SharedPrefUtils.saveData(requireActivity(), "uid", id)
                            SharedPrefUtils.saveData(requireActivity(), "phoneNum", phoneNum)
                            SharedPrefUtils.saveData(requireActivity(), "pid", pid)
                            layoutNumber!!.visibility = View.VISIBLE
                            num!!.text = "Your number is $phoneNum"
                            hideDialog()
                        }

//                        hideDialog()
                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        hideDialog()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                }
            }
        )

    }

    private fun alreadyAddDialog(messagePhone: String) {
        val builder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialogTheme)
        builder.setTitle("Your current mobile device is " + Build.BRAND + ", " + Build.MODEL)
        builder.setMessage(messagePhone)
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which -> // Do do my action here
            enterNumberDialog()
            dialog.dismiss()
        }
        builder.setNeutralButton(
            getString(R.string.not_now)
        ) { dialog, which -> // Do do my action here
            FirebaseAuth.getInstance().signOut()
            baseContext!!.navigateFragment(R.id.nav_profile)
            mainContext!!.updateNavView()
            baseContext!!.changeMenu()
            dialog.dismiss()
        }
        builder.setNegativeButton(
            getString(R.string.replace)
        ) { dialog, which -> // Do do my action here
            showDialog("Replacing device...")
            replaceDevice()
            dialog.dismiss()
        }


        val alert = builder.create()
        alert.show()
    }

    @SuppressLint("InflateParams")
    fun refreshAd(frameLayout: FrameLayout, layout: Int) {

        try {
            if (!SharedPrefUtils.getBooleanData(requireActivity(), "hideAds")) {

                val builder = AdLoader.Builder(requireActivity(), getString(R.string.nativead))

                builder.forUnifiedNativeAd { unifiedNativeAd ->
                    // OnUnifiedNativeAdLoadedListener implementation.
                    try {

                        val adView = layoutInflater
                            .inflate(layout, null) as UnifiedNativeAdView
                        populateUnifiedNativeAdView(unifiedNativeAd, adView)

                        frameLayout.removeAllViews()
                        frameLayout.addView(adView)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }


                val adLoader = builder.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        frameLayout.visibility = View.GONE
                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())


            } else {
                frameLayout.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //TODO: populateUnifiedNativeAdView
    private fun populateUnifiedNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView
    ) {
        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.videoController

        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
        }

        val mediaView = adView.ad_media
        val mainImageView = adView.ad_image

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeAppInstallAd has a video asset.
        if (vc.hasVideoContent()) {
            adView.mediaView = mediaView
            mainImageView.visibility = View.GONE

        } else {
            adView.imageView = mainImageView
            mediaView.visibility = View.GONE

            // At least one image is guaranteed.
            val images = nativeAd.images
            mainImageView.setImageDrawable(images[0].drawable)

        }

        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // Some assets are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.setNativeAd(nativeAd)
    }

}

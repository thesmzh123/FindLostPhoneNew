@file:Suppress("DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")

package device.spotter.finder.appss.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.findNavController
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.facebook.ads.*
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.tasks.TaskExecutors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import device.spotter.finder.appss.R
import device.spotter.finder.appss.fragments.ProfileFragment
import device.spotter.finder.appss.utils.Constants.PRODUCT_KEY
import device.spotter.finder.appss.utils.Constants.TAGI
import device.spotter.finder.appss.utils.DatabaseHelperUtils
import device.spotter.finder.appss.utils.GPSTracker
import device.spotter.finder.appss.utils.InternetConnection
import device.spotter.finder.appss.utils.RegisterAPI
import kotlinx.android.synthetic.main.enter_phone_num_layout.view.ccpNUm
import kotlinx.android.synthetic.main.enter_phone_num_layout.view.editText_carrierNumber1
import kotlinx.android.synthetic.main.enter_phone_num_layout.view.mainBtnNUm
import kotlinx.android.synthetic.main.enter_phone_num_otp_layout.view.*
import kotlinx.android.synthetic.main.enter_phone_num_update_layout.view.*
import kotlinx.android.synthetic.main.layout_loading_dialog.view.*
import kotlinx.android.synthetic.main.profile_menu_layout.view.*
import org.json.JSONObject
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseActivity : AppCompatActivity(), ProfileFragment.MenuButtonListener,
    BillingProcessor.IBillingHandler {
    var menu: Menu? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var auth1: FirebaseAuth
    private var isMenuEnable = true
    lateinit var interstitial: InterstitialAd
    private var dialog: AlertDialog? = null
    var gpsTracker: GPSTracker? = null
    var mainUrl: String? = null
    var ringtone: Ringtone? = null
    var databaseHelperUtils: DatabaseHelperUtils? = null
    var queue: RequestQueue? = null
    var noAdsItem: MenuItem? = null
    var bp: BillingProcessor? = null
    private var fbAdView: com.facebook.ads.AdView? = null
    var interstitialAdFb: com.facebook.ads.InterstitialAd? = null

    fun loadFbInter() {
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad?) {
                // Interstitial ad displayed callback
                Log.e(TAGI, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad?) {
                // Interstitial dismissed callback
                Log.e(TAGI, "Interstitial ad dismissed.")
            }

            override fun onError(ad: Ad?, adError: AdError) {
                // Ad error callback
                Log.e(
                    TAGI,
                    "Interstitial ad failed to load: " + adError.errorMessage
                )
            }

            override fun onAdLoaded(p0: Ad?) {
                Log.d(TAGI, "onAdLoaded: ")
//                interstitialAdFb!!.show()
            }

            override fun onAdClicked(ad: Ad?) {
                // Ad clicked callback
                Log.d(TAGI, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad?) {
                // Ad impression logged callback
                Log.d(TAGI, "Interstitial ad impression logged!")
            }
        }


        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAdFb!!.loadAd(
            interstitialAdFb!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )

    }

    fun fbBanner(linearLayout: LinearLayout) {
        if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
            fbAdView = AdView(
                this,
                getString(R.string.fb_banner),
                AdSize.BANNER_HEIGHT_50
            )

            // Find the Ad Container


            // Add the ad view to your activity layout
            linearLayout.addView(fbAdView)


            // Request an ad
            fbAdView!!.loadAd()
            val adListener: com.facebook.ads.AdListener = object : com.facebook.ads.AdListener {

                override fun onAdLoaded(ad: Ad?) {
                    // Ad loaded callback
                    linearLayout.visibility = View.VISIBLE
                }

                override fun onAdClicked(ad: Ad?) {
                    // Ad clicked callback
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    linearLayout.visibility = View.GONE
                }

                override fun onLoggingImpression(ad: Ad?) {
                    // Ad impression logged callback
                }
            }


            // Request an ad
            fbAdView!!.loadAd(fbAdView!!.buildLoadAdConfig().withAdListener(adListener).build())
        } else {
            linearLayout.visibility = View.GONE

        }
    }

    //TODO: add back arrow to activity
    fun addBackArrow() {
        // add back arrow to toolbar
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }


    //TODO: fetch base url
    fun fetchBaseUrl() {
        //connecting declared wiidgets with xml
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("web")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val value =
                    dataSnapshot.child("url").getValue(
                        String::class.java
                    )!!
//                Log.d(TAGI, value)
                try {
                    SharedPrefUtils.saveData(
                        applicationContext,
                        "base_url",
                        value
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                Log.d(
                    TAGI,
                    "loadPost:onCancelled",
                    databaseError.toException()
                )

            }
        })
    }

    //TODO: fetch lic key
    fun fetchLicKey() {
        //connecting declared wiidgets with xml
        val databaseReference =
            FirebaseDatabase.getInstance().reference.child("noads")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val value =
                    dataSnapshot.child("lic_key").getValue(
                        String::class.java
                    )!!
//                Log.d(TAGI, value)
                try {
                    SharedPrefUtils.saveData(
                        applicationContext,
                        "lic_key",
                        value
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                bp = BillingProcessor(this@BaseActivity, value, this@BaseActivity)
                bp!!.initialize()
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                Log.d(
                    TAGI,
                    "loadPost:onCancelled",
                    databaseError.toException()
                )
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        /*     val p: PackageManager = packageManager
             val componentName = ComponentName(this, MainActivity::class.java)
             p.setComponentEnabledSetting(
                 componentName,
                 PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                 PackageManager.DONT_KILL_APP
             )*/
        super.onCreate(savedInstanceState)
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)
        queue = Volley.newRequestQueue(this) // this = context
        databaseHelperUtils = DatabaseHelperUtils(this@BaseActivity)
        auth = FirebaseAuth.getInstance()
        auth1 = FirebaseAuth.getInstance()
        if (!SharedPrefUtils.getBooleanData(this, "isFirst")) {

            FirebaseAuth.getInstance().signOut()
        }
        fetchBaseUrl()
        fetchLicKey()
        gpsTracker = GPSTracker(this)
        try {

            mainUrl = SharedPrefUtils.getStringData(this@BaseActivity, "base_url")

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isLoggedIn()) {
            val phone = SharedPrefUtils.getStringData(this@BaseActivity, "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                FirebaseAuth.getInstance().signOut()
            }
            if (InternetConnection().checkConnection(this)) {
                registerDevice(SharedPrefUtils.getStringData(this, "uid").toString())
                if (gpsTracker!!.canGetLocation()) {
                    updateLocation(
                        gpsTracker!!.getLatitude().toString(),
                        gpsTracker!!.getLongitude().toString(), lastUpdatedDate()
                    )
                }
            }
        }
        AdSettings.addTestDevice("6f0d4546-e1fe-4a30-9d03-6d7d24edd597")
        interstitialAdFb = InterstitialAd(this,getString(R.string.fb_inter))
        loadFbInter()
    }

    //TODO: change profile menu
    fun changeMenu() {
        try {
            val profileItem = menu!!.findItem(R.id.profile_menu)
            val v = profileItem.actionView
            val itemClick = v.layout
            val profileImage = changeProfile()
            if (isLoggedIn()) {
                Glide.with(this@BaseActivity)
                    .load(SharedPrefUtils.getStringData(this@BaseActivity, "userprofile"))
                    .into(profileImage)
            } else {
                Glide.with(this@BaseActivity).load(R.drawable.ic_profile_black_24dp)
                    .into(profileImage)

            }
            itemClick.setOnClickListener {
                if (isMenuEnable) {
                    navigateFragmentByAds(R.id.nav_profile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: change profile image
    private fun changeProfile(): CircleImageView {
        val profileItem = menu!!.findItem(R.id.profile_menu)
        val v = profileItem.actionView
        return v.toolbar_profile_image
    }

    override fun menuEnable(isEnable: Boolean) {
        isMenuEnable = isEnable
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

    //TODO: load interstial
    fun loadInterstial() {
        try {

            Log.d(TAGI, "load ads")
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                interstitial = InterstitialAd(this)
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

    //TODO: show dialog
    fun showDialog(message: String) {
        dialog = setProgressDialog(this, message)
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
    private fun setProgressDialog(context: Context, message: String): AlertDialog {

        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.MaterialAlertDialogTheme
        )
        builder.setCancelable(false)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.layout_loading_dialog, null)
        builder.setView(view)

        view.dialogText.text = message
        return builder.create()
    }

    //TODO: navigate to new fragment by ads
    fun navigateFragmentByAds(id: Int) {
        showDialog(getString(R.string.loading))
        Handler(Looper.getMainLooper()).postDelayed({
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                hideDialog()
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    if (interstitialAdFb!!.isAdLoaded) {
                        interstitialAdFb!!.show()
                    } else {
                        findNavController(R.id.nav_host_fragment).navigate(id)
                    }
                    val interstitialAdListener: InterstitialAdListener =
                        object : InterstitialAdListener {
                            override fun onInterstitialDisplayed(ad: Ad?) {
                                // Interstitial ad displayed callback
                                Log.e(TAGI, "Interstitial ad displayed.")
                            }

                            override fun onInterstitialDismissed(ad: Ad?) {
                                // Interstitial dismissed callback
                                loadFbInter()
                                Log.e(TAGI, "Interstitial ad dismissed.")
                                findNavController(R.id.nav_host_fragment).navigate(id)

                            }

                            override fun onError(ad: Ad?, adError: AdError) {
                                // Ad error callback
                                Log.e(
                                    TAGI,
                                    "Interstitial ad failed to load: " + adError.errorMessage
                                )
                            }

                            override fun onAdLoaded(p0: Ad?) {
                                Log.d(TAGI, "onAdLoaded: ")
                            }

                            override fun onAdClicked(ad: Ad?) {
                                // Ad clicked callback
                                Log.d(TAGI, "Interstitial ad clicked!")
                            }

                            override fun onLoggingImpression(ad: Ad?) {
                                // Ad impression logged callback
                                Log.d(TAGI, "Interstitial ad impression logged!")
                            }
                        }


                    // For auto play video ads, it's recommended to load the ad
                    // at least 30 seconds before it is shown
                    interstitialAdFb!!.loadAd(
                        interstitialAdFb!!.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build()
                    )
                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        findNavController(R.id.nav_host_fragment).navigate(id)
                    }
                }
            } else {
                hideDialog()
                findNavController(R.id.nav_host_fragment).navigate(id)

            }
        }, 2000)
    }

    //TODO: navigate to new fragment by ads fb
    fun navigateFragmentByAdsFb(id: Int) {
        showDialog(getString(R.string.loading))
        Handler(Looper.getMainLooper()).postDelayed({
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                hideDialog()
                if (interstitialAdFb!!.isAdLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitialAdFb!!.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    findNavController(R.id.nav_host_fragment).navigate(id)

                }
                val interstitialAdListener: InterstitialAdListener =
                    object : InterstitialAdListener {
                        override fun onInterstitialDisplayed(ad: Ad?) {
                            // Interstitial ad displayed callback
                            Log.e(TAGI, "Interstitial ad displayed.")
                        }

                        override fun onInterstitialDismissed(ad: Ad?) {
                            // Interstitial dismissed callback
                            loadFbInter()
                            Log.e(TAGI, "Interstitial ad dismissed.")
                            findNavController(R.id.nav_host_fragment).navigate(id)

                        }

                        override fun onError(ad: Ad?, adError: AdError) {
                            // Ad error callback
                            Log.e(
                                TAGI,
                                "Interstitial ad failed to load: " + adError.errorMessage
                            )
                        }

                        override fun onAdLoaded(p0: Ad?) {
                            Log.d(TAGI, "onAdLoaded: ")
                        }

                        override fun onAdClicked(ad: Ad?) {
                            // Ad clicked callback
                            Log.d(TAGI, "Interstitial ad clicked!")
                        }

                        override fun onLoggingImpression(ad: Ad?) {
                            // Ad impression logged callback
                            Log.d(TAGI, "Interstitial ad impression logged!")
                        }
                    }


                // For auto play video ads, it's recommended to load the ad
                // at least 30 seconds before it is shown
                interstitialAdFb!!.loadAd(
                    interstitialAdFb!!.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build()
                )

            } else {
                hideDialog()
                findNavController(R.id.nav_host_fragment).navigate(id)

            }
        }, 2000)
    }

    fun openProfileNUmDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        navigateFragment(R.id.nav_profile)
                        dialog.dismiss()
                    }

                }
            }

        val builder = MaterialAlertDialogBuilder(
            this@BaseActivity,
            R.style.MaterialAlertDialogTheme
        )
        builder.setMessage(getString(R.string.not_enter_phone_num))
            .setPositiveButton(getString(R.string.ok), dialogClickListener).show()
    }

    //TODO: get mac address
    @SuppressLint("HardwareIds")
    fun getMacAddres(): String {
        try {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes = nif.hardwareAddress ?: return ""

                val res1 = StringBuilder()
                for (b in macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:", b))
                }

                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ""
    }

    //TODO: get latitude
    /*   fun getLat(): String {
           var lat: String? = null
           if (gpsTracker?.canGetLocation()!!) {
               Log.d(TAGI, "lat: " + gpsTracker!!.getLatitude())
               Log.d(TAGI, "long: " + gpsTracker!!.getLongitude())
               lat = gpsTracker!!.getLatitude().toString()
           }
           return lat.toString()
       }

       //TODO: get Longitude
       fun getLongi(): String {
           var longi: String? = null
           if (gpsTracker?.canGetLocation()!!) {
               Log.d(TAGI, "lat: " + gpsTracker!!.getLatitude())
               Log.d(TAGI, "long: " + gpsTracker!!.getLongitude())
               longi = gpsTracker!!.getLongitude().toString()
           }
           return longi.toString()
       }*/

    //TODO: last updated
    @SuppressLint("SimpleDateFormat")
    fun lastUpdatedDate(): String {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        return sdf.format(Date())
    }

    //TODO: register device
    fun registerDevice(id: String) {

        try {

            val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
            val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
            api.fetchAllDevices(id,
                getMacAddres(),
                SharedPrefUtils.getStringData(this@BaseActivity, "lati").toString(),
                SharedPrefUtils.getStringData(this@BaseActivity, "longi").toString(),
                SharedPrefUtils.getStringData(this@BaseActivity, "deviceToken").toString(),
                lastUpdatedDate(),
                object : Callback<Response> {
                    override fun success(result: Response, response: Response) {
                        //On success we will read the server's output using buffered reader
                        //Creating a buffered reader object
                        val reader: BufferedReader?

                        //An string to store output from the server
                        val output: String

                        try {
                            //Initializing buffered reader
                            reader = BufferedReader(InputStreamReader(result.body.`in`()))

                            //Reading the output in the string
                            output = reader.readLine()
                            Log.d(TAGI, "device: $output")
                            SharedPrefUtils.saveData(
                                this@BaseActivity,
                                "devicedata",
                                output
                            )
                        } catch (e: Exception) {
                            Log.d(TAGI, "error: " + e.message)
                            e.printStackTrace()
                        }

                    }

                    override fun failure(error: RetrofitError) {
                        Log.d(TAGI, error.toString())
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: navigate to new fragment by ads
    fun navigateFragmentByAdsActivate(id: Int) {
        val bundle = bundleOf("isActivate" to true)
        showDialog(getString(R.string.loading))
        Handler(Looper.getMainLooper()).postDelayed({
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                hideDialog()
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                            Lifecycle.State.STARTED
                        )
                    ) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")
                    }
                } else {
                    if (interstitialAdFb!!.isAdLoaded) {
                        interstitialAdFb!!.show()
                    } else {
                        findNavController(R.id.nav_host_fragment).navigate(id, bundle)
                    }
                    val interstitialAdListener: InterstitialAdListener =
                        object : InterstitialAdListener {
                            override fun onInterstitialDisplayed(ad: Ad?) {
                                // Interstitial ad displayed callback
                                Log.e(TAGI, "Interstitial ad displayed.")
                            }

                            override fun onInterstitialDismissed(ad: Ad?) {
                                // Interstitial dismissed callback
                                loadFbInter()
                                Log.e(TAGI, "Interstitial ad dismissed.")
                                findNavController(R.id.nav_host_fragment).navigate(id, bundle)

                            }

                            override fun onError(ad: Ad?, adError: AdError) {
                                // Ad error callback
                                Log.e(
                                    TAGI,
                                    "Interstitial ad failed to load: " + adError.errorMessage
                                )
                            }

                            override fun onAdLoaded(p0: Ad?) {
                                Log.d(TAGI, "onAdLoaded: ")
                            }

                            override fun onAdClicked(ad: Ad?) {
                                // Ad clicked callback
                                Log.d(TAGI, "Interstitial ad clicked!")
                            }

                            override fun onLoggingImpression(ad: Ad?) {
                                // Ad impression logged callback
                                Log.d(TAGI, "Interstitial ad impression logged!")
                            }
                        }


                    // For auto play video ads, it's recommended to load the ad
                    // at least 30 seconds before it is shown
                    interstitialAdFb!!.loadAd(
                        interstitialAdFb!!.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build()
                    )

                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        findNavController(R.id.nav_host_fragment).navigate(id, bundle)
                    }
                }
            } else {
                hideDialog()
                findNavController(R.id.nav_host_fragment).navigate(id, bundle)

            }
        }, 2000)
    }

    //TODO:  navigate to new fragment
    fun navigateFragment(id: Int) {
        findNavController(R.id.nav_host_fragment).navigate(id, null)

    }

    //TODO: banner
    fun adView(adView: AdView) {
//        adView.visibility = View.GONE
        try {
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
                adView.adListener = object : AdListener() {

                    override fun onAdLoaded() {
                        adView.visibility = View.VISIBLE
                    }

                    override fun onAdFailedToLoad(error: Int) {
                        adView.visibility = View.GONE

                    }

                }
            } else {
                adView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: ring phone
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun ringSilentPhone() {
        ringtone = RingtoneManager.getRingtone(
            applicationContext,
            Settings.System.DEFAULT_RINGTONE_URI
        )
        var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alert == null) {
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alert == null) {
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }
        ringtone = RingtoneManager.getRingtone(applicationContext, alert)
        //ringtone!!.setStreamType(AudioManager.STREAM_ALARM)
        ringtone?.audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        //send acknownledgement back to requester
        if (ringtone != null) {
            Log.d("Test", "played")
            ringtone!!.play()


        }
    }

    fun showToast(toast: String) {
        Toast.makeText(this@BaseActivity, toast, Toast.LENGTH_LONG).show()

    }

    //TODO: rate us
    fun rateUs() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }

    //TODO: share App
    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + packageName + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //TODO: share App family locator
    fun shareAppFamily(number: String?) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage =
                "\nI recommend you to join me as family member by using this phone number $number by visiting this app and installing the application from play store.\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + packageName + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var deleteDialog: AlertDialog? = null
    private var verificationId: String? = null
    var otpDialogView: View? = null
    private var isRecent: Boolean = false
    private var isFinish: Boolean = false
    private var cdt: CountDownTimer? = null
    private var otpDialog: AlertDialog? = null
    private var getNum: String? = null

    private fun sendVerificationCode(number: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+$number",
            60,
            TimeUnit.SECONDS,
            TaskExecutors.MAIN_THREAD,
            mCallBack
        )
    }

    private val mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
                deleteDialog!!.dismiss()
                hideDialog()
                showOTPDialog()
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code: String = phoneAuthCredential.smsCode.toString()
                Log.d(TAGI, "onVerificationCompleted: $code")
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
            MaterialAlertDialogBuilder(this@BaseActivity, R.style.MaterialAlertDialogTheme)
        builder.setTitle("OTP Error!")
        builder.setMessage("Some error occurred while generating OTP or Your Quota of OTP for this number has expired!\n\n Please try again by re-entering the number.")
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, which -> // Do do my action here
            updateNumberDialog()
            dialog.dismiss()
        }

        builder.setNegativeButton(
            getString(R.string.no)
        ) { dialog, which -> // I do not need any action here you might
            FirebaseAuth.getInstance().signOut()
            dialog.dismiss()

        }

        val alert = builder.create()
        alert.show()
    }
/*

    private fun enterNumberDialog() {
        val factory = LayoutInflater.from(this@BaseActivity)
        @SuppressLint("InflateParams") val deleteDialogView: View =
            factory.inflate(R.layout.enter_phone_num_layout, null)
        deleteDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(this@BaseActivity).create()
        } else {
            AlertDialog.Builder(this@BaseActivity).create()
        }

        deleteDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog!!.setView(deleteDialogView)
        deleteDialog!!.setCancelable(false)
//        deleteDialogView.ccp1.registerCarrierNumberEditText(deleteDialogView.editText_carrierNumber1)

        deleteDialogView.mainBtnNUm.setOnClickListener {
            if (TextUtils.isEmpty(deleteDialogView.editText_carrierNumber1.text)) {
                showToast(getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(this@BaseActivity)) {
                    if (!SharedPrefUtils.getBooleanData(this@BaseActivity, "isInserted")) {
                        showDialog(getString(R.string.sending_you_verification_code))
                        getNum =
                            deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString()
//                        sendVerificationCode(deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString())
                        updatePhoneNumber(deleteDialogView.editText_carrierNumber1.text.toString())
                    }
                    deleteDialog!!.dismiss()
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }

        deleteDialog!!.show()
        deleteDialog!!.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }
*/

    @SuppressLint("InflateParams")
    private fun showOTPDialog() {
        isFinish = false
        isRecent = false
        val factory = LayoutInflater.from(this@BaseActivity)
        otpDialogView = factory.inflate(R.layout.enter_phone_num_otp_layout, null)
        otpDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(this@BaseActivity).create()
        } else {
            AlertDialog.Builder(this@BaseActivity).create()
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
                try {
                    if (InternetConnection().checkConnection(this@BaseActivity)) {
                        showDialog(getString(R.string.verifying_code))
                        val code = otpDialogView!!.editText_carrierNumber2.text.toString()
                        verifyCode(code)
                        //                        updatePhoneNumber(deleteDialogView.editText_carrierNumber1.text.toString())

                        //                    otpDialog!!.dismiss()
                    } else {
                        showToast(getString(R.string.no_internet))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        otpDialogView!!.mainBtn2.setOnClickListener {
            try {
                if (isFinish) {
                    cdt!!.cancel()
                    isRecent = true
                    showToast("Please re-enter the number to get the verification code (OTP).")
                    otpDialog!!.dismiss()
                    updateNumberDialog()
                } else {
                    showToast("Timer is already running.\n Can't resend the OTP Code.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
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
    }

    private fun updatePhoneNumber(num1: String) {

        val restAdapter: RestAdapter =
            RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.updatePhoneNum(
            SharedPrefUtils.getStringData(this@BaseActivity, "uid").toString(),
            num1,
            SharedPrefUtils.getStringData(this@BaseActivity, "deviceToken").toString(),
            getMacAddres(),
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
                        val jsonObject = JSONObject(output)
                        if (jsonObject.getBoolean("error")) {
                            deleteDialog!!.dismiss()
                            hideDialog()
                            Log.d(TAGI, "success: " + jsonObject.getString("message"))
                            numberExistDialog(jsonObject.getString("message"))
                        } else {
                            val newJson = jsonObject.getJSONObject("data")
                            val pid = newJson.getString("pid")
                            val phoneNum = newJson.getString("phone_num")
                            val id = newJson.getString("uid")
//                        val isInserted = jsonObject.getBoolean("isInserted")
                            SharedPrefUtils.saveData(this@BaseActivity, "uid", id)
                            SharedPrefUtils.saveData(this@BaseActivity, "phoneNum", phoneNum)
                            SharedPrefUtils.saveData(this@BaseActivity, "pid", pid)


                            hideDialog()
                            navigateFragment(R.id.nav_profile)
                        }
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

    override fun onBillingInitialized() {
        Log.d(TAGI, "onBillingInitialized")

    }

    fun updateNumberDialog() {
        val factory = LayoutInflater.from(this@BaseActivity)
        @SuppressLint("InflateParams") val deleteDialogView: View =
            factory.inflate(R.layout.enter_phone_num_update_layout, null)
        deleteDialog = if (Build.VERSION.SDK_INT > 23) {

            MaterialAlertDialogBuilder(this@BaseActivity).create()
        } else {
            AlertDialog.Builder(this@BaseActivity).create()
        }

        deleteDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog!!.setView(deleteDialogView)
        deleteDialog!!.setCancelable(false)
//        deleteDialogView.ccp1.registerCarrierNumberEditText(deleteDialogView.editText_carrierNumber1)
        deleteDialogView.mainBtnNUmCancel.setOnClickListener {
            deleteDialog!!.dismiss()
        }
        deleteDialogView.mainBtnNUm.setOnClickListener {
            if (TextUtils.isEmpty(deleteDialogView.editText_carrierNumber1.text)) {
                showToast(getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(this@BaseActivity)) {
                    showDialog(getString(R.string.sending_you_verification_code))
                    getNum =
                        deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString()
                    sendVerificationCode(deleteDialogView.ccpNUm.selectedCountryCode + deleteDialogView.editText_carrierNumber1.text.toString())
//                    updatePhoneNumber(getNum.toString())
//                        updatePhoneNumber(deleteDialogView.editText_carrierNumber1.text.toString())

                    deleteDialog!!.dismiss()
                } else {
                    showToast(getString(R.string.no_internet))
                }
            }
        }

        deleteDialog!!.show()
        deleteDialog!!.window!!.decorView.setBackgroundResource(
            android.R.color.transparent
        )
    }

    private fun numberExistDialog(messagePhone: String) {
        val builder =
            MaterialAlertDialogBuilder(this@BaseActivity, R.style.MaterialAlertDialogTheme)
        builder.setTitle("Your current mobile device is " + Build.BRAND + ", " + Build.MODEL)
        builder.setMessage(messagePhone)
        builder.setCancelable(false)
        builder.setPositiveButton(
            getString(R.string.ok)
        ) { dialog, which -> // Do do my action here
            updateNumberDialog()
            dialog.dismiss()
        }


        val alert = builder.create()
        alert.show()
    }

    override fun onPurchaseHistoryRestored() {
        try {
            Log.d(TAGI, "onPurchaseHistoryRestored: ")
            if (bp!!.isPurchased(PRODUCT_KEY)) {
                Log.d(TAGI, "onPurchaseHistoryRestored: true")
                hideAds()
            } else {
                Log.d(TAGI, "onPurchaseHistoryRestored: false")
                loadInterstial()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        try {
            Log.d(TAGI, "onProductPurchased: $productId")
            Log.d(TAGI, "onProductPurchased: $details")
            hideAds()
            startActivity(Intent(this@BaseActivity, MainActivity::class.java))
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        try {
            Log.d(TAGI, "onBillingError: " + error?.message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (!bp!!.handleActivityResult(requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data)
                Log.d(TAGI, "onActivityResult: done")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        if (bp != null) {
            bp!!.release()
        }
        if (fbAdView != null) {
            fbAdView!!.destroy()
        }
        super.onDestroy()
    }

    //TODO: hide ads
    fun hideAds() {
        try {
            SharedPrefUtils.saveData(this, "hideAds", true)
            noAdsItem!!.isVisible = false

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateLocation(
        lat: String,
        longi: String,
        lastUpdatedDate: String
    ) {

        val restAdapter: RestAdapter =
            RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.updateLocation(
            SharedPrefUtils.getStringData(this@BaseActivity, "uid").toString(),
            getMacAddres(),
            lat,
            longi,
            lastUpdatedDate,
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
                        SharedPrefUtils.saveData(
                            this@BaseActivity,
                            "devicedata",
                            output
                        )
//                            SharedPrefUtils.saveData(context!!, "lastUpdate", lastUpdatedDate())
//
                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                }
            }
        )

    }

}
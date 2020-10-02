@file:Suppress("DEPRECATION")

package lost.phone.finder.app.online.finder.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
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
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_loading_dialog.view.*
import kotlinx.android.synthetic.main.profile_menu_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.fragments.ProfileFragment
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.DatabaseHelperUtils
import lost.phone.finder.app.online.finder.utils.GPSTracker
import lost.phone.finder.app.online.finder.utils.RegisterAPI
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.util.*

open class BaseActivity : AppCompatActivity(), ProfileFragment.MenuButtonListener {
    var menu: Menu? = null
    lateinit var auth: FirebaseAuth
    private var isMenuEnable = true
    lateinit var interstitial: InterstitialAd
    private var dialog: AlertDialog? = null
    var gpsTracker: GPSTracker? = null
    var mainUrl: String? = null
    var ringtone: Ringtone? = null
    var databaseHelperUtils: DatabaseHelperUtils? = null
    var queue: RequestQueue? = null


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
        if (!SharedPrefUtils.getBooleanData(this, "isFirst")) {
            FirebaseAuth.getInstance().signOut()
        }
        fetchBaseUrl()
        gpsTracker = GPSTracker(this)
        try {
            mainUrl = SharedPrefUtils.getStringData(this@BaseActivity, "base_url")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (isLoggedIn()) {
            registerDevice(SharedPrefUtils.getStringData(this, "uid").toString())
        }
    }

    //TODO: change profile menu
    fun changeMenu() {
        val profileItem = menu!!.findItem(R.id.profile_menu)
        val v = profileItem.actionView
        val itemClick = v.layout
        val profileImage = changeProfile()
        if (isLoggedIn()) {
            Glide.with(this@BaseActivity).load(auth.currentUser!!.photoUrl).into(profileImage)
        } else {
            Glide.with(this@BaseActivity).load(R.drawable.ic_profile_black_24dp).into(profileImage)

        }
        itemClick.setOnClickListener {
            if (isMenuEnable) {
                navigateFragmentByAds(R.id.nav_profile)
            }
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
                    findNavController(R.id.nav_host_fragment).navigate(id)

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
    fun getLat(): String {
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
    }

    //TODO: register device
    fun registerDevice(id: String) {

        try {

            val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
            val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
            api.fetchAllDevices(id, object : Callback<Response> {
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
                    findNavController(R.id.nav_host_fragment).navigate(id, bundle)

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
                "\nI recommend you to join me as family member by using this phone number " + number + " by visiting this app and installing the application from play store.\n\n"
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + packageName + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
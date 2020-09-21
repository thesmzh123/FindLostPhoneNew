package lost.phone.finder.app.online.finder.receivers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
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
import java.text.SimpleDateFormat
import java.util.*

class UpdateReceiver : BroadcastReceiver() {
    private var gpsTracker: GPSTracker? = null
    private var context: Context? = null
    private var databaseUtils: DatabaseHelperUtils? = null
    private var mainUrl: String? = null

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, p1: Intent?) {
        this.context = context
        Log.d("Test", "onReceive: running")
        try {
            mainUrl = SharedPrefUtils.getStringData(context!!, "base_url")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        gpsTracker = GPSTracker(context!!)
        databaseUtils = DatabaseHelperUtils(context)
        if (SharedPrefUtils.getBooleanData(context, "isTrackLoc")) {
            if (InternetConnection().checkConnection(context)) {

                if (databaseUtils!!.checkGpsExist(1)) {
                    Log.d(TAGI, "lati-> " + databaseUtils!!.getLatitude(1))
                    if (gpsTracker!!.canGetLocation()) {
                        updateLocation(
                            databaseUtils!!.getLatitude(1),
                            databaseUtils!!.getLongitude(1),
                            databaseUtils!!.getTime(1)
                        )
                        databaseUtils!!.deleteLocation("1")
                    }
                } else {
                    if (gpsTracker!!.canGetLocation()) {
                        updateLocation(
                            gpsTracker!!.getLatitude().toString(),
                            gpsTracker!!.getLongitude().toString(),
                            lastUpdatedDate()
                        )
                    }
                }
                /*   fetchVideos(
                       SharedPrefUtils.getStringData(context, "uid").toString()
                   )*/
            } else {
                if (databaseUtils!!.checkGpsExist(1)) {
                    if (gpsTracker!!.canGetLocation()) {
                        databaseUtils!!.updateGPSLoc(
                            "1", gpsTracker!!.getLatitude().toString(),
                            gpsTracker!!.getLongitude().toString(),
                            lastUpdatedDate()
                        )
                    }
                } else {
                    if (gpsTracker!!.canGetLocation()) {
                        databaseUtils!!.addGpsTrack(
                            1, gpsTracker!!.getLatitude().toString(),
                            gpsTracker!!.getLongitude().toString(),
                            lastUpdatedDate()
                        )
                    }
                }
                Log.d(TAGI, "onHandleIntent: no internet")
            }
            startReciver(context)
        }
        /*   if (Intent.ACTION_BOOT_COMPLETED == p1!!.action) {

           } else {
               Log.d(TAGI, "no boot")

           }*/
    }

    private fun updateLocation(
        lat: String,
        longi: String,
        lastUpdatedDate: String
    ) {

        val restAdapter: RestAdapter =
            RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.updateLocation(
            SharedPrefUtils.getStringData(context!!, "uid").toString(),
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
                            context!!,
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

    private fun startReciver(context: Context) {
        val intent = Intent(context, UpdateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 103, intent, 0)
        //60000 equal to 1 minute
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager
            .set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 180000,
                pendingIntent
            )//180000
    }


    //TODO: last updated
    @SuppressLint("SimpleDateFormat")
    private fun lastUpdatedDate(): String {
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        println(" C DATE is  $currentDate")
        return currentDate
    }


}
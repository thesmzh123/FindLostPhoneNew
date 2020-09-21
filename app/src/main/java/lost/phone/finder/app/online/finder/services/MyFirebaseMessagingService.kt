package lost.phone.finder.app.online.finder.services


import android.app.*
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.ContactOwnerActivity
import lost.phone.finder.app.online.finder.activities.PlayRingPhoneActivity
import lost.phone.finder.app.online.finder.activities.PlayRingSilentActivity
import lost.phone.finder.app.online.finder.receivers.DeviceAdminSample
import lost.phone.finder.app.online.finder.utils.Constants.CHANNEL_ID
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAGI, "onNewToken: $p0")
    }

    internal var name: CharSequence = "My Channel"// The user-visible name of the channel.

    @RequiresApi(Build.VERSION_CODES.N)
    internal var importance = NotificationManager.IMPORTANCE_HIGH
    private val random = Random()

    private var devicePolicyManager: DevicePolicyManager? = null
    private var componentName: ComponentName? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        devicePolicyManager =
            applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        componentName = ComponentName(applicationContext, DeviceAdminSample::class.java)
        auth = FirebaseAuth.getInstance()

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val messageTitle = remoteMessage.data["title"]
        val messageBody = remoteMessage.data["message"]
        val clickAction = remoteMessage.data["action"]

        Log.d(TAGI, "msg: $messageBody")
        Log.d(TAGI, "title: $messageTitle")
        if (isLoggedIn()) {
            if (messageBody.equals("1") && messageTitle.equals("lockPhone")) {
                Log.d(TAGI, "lock")
                if (this.componentName?.let { devicePolicyManager!!.isAdminActive(it) }!!) {
                    devicePolicyManager!!.lockNow()
                }
            } else if (messageBody.equals("1") && messageTitle.equals("eraseData")) {
                Log.d(TAGI, "erase")
                if (this.componentName?.let { devicePolicyManager!!.isAdminActive(it) }!!) {
                    devicePolicyManager?.wipeData(1)
                }
            } else if (messageBody.equals("1") && messageTitle.equals("ringSilent")) {
                Handler(Looper.getMainLooper()).post {
                    startNewActivty(PlayRingSilentActivity())
                }

            } else if (messageBody.equals("1") && messageTitle.equals("ringPlay")) {
                Handler(Looper.getMainLooper()).post {
                    startNewActivty(PlayRingPhoneActivity())
                }

            } else if (messageTitle.equals("hope")) {
                val splitter: Array<String> = messageBody!!.split(",").toTypedArray()
                val message = splitter[1]
                val title = splitter[0]
                sendNotificationMsg(title, message, rand(1, 100))

            } else if (messageBody.equals("hope")) {
                val splitter: Array<String> = messageTitle!!.split(",").toTypedArray()
                val message = splitter[1]
                val title = splitter[0]
                sendNotificationMsg(title, message, rand(1, 100))

            } else if (messageTitle.equals("request_send", true)) {
                sendNotificationMsgFamily(
                    clickAction.toString(),
                    messageBody.toString(),
                    rand(1, 100),true
                )

            } else if (messageTitle.equals("cancel_request_pending", true)) {
                sendNotificationMsgFamily(
                    clickAction.toString(),
                    messageBody.toString(),
                    rand(2, 1000),
                    false
                )

            } else if (messageTitle.equals("cancel_request", true)) {
                sendNotificationMsgFamily(
                    clickAction.toString(),
                    messageBody.toString(),
                    rand(2, 1000),
                    false
                )

            }else if (messageTitle.equals("request_accept", true)) {
                sendNotificationMsgFamily(
                    clickAction.toString(),
                    messageBody.toString(),
                    rand(2, 1000),
                    false
                )

            } else if (messageTitle.equals("device")) {
                SharedPrefUtils.saveData(
                    applicationContext,
                    "devicedata",
                    messageBody.toString()
                )
            }
        }


    }

    //TODO: start activity
    private fun startNewActivty(activity: Activity) {
        val intent = Intent(applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)


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

    private fun sendNotificationMsg(
        messageTitle: String,
        body: String,
        rand: Int
    ) {
        try {
            Log.d(TAGI, "rand: $rand")
            val intent = Intent(applicationContext, ContactOwnerActivity::class.java)

            intent.putExtra("title", messageTitle)
            intent.putExtra("message", body)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent =
                PendingIntent.getActivity(this, rand, intent, PendingIntent.FLAG_ONE_SHOT)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.setSound(null, null)
                notificationManager.createNotificationChannel(mChannel)

                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Phone Owner")
                    .setContentText(body)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(rand, mBuilder.build())
                // Turn on the screen for notification


            } else {
                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Phone Owner")
                    .setContentText(body)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                val mNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(rand, mBuilder.build())


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sendNotificationMsgFamily(
        action: String,
        body: String,
        rand: Int,
        b: Boolean
    ) {
        try {
            Log.d(TAGI, "rand: $rand")
            val intent = Intent(action)
            intent.putExtra("b",b)


            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pendingIntent =
                PendingIntent.getActivity(this, rand, intent, PendingIntent.FLAG_ONE_SHOT)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                mChannel.setSound(null, null)
                notificationManager.createNotificationChannel(mChannel)

                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(body)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                notificationManager.notify(rand, mBuilder.build())
                // Turn on the screen for notification


            } else {
                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(body)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)

                val mNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.notify(rand, mBuilder.build())


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun rand(from: Int, to: Int): Int {
        return random.nextInt(to - from) + from
    }
}
package device.spotter.finder.appss.receivers

import android.annotation.TargetApi
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.util.Log
import device.spotter.finder.appss.utils.Constants.TAGI

class DeviceAdminSample : DeviceAdminReceiver() {


    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return super.onDisableRequested(context, intent)
    }

    override fun onPasswordChanged(context: Context, intent: Intent, user: UserHandle) {
        super.onPasswordChanged(context, intent, user)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }

    @Suppress("DEPRECATION")
    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        Log.d(TAGI, "Welcome Device Owner")
    }


    @Suppress("DEPRECATION")
    override fun onPasswordFailed(context: Context, intent: Intent) {
        Log.d(TAGI, "onPasswordFailed:")

    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        Log.d(TAGI, "onPasswordFailed: new")

    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        Log.d(TAGI, "Welcome Device Owner new")

    }
}

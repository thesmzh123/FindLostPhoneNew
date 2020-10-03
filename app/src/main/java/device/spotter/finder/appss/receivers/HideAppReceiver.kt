package device.spotter.finder.appss.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import device.spotter.finder.appss.activities.MainActivity
import device.spotter.finder.appss.utils.Constants.TAGI


class HideAppReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAGI, "onReceive: ")
    }
}
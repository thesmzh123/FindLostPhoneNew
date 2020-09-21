package lost.phone.finder.app.online.finder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import lost.phone.finder.app.online.finder.activities.MainActivity
import lost.phone.finder.app.online.finder.utils.Constants.TAGI


class HideAppReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.d(TAGI, "onReceive: ")
    }
}
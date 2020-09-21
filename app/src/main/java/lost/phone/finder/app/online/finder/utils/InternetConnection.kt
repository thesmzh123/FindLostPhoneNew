package com.find.lost.app.phone.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import lost.phone.finder.app.online.finder.utils.Constants.TAGI

@Suppress("DEPRECATION")
class InternetConnection {
    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */
    fun checkConnection(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork())
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
                }
            }
        } else {
            val activeNetworkInfo = connMgr.activeNetworkInfo

            if (activeNetworkInfo != null) { // connected to the internet
                // connected to the mobile provider's data plan
                return if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    Log.d(TAGI, "checkConnection: true")
                    true
                } else
                    activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
            }
        }
        return false
    }
}
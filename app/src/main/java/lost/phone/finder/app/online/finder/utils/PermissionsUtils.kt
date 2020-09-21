package lost.phone.finder.app.online.finder.utils

import android.Manifest.permission.*
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import lost.phone.finder.app.online.finder.R
import java.util.*

@Suppress("IMPLICIT_BOXING_IN_IDENTITY_EQUALS")
class PermissionsUtils {
    companion object {
        const val PERMISSION_ACCESS_COARSE_LOCATION = ACCESS_COARSE_LOCATION
        const val PERMISSION_ACCESS_FINE_LOCATION = ACCESS_FINE_LOCATION
        const val PERMISSION_READ_STORAGE = READ_EXTERNAL_STORAGE
        const val PERMISSION_WRITE_STORAGE = WRITE_EXTERNAL_STORAGE
        const val PERMISSION_WRITE_CONTACTS = WRITE_CONTACTS
        const val PERMISSION_READ_CONTACTS = READ_CONTACTS

        const val PERMISSION_REQUEST_CODE = 1
        const val PERMISSION_AUDIO = RECORD_AUDIO
    }


    private var permissions: PermissionsUtils? = null
    private var activity: Activity? = null
    private var requiredPermissions: ArrayList<String>? = null
    private var ungrantedPermissions = ArrayList<String>()

    constructor(activity: Activity?) {
        this.activity = activity
    }

    constructor()


    @Synchronized
    fun getInstance(activity: Activity): PermissionsUtils? {
        if (permissions == null) {
            permissions = PermissionsUtils(activity)
        }
        return this.permissions
    }


    private fun initPermissions() {
        requiredPermissions = ArrayList()
        requiredPermissions!!.add(PERMISSION_ACCESS_COARSE_LOCATION)
        requiredPermissions!!.add(PERMISSION_ACCESS_FINE_LOCATION)
        requiredPermissions!!.add(PERMISSION_READ_STORAGE)
        requiredPermissions!!.add(PERMISSION_WRITE_STORAGE)
        requiredPermissions!!.add(PERMISSION_READ_CONTACTS)
        requiredPermissions!!.add(PERMISSION_WRITE_CONTACTS)

        //Add all the required permission in the list
    }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    fun requestPermissionsIfDenied() {
        ungrantedPermissions = getUnGrantedPermissionsList()
        if (canShowPermissionRationaleDialog()) {
            showMessageOKCancel(activity!!.resources.getString(R.string.permission_message),
                DialogInterface.OnClickListener { dialog, which -> askPermissions() })
            return
        }
        askPermissions()
    }

    /* fun requestPermissionsIfDenied(permission: String) {
         if (canShowPermissionRationaleDialog(permission)) {
             showMessageOKCancel(activity!!.resources.getString(R.string.permission_message),
                 DialogInterface.OnClickListener { dialog, which -> askPermission(permission) })
             return
         }
         askPermission(permission)
     }*/

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    private fun canShowPermissionRationaleDialog(): Boolean {
        var shouldShowRationale = false
        for (permission in ungrantedPermissions) {
            val shouldShow =
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        it,
                        permission
                    )
                }
            if (shouldShow!!) {
                shouldShowRationale = true
            }
        }
        return shouldShowRationale
    }
/*
    private fun canShowPermissionRationaleDialog(permission: String): Boolean {
        var shouldShowRationale = false
        val shouldShow =
            activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) }
        if (shouldShow!!) {
            shouldShowRationale = true
        }
        return shouldShowRationale
    }*/

    private fun askPermissions() {
        if (ungrantedPermissions.size > 0) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    ungrantedPermissions.toTypedArray(),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

/*    private fun askPermission(permission: String) {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf<String>(permission),
                PERMISSION_REQUEST_CODE
            )
        }
    }*/

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton(R.string.ok, okListener)
            .setNegativeButton(
                R.string.cancel
            ) { dialogInterface, i ->
                Log.d("test", "ok")
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }


    fun isAllPermissionAvailable(): Boolean {
        var isAllPermissionAvailable = true
        initPermissions()
        for (permission in requiredPermissions!!) {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        permission
                    )
                } !== PackageManager.PERMISSION_GRANTED
            ) {
                isAllPermissionAvailable = false
                break
            }
        }
        return isAllPermissionAvailable
    }

    private fun getUnGrantedPermissionsList(): ArrayList<String> {
        val list = ArrayList<String>()
        for (permission in requiredPermissions!!) {
            val result = activity?.let { ActivityCompat.checkSelfPermission(it, permission) }
            if (result != PackageManager.PERMISSION_GRANTED) {
                list.add(permission)
            }
        }
        return list
    }

}
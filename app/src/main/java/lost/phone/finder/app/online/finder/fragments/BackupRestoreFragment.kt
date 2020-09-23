@file:Suppress("DEPRECATION", "SameParameterValue", "UNUSED_ANONYMOUS_PARAMETER")

package lost.phone.finder.app.online.finder.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.banner.view.*
import kotlinx.android.synthetic.main.contact_email_restore_dialog.view.*
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_backup_restore.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.PlayRingPhoneActivity
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.RestoreContacts
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class BackupRestoreFragment : BaseFragment() {
    private var isEmailBackup: Boolean = false
    private var isEmailRestore: Boolean = false
    private var cursor: Cursor? = null
    private var vCard: ArrayList<String>? = null
    var vfile: String? = null
    var vPath: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_backup_restore, container, false)

        root!!.titleText.text =
            getString(R.string.backup_and_restore)
        Glide.with(requireActivity()).load(R.drawable.backupcell).into(root!!.imageView)

        root!!.backupFile.setOnClickListener {
            root!!.restoreLayout.visibility = View.GONE
            root!!.backupLayout.visibility = View.VISIBLE
        }
        root!!.restoreFile.setOnClickListener {
            root!!.restoreLayout.visibility = View.VISIBLE
            root!!.backupLayout.visibility = View.GONE

        }
        root!!.emailRestore.isChecked = true
        root!!.emailBackup.isChecked = true
        isEmailRestore = true
        isEmailBackup = true
        vfile = "Contacts" + "_Ba.vcf"
        vPath = Environment.getExternalStorageDirectory().toString()

        root!!.radioBackup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.emailBackup -> {
                    isEmailBackup = true
                }
                R.id.memoryBackup -> {
                    isEmailBackup = false
                }
            }
        }
        root!!.radioRestore.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.emailRestore -> {
                    isEmailRestore = true
                }
                R.id.memoryRestore -> {
                    isEmailRestore = false
                }
            }
        }
        root!!.restoreBtn.setOnClickListener {
            if (isEmailRestore) {
                restoreContactFromEmail()
            } else {
                ContactsBackground("memoryrestore").execute()
            }

        }
        root!!.backupBtn.setOnClickListener {
            if (isEmailBackup) {
                ContactsBackground("emailbackup").execute()

            } else {
                ContactsBackground("memorybackup").execute()
            }
        }
        baseContext!!.adView(root!!.adView)
        return root
    }

    private fun restoreContactFromEmail() {
        val factory = LayoutInflater.from(requireActivity())
        @SuppressLint("InflateParams") val deleteDialogView: View =
            factory.inflate(R.layout.contact_email_restore_dialog, null)
        val deleteDialog: AlertDialog = MaterialAlertDialogBuilder(requireActivity()).create()
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setView(deleteDialogView)
//        deleteDialog.setCancelable(false)
        deleteDialogView.mainBtn.setOnClickListener {
            deleteDialog.dismiss()
            openGmailApp(requireActivity())
        }

        deleteDialog.show()
        deleteDialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }


    @SuppressLint("Recycle")
    private fun getVcardString() {
        // TODO Auto-generated method stub

        vCard = ArrayList()
        cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        if (cursor != null && cursor!!.count > 0) {
            try {
                cursor!!.moveToFirst()
                for (i in 0 until cursor!!.count) {
                    get(cursor!!)
                    Log.d(TAGI, "Contact " + (i + 1) + " VcF String is " + vCard!![i])
                    cursor!!.moveToNext()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    showToast("Sorry! You do not have contacts in your phone storage.\n Please try again!")
                }
            }
        } else {
            Log.d(TAGI, "No Contacts in Your Phone")
            showToast(getString(R.string.no_contacts_in_phone))
            hideDialog()
        }
    }

    operator fun get(cursor: Cursor) {


        //cursor.moveToFirst();
        val lookupKey =
            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
        val uri: Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        val fd: AssetFileDescriptor
        try {
            fd = requireActivity().contentResolver.openAssetFileDescriptor(uri, "r")!!


            val fis: FileInputStream = fd.createInputStream()
            val buf = ByteArray(fd.declaredLength.toInt())
            fis.read(buf)
            val vcardstring = String(buf)
            vCard!!.add(vcardstring)

            val mFileOutputStream = FileOutputStream("$vPath/$vfile", false)
            mFileOutputStream.write(vcardstring.toByteArray())

        } catch (e1: Exception) {
            // TODO Auto-generated catch block
            e1.printStackTrace()
        }
    }


    @SuppressLint("StaticFieldLeak")
    inner class ContactsBackground(private val type: String) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            when (type) {
                "memorybackup" -> {
                    getVcardString()
                }
                "emailbackup" -> {
                    getVcardString()
                }
                "memoryrestore" -> {
                    val file = File(
                        "$vPath/$vfile"
                    )

                    RestoreContacts.restoreData(file.absolutePath, requireActivity())
                }
            }
            return null

        }

        override fun onPreExecute() {
            super.onPreExecute()
            if (type == "memorybackup" || type == "emailbackup") {
                showDialog(getString(R.string.backuping_up_ct))

            } else if (type == "memoryrestore") {
                showDialog(getString(R.string.restoring_up_ct))

            }
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            when (type) {
                "memorybackup" -> {
                    hideDialog()
                    showToast(getString(R.string.contacts_success))
                }
                "emailbackup" -> {
                    if (File("$vPath/$vfile").exists()) {
                        val content = File("$vPath/$vfile")
                        val pm = requireActivity().packageManager
                        val isInstalled = isPackageInstalled("com.google.android.gm", pm)
                        if (isInstalled) {
                            sendMail(content.absolutePath)
                        } else {
                            showToast("Email application not installed!")
                        }
                    }
                    hideDialog()
                }
                "memoryrestore" -> {
                    hideDialog()
                }
            }

        }
    }

    private fun sendMail(content: String) {
        try {
            val pm = requireActivity().packageManager
            val isInstalled = isPackageInstalled("com.google.android.gm", pm)
            val intent = Intent("android.intent.action.SEND")
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("android.intent.extra.TEXT", getEmailBody())
            try {
                intent.putExtra(
                    "android.intent.extra.SUBJECT",
                    "Contact backup in VCF"
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                intent.putExtra(
                    "android.intent.extra.STREAM",
                    FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().packageName + ".provider",
                        File(content)
                    )
                )
                if (isInstalled) {
                    intent.type = "application/*"
                } else {
                    intent.type = "vnd.android.cursor.dir/email"
                }
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            } catch (e2: Exception) {
                e2.printStackTrace()
            }

            if (isInstalled) {
                intent.setPackage("com.google.android.gm")
                startActivity(intent)
            } else {
                startActivity(Intent.createChooser(intent, "Email:"))
            }
            showToast(getString(R.string.contacts_success))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isPackageInstalled(
        packageName: String,
        packageManager: PackageManager
    ): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getEmailBody(): String {
        return "This Attachment contains backup of your Device Contacts. Please Email it to your own email address & Keep it. Our Company. doesn’t access, save or store your backups.\n\n"
    }

    private fun openGmailApp(context: Context) {
        val pm = requireActivity().packageManager
        val isInstalled = isPackageInstalled("com.google.android.gm", pm)
        if (!isInstalled) {
            showToast("Gmail app is not installed.")
        } else if (isAppEnabled(context, "com.google.android.gm")) {
            context.startActivity(
                context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
            )
        } else {
            showToast("Gmail app is not enabled.")
        }
    }


    private fun isAppEnabled(context: Context, str: String?): Boolean {
        return try {
            val applicationInfo =
                context.packageManager.getApplicationInfo(str!!, 0)
            applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

}
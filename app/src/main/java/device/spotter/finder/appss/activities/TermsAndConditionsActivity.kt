@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package device.spotter.finder.appss.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.find.lost.app.phone.utils.SharedPrefUtils
import device.spotter.finder.appss.R

class TermsAndConditionsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)
        fetchBaseUrl()
        fetchLicKey()
        policyDialog()

    }

    private fun policyDialog() {
        val materialAlertDialog = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view: View = inflater.inflate(R.layout.terms_layout, null, false)
        materialAlertDialog.setView(view)
        materialAlertDialog.setCancelable(false)
//        setupHyperlink(view.subtitle)
        materialAlertDialog.create()
        materialAlertDialog.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
            finish()
            dialog.dismiss()
        }
        materialAlertDialog.setPositiveButton(getString(R.string.agree1)) { dialog, which ->
            SharedPrefUtils.saveData(this, "isTerms", true)
            SharedPrefUtils.saveData(this, "isFirst", true)
            SharedPrefUtils.saveData(applicationContext, "base_url", getString(R.string.site_url))
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finishAffinity()
            dialog.dismiss()


        }.show()
    }

   /* private fun setupHyperlink(subtitle: TextView) {
        subtitle.movementMethod = LinkMovementMethod.getInstance()
    }*/

    override fun onBackPressed() {
        finishAffinity()
    }
}

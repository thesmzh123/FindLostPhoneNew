package device.spotter.finder.appss.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contact_owner.*
import kotlinx.android.synthetic.main.banner.*
import device.spotter.finder.appss.R

class ContactOwnerActivity : BaseActivity() {
    private var title: String = ""
    private var message: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_owner)
        setTitle(R.string.app_name)
        adView(adView)
        val extra = intent.extras
        title = extra?.getString("title").toString()
        message = extra?.getString("message").toString()
        messageCustom.text = message
        numberCustom.text = getString(R.string.contact_number) + " " + title

        contactBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$title")
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        finish()
    }

}

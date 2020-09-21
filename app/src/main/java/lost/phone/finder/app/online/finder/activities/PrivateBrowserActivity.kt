@file:Suppress("NAME_SHADOWING", "DEPRECATION", "UNUSED_ANONYMOUS_PARAMETER")

package lost.phone.finder.app.online.finder.activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslCertificate
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.ProgressBar
import androidx.annotation.NonNull
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_private_browser.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI

class PrivateBrowserActivity : BaseActivity() {
    private val searchUrl = "https://www.google.com/search?q=%s"
    private var urlSearch: TextInputEditText? = null
    private var webview: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        setContentView(R.layout.activity_private_browser)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        urlSearch = findViewById(R.id.urlSearch)
        webview = findViewById(R.id.searchwebsite)
        createWebView()
        if (intent.getStringExtra("webAddress") != null) {
            loadUrl(intent.getStringExtra("webAddress").toString())
            webview!!.requestFocus()
            hideKeyboard()
        } else {
            loadUrl("https://www.google.com/")
        }
        urlSearch!!.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (TextUtils.isEmpty(urlSearch!!.text)) {
                    showToast(getString(R.string.something_to_search))

                } else {
                    loadUrl(urlSearch!!.text.toString())
                    webview!!.requestFocus()
                    hideKeyboard()
                }
                return@setOnKeyListener true
            } else {
                return@setOnKeyListener false
            }
        }

        searchOp.setOnClickListener {
            if (TextUtils.isEmpty(urlSearch!!.text)) {
                showToast(getString(R.string.something_to_search))

            } else {
                loadUrl(urlSearch!!.text.toString())
                webview!!.requestFocus()
                hideKeyboard()
            }
        }
    }

    override fun onBackPressed() {
        if (webview?.canGoBack()!!) {
            webview?.goBack()
        } else {
            finish()
        }
    }

    private fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlSearch!!.windowToken, 0)
    }

    private fun loadUrl(url: String) {
        var url = url
        url = url.trim { it <= ' ' }
        if (url.isEmpty()) {
            url = "about:blank"
        }
        url =
            if (url.startsWith("about:") || url.startsWith("javascript:") || url.startsWith("file:") || url.startsWith(
                    "data:"
                ) ||
                url.indexOf(' ') == -1 && Patterns.WEB_URL.matcher(url).matches()
            ) {
                val indexOfHash = url.indexOf('#')
                val guess = URLUtil.guessUrl(url)
                if (indexOfHash != -1 && guess.indexOf('#') == -1) {
                    // Hash exists in original URL but no hash in guessed URL
                    guess + url.substring(indexOfHash)
                } else {
                    guess
                }
            } else {
                URLUtil.composeSearchUrl(url, searchUrl, "%s")
            }
        webview?.loadUrl(url)
        hideKeyboard()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        val progressBar: ProgressBar = progressBar
        val settings = webview!!.settings
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        settings.allowUniversalAccessFromFileURLs = true

        settings.javaScriptEnabled = true
        settings.pluginState = WebSettings.PluginState.ON
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCacheEnabled(true)
        settings.domStorageEnabled = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        webview!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.progress = newProgress
                }
            }

        }
        webview!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView,
                url: String,
                favicon: Bitmap?
            ) {
                progressBar.progress = 0
                progressBar.visibility = View.VISIBLE
                urlSearch!!.setText(url)
                urlSearch!!.setSelection(0)
                view.requestFocus()

            }

            override fun onPageFinished(view: WebView, url: String) {
                // Don't use the argument url here since navigation to that URL might have been
                // cancelled due to SSL error

                Log.d(TAGI, "onPageFinished: ")
            }


            override fun onLoadResource(view: WebView, url: String) {
                try {
                    val page = view.url
                    Log.d(TAGI, "url1: $page")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val sslErrors = arrayOf(
                "Not yet valid",
                "Expired",
                "Hostname mismatch",
                "Untrusted CA",
                "Invalid date",
                "Unknown error"
            )

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                val primaryError = error.primaryError
                val errorStr =
                    if (primaryError >= 0 && primaryError < sslErrors.size) sslErrors[primaryError] else "Unknown error $primaryError"
                MaterialAlertDialogBuilder(
                    this@PrivateBrowserActivity,
                    R.style.MaterialAlertDialogTheme
                )
                    .setTitle("Insecure connection")
                    .setMessage(
                        String.format(
                            "Error: %s\nURL: %s\n\nCertificate:\n%s",
                            errorStr,
                            error.url,
                            certificateToStr(error.certificate)
                        )
                    )
                    .setPositiveButton(
                        getString(R.string.proceed)
                    ) { dialog: DialogInterface?, which: Int -> handler.proceed() }
                    .setNegativeButton(
                        getString(R.string.cancel)
                    ) { dialog: DialogInterface?, which: Int -> handler.cancel() }
                    .show()
            }
        }

    }

    @SuppressLint("DefaultLocale")
    private fun certificateToStr(certificate: SslCertificate?): String? {
        if (certificate == null) {
            return null
        }
        var s = ""
        val issuedTo = certificate.issuedTo
        if (issuedTo != null) {
            s += """
                Issued to: ${issuedTo.dName}
                
                """.trimIndent()
        }
        val issuedBy = certificate.issuedBy
        if (issuedBy != null) {
            s += """
                Issued by: ${issuedBy.dName}
                
                """.trimIndent()
        }
        val issueDate = certificate.validNotBeforeDate
        if (issueDate != null) {
            s += String.format("Issued on: %tF %tT %tz\n", issueDate, issueDate, issueDate)
        }
        val expiryDate = certificate.validNotAfterDate
        if (expiryDate != null) {
            s += String.format(
                "Expires on: %tF %tT %tz\n",
                expiryDate,
                expiryDate,
                expiryDate
            )
        }
        return s
    }

    private val mOnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {

            @SuppressLint("SetJavaScriptEnabled")
            override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.action_backward -> {
                        if (webview!!.canGoBack()) {

                            webview!!.goBack()
                        }
                        return true
                    }
                    R.id.action_forward -> {
                        if (webview!!.canGoForward()) {
                            webview!!.goForward()
                        }
                        return true
                    }

                    R.id.action_addbookmark -> {
                        if (webview!!.url.isNullOrEmpty()) {
                            showToast(getString(R.string.no_url_to_book))
                        } else {
                            if (databaseHelperUtils!!.checkBookmarkExist(webview!!.title.toString())) {
                                showToast(getString(R.string.bookmark_already_exist))
                            } else {
                                databaseHelperUtils!!.addbookmark(
                                    webview!!.title.toString(),
                                    webview!!.url.toString()
                                )
                                showToast(getString(R.string.added_success))
                            }
                        }

                        return true
                    }
                    R.id.action_viewaddbookmark -> {
                        startActivity(
                            Intent(
                                this@PrivateBrowserActivity,
                                ViewBookmarkActivity::class.java
                            )
                        )
                        finish()
                        return true
                    }
                }
                return false
            }
        }

}
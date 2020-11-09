@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package device.spotter.finder.appss.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import device.spotter.finder.appss.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_view_bookmark.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.adapters.BookmarkAdapter
import device.spotter.finder.appss.models.Bookmarks
import device.spotter.finder.appss.utils.Constants.TAGI
import device.spotter.finder.appss.utils.RecyclerClickListener
import device.spotter.finder.appss.utils.RecyclerTouchListener

class ViewBookmarkActivity : BaseActivity() {
    private var bookmarkList: ArrayList<Bookmarks>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_bookmark)
        title = getString(R.string.view_bookmark_s)
        addBackArrow()
        bookmarkList = databaseHelperUtils!!.getAllBookmarks()
        val adapter = BookmarkAdapter(this@ViewBookmarkActivity, bookmarkList!!)
        recyclerView.adapter = adapter
        checkEmptyState()
        recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                this,
                recyclerView,
                object : RecyclerClickListener {
                    override fun onClick(view: View?, position: Int) {
                        val websites = bookmarkList!![position]
                        if (InternetConnection().checkConnection(this@ViewBookmarkActivity)) {

                            if (!SharedPrefUtils.getBooleanData(
                                    this@ViewBookmarkActivity,
                                    "hideAds"
                                )
                            ) {
                                if (interstitial.isLoaded) {
                                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                                            Lifecycle.State.STARTED
                                        )
                                    ) {
                                        interstitial.show()
                                    } else {
                                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                                    }
                                } else {
                                    openActivity(websites.address)
                                }
                                interstitial.adListener = object : AdListener() {
                                    override fun onAdClosed() {
                                        requestNewInterstitial()
                                        openActivity(websites.address)


                                    }
                                }
                            } else {
                                openActivity(websites.address)


                            }
                        } else {
                            showToast(getString(R.string.no_internet))
                        }

                    }

                    override fun onLongClick(view: View?, position: Int) {
                        Log.d(TAGI, "onLongClick")
                        if (!SharedPrefUtils.getBooleanData(this@ViewBookmarkActivity, "hideAds")) {
                            if (interstitial.isLoaded) {
                                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                                        Lifecycle.State.STARTED
                                    )
                                ) {
                                    interstitial.show()
                                } else {
                                    Log.d(
                                        TAGI,
                                        "App Is In Background Ad Is Not Going To Show"
                                    )

                                }
                            } else {
                                deleteBookmark(position, adapter)

                            }
                            interstitial.adListener = object : AdListener() {
                                override fun onAdClosed() {
                                    requestNewInterstitial()
                                    deleteBookmark(position, adapter)

                                }
                            }
                        } else {
                            deleteBookmark(position, adapter)

                        }

                    }
                })
        )
        loadInterstial()

    }

    override fun onBackPressed() {
        startActivity(
            Intent(
                this@ViewBookmarkActivity,
                PrivateBrowserActivity::class.java
            )
        )
        finish()
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            startActivity(
                Intent(
                    this@ViewBookmarkActivity,
                    PrivateBrowserActivity::class.java
                )
            )
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun deleteBookmark(position: Int, adapter: BookmarkAdapter) {
        val yesNoDialog =
            MaterialAlertDialogBuilder(
                this@ViewBookmarkActivity, R.style.MaterialAlertDialogTheme
            )
        //yes or no alert box
        yesNoDialog.setMessage(getString(R.string.delete_this_bookmark))
            .setCancelable(false)
            .setNegativeButton(
                getString(R.string.no)
            ) { dialog: DialogInterface?, which: Int ->
                dialog?.dismiss()
            }
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialogInterface: DialogInterface?, i: Int ->
                try {
                    val websites = bookmarkList!![position]
                    databaseHelperUtils!!.deletebookmark(websites.id)
                    bookmarkList!!.removeAt(position)
                    adapter.notifyItemChanged(position)
                    adapter.notifyItemRangeRemoved(0, bookmarkList!!.size)
                    checkEmptyState()
                    dialogInterface!!.dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        val dialog = yesNoDialog.create()
        dialog.show()
    }

    private fun checkEmptyState() {
        if (bookmarkList!!.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    private fun openActivity(text: String) {
        val intent = Intent(this, PrivateBrowserActivity::class.java)
        intent.putExtra("webAddress", text)
        startActivity(intent)
        finish()
    }
}
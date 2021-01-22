@file:Suppress("DEPRECATION")

package device.spotter.finder.appss.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.custom_feature_layout.view.*
import kotlinx.android.synthetic.main.feature_icon_layout.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.native_ad_layout.view.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.FamilyLocatorActivity
import device.spotter.finder.appss.adapters.ToolsAdapter
import device.spotter.finder.appss.adapters.ViewPagerAdapter
import device.spotter.finder.appss.models.Tools
import device.spotter.finder.appss.utils.Constants.TAGI
import kotlinx.android.synthetic.main.facebook_native_ad_layout.view.*


@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class HomeFragment : BaseFragment() {

    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var lockView: View? = null
    private var eraseView: View? = null
    private var locView: View? = null
    private var alarmView: View? = null
    private var messageView: View? = null
    private var backupView: View? = null

    //    private var hideView: View? = null
    private var familyView: View? = null
    private var neworkView: View? = null
    private var toolList: ArrayList<Tools>? = null
    private var addItemAdapter: ToolsAdapter? = null

    private fun exit() {
        val yesNoDialog =
            MaterialAlertDialogBuilder(
                requireActivity()
                , R.style.MaterialAlertDialogTheme
            )
        //yes or no alert box
        yesNoDialog.setMessage(getString(R.string.do_you_want_exit)).setCancelable(false)
            .setNegativeButton(
                getString(R.string.rate_us)
            ) { dialog: DialogInterface?, which: Int ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().packageName)
                    )
                )
            }
            .setPositiveButton(
                getString(R.string.exit)
            ) { dialogInterface: DialogInterface?, i: Int -> requireActivity().finishAffinity() }
            .setNeutralButton(
                getString(R.string.cancel)
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        val dialog = yesNoDialog.create()
        dialog.show()
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_home, container, false)
        viewPagerAdapter = ViewPagerAdapter()
        toolList = ArrayList()

        root!!.viewpager.adapter = viewPagerAdapter
        root!!.dots_indicator.setViewPager(root!!.viewpager)
        root!!.viewpager.pageMargin = 15
        init()
        viewPagerAdapter!!.notifyDataSetChanged()


        root!!.backFeature.setOnClickListener {
            if (!isFirstPage()) {
                root!!.viewpager.setCurrentItem(root!!.viewpager.currentItem - 1, true)
            }
        }
        root!!.forwardFeature.setOnClickListener {
            if (!isLastPage()) {
                root!!.viewpager.setCurrentItem(root!!.viewpager.currentItem + 1, true)
            }
        }

        root!!.viewpager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                handleVisibility()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        initTool()
        refreshAd(root!!.nativeAd, R.layout.ad_unified,root!!.native_ad_container)
        root!!.nestedScrollView.fullScroll(View.FOCUS_UP)
        root!!.nestedScrollView.scrollTo(0, 0)
        return root!!
    }


    private fun initTool() {
        addItemAdapter = ToolsAdapter(toolsList(), requireActivity())
        val horizontalLayoutManagaer =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        root!!.recyclerView.layoutManager = horizontalLayoutManagaer
        root!!.recyclerView.itemAnimator = DefaultItemAnimator()
        root!!.recyclerView.adapter = addItemAdapter
        addItemAdapter!!.notifyDataSetChanged()
        root!!.recyclerViewIndicator.setRecyclerView(root!!.recyclerView)

        /*   root!!.recyclerView.addOnItemTouchListener(
               RecyclerTouchListener(
                   requireActivity(),
                   root!!.recyclerView,
                   object : RecyclerClickListener {
                       override fun onClick(view: View?, position: Int) {
                           if (position == 0) {
                               baseContext!!.navigateFragmentByAds(R.id.compassFragment)

                           } else if (position == 1) {
                               holderTool = ToolsAdapter.MyHolder(view!!)
                               checkFlash()
                               if (level <= 15) {
                                   showToast(getString(R.string.battery_low_flash))
                               } else {
                                   openFlash()
                               }
                           } else if (position == 2) {
                               startActivity(
                                   Intent(
                                       requireActivity(),
                                       PrivateBrowserActivity::class.java
                                   )
                               )
                           }
                       }

                       override fun onLongClick(view: View?, position: Int) {
                           Log.d(TAGI, "onLongClick")

                       }
                   })
           )*/
    }


    private fun toolsList(): ArrayList<Tools> {
        toolList!!.add(Tools(R.drawable.compass_icon, getString(R.string.compass)))
        toolList!!.add(Tools(R.drawable.torch, getString(R.string.torch)))
        toolList!!.add(Tools(R.drawable.private_browser_icon, getString(R.string.private_browser)))
        return toolList!!
    }

    private fun isLastPage(): Boolean {
        return root!!.viewpager.currentItem == root!!.viewpager.adapter!!.count - 1
    }

    private fun isFirstPage(): Boolean {
        return root!!.viewpager.currentItem == 0
    }

    private fun handleVisibility() {
        if (isFirstPage()) {
            root!!.backFeature.visibility = View.INVISIBLE
        } else {
            root!!.backFeature.visibility = View.VISIBLE
        }
        if (isLastPage()) {
            root!!.forwardFeature.visibility = View.INVISIBLE
        } else {
            root!!.forwardFeature.visibility = View.VISIBLE
        }
    }

    @SuppressLint("InflateParams")
    private fun init() {
        lockView = layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(lockView)
        eraseView = layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(eraseView)
        locView = layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(locView)
        alarmView = layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(alarmView)
        messageView =
            layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(messageView)
        backupView =
            layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(backupView)
        /* hideView =
             layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
         viewPagerAdapter!!.addView(hideView)*/
        familyView =
            layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(familyView)
        neworkView =
            layoutInflater.inflate(R.layout.custom_feature_layout, null, false) as ViewGroup
        viewPagerAdapter!!.addView(neworkView)

        setLockView()
        setEraseView()
        setLocView()
        setAlarmView()
        setmessageView()
        setbackupView()
//        sethideView()
        setFamilyView()
        setNetworkView()

    }

    private fun setNetworkView() {
        Glide.with(requireActivity()).load(R.drawable.network_feature)
            .into(neworkView!!.featureIcon)
        neworkView!!.featureTitle.text = getString(R.string.newtork_provider)
        neworkView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_network_your_phone)
        neworkView!!.featureBtn.text = getString(R.string.find)
        if (getScreenWidth() < 720 && getScreenHeight() < 1384) {
            neworkView!!.featureDescp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14F)
        }
        neworkView!!.cardClick.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.networkProviderFragment)
        }
        neworkView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.networkProviderFragment)

        }
    }

    private fun setFamilyView() {
        Glide.with(requireActivity()).load(R.drawable.location_feature)
            .into(familyView!!.featureIcon)
        familyView!!.featureTitle.text = getString(R.string.family_locator)
        familyView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_family_your_phone)
        familyView!!.featureBtn.text = getString(R.string.locate)
        if (getScreenWidth() < 720 && getScreenHeight() < 1384) {
            familyView!!.featureDescp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15F)
            familyView!!.featureTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18F)
        }
        familyView!!.cardClick.setOnClickListener {
            if (isLoggedIn()) {
                callFamilyLocatorActivity()
            } else {
                showToast(getString(R.string.login_to_use_this))
            }
        }
        familyView!!.featureBtn.setOnClickListener {
            if (isLoggedIn()) {
                callFamilyLocatorActivity()
            } else {
                showToast(getString(R.string.login_to_use_this))
            }
        }
    }

    private fun callFamilyLocatorActivity() {
        if (!SharedPrefUtils.getBooleanData(requireActivity(), "hideAds")) {
            if (interstitial.isLoaded) {
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    interstitial.show()
                } else {
                    Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                }
            } else {
                if (baseContext!!.interstitialAdFb!!.isAdLoaded) {
                    baseContext!!.interstitialAdFb!!.show()
                } else {
                    val phone =
                        SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                    if (phone.isEmpty() || phone.equals("null", true)) {
                        openProfileNUmDialog()
                    } else {
                        startActivity(Intent(requireActivity(), FamilyLocatorActivity::class.java))
                    }
                }
                val interstitialAdListener: InterstitialAdListener =
                    object : InterstitialAdListener {
                        override fun onInterstitialDisplayed(ad: Ad?) {
                            // Interstitial ad displayed callback
                            Log.e(TAGI, "Interstitial ad displayed.")
                        }

                        override fun onInterstitialDismissed(ad: Ad?) {
                            // Interstitial dismissed callback
                            baseContext!!.loadFbInter()
                            Log.e(TAGI, "Interstitial ad dismissed.")
                            val phone =
                                SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                            if (phone.isEmpty() || phone.equals("null", true)) {
                                openProfileNUmDialog()
                            } else {
                                startActivity(Intent(requireActivity(), FamilyLocatorActivity::class.java))
                            }
                        }

                        override fun onError(ad: Ad?, adError: AdError) {
                            // Ad error callback
                            Log.e(
                                TAGI,
                                "Interstitial ad failed to load: " + adError.errorMessage
                            )
                        }

                        override fun onAdLoaded(p0: Ad?) {
                            Log.d(TAGI, "onAdLoaded: ")
                        }

                        override fun onAdClicked(ad: Ad?) {
                            // Ad clicked callback
                            Log.d(TAGI, "Interstitial ad clicked!")
                        }

                        override fun onLoggingImpression(ad: Ad?) {
                            // Ad impression logged callback
                            Log.d(TAGI, "Interstitial ad impression logged!")
                        }
                    }


                // For auto play video ads, it's recommended to load the ad
                // at least 30 seconds before it is shown
                baseContext!!.interstitialAdFb!!.loadAd(
                    baseContext!!.interstitialAdFb!!.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build()
                )

            }
            interstitial.adListener = object : AdListener() {
                override fun onAdClosed() {
                    requestNewInterstitial()
                    val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                    if (phone.isEmpty() || phone.equals("null", true)) {
                        openProfileNUmDialog()
                    } else {
                        startActivity(Intent(requireActivity(), FamilyLocatorActivity::class.java))
                    }
                }
            }
        } else {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                startActivity(Intent(requireActivity(), FamilyLocatorActivity::class.java))
            }

        }
    }

/*    private fun sethideView() {
        Glide.with(requireActivity()).load(R.drawable.hide_featire)
            .into(hideView!!.featureIcon)
        hideView!!.featureTitle.text = getString(R.string.hide_icon)
        hideView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_hide_your_phone)
        hideView!!.featureBtn.text = getString(R.string.hide)
        hideView!!.cardClick.setOnClickListener {
            baseContext!!.navigateFragment(R.id.hideAppFragment)
        }
        hideView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragment(R.id.hideAppFragment)

        }
    }*/

    private fun setbackupView() {
        Glide.with(requireActivity()).load(R.drawable.backup_feature)
            .into(backupView!!.featureIcon)
        backupView!!.featureTitle.text = getString(R.string.backup_and_restore)
        backupView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_backup_your_phone)
        backupView!!.featureBtn.text = getString(R.string.view)
        backupView!!.cardClick.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.backupRestoreFragment)
        }
        backupView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.backupRestoreFragment)

        }
    }

    private fun setmessageView() {
        Glide.with(requireActivity()).load(R.drawable.message_feature)
            .into(messageView!!.featureIcon)
        messageView!!.featureTitle.text = getString(R.string.send_my_phone)
        messageView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_send_your_phone)
        messageView!!.featureBtn.text = getString(R.string.last_hope)
        if (getScreenWidth() < 720 && getScreenHeight() < 1384) {
            messageView!!.featureDescp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14F)
        }
/*        messageView!!.featureBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, 17F)
        val params: LinearLayout.LayoutParams =
            messageView!!.featureBtn.getLayoutParams() as LinearLayout.LayoutParams
        params.width = 240
        params.height = 80
        messageView!!.featureBtn.setLayoutParams(params)*/
        messageView!!.cardClick.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.lastHopeFragment)
            }
        }
        messageView!!.featureBtn.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.lastHopeFragment)
            }

        }
    }


    private fun setAlarmView() {
        Glide.with(requireActivity()).load(R.drawable.riing_feature)
            .into(alarmView!!.featureIcon)
        alarmView!!.featureTitle.text = getString(R.string.alarm_my_phone)
        alarmView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_alarm_your_phone)
        alarmView!!.featureBtn.text = getString(R.string.alarm)

        alarmView!!.cardClick.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.ringPhoneFragment)
            }
        }
        alarmView!!.featureBtn.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.ringPhoneFragment)
            }

        }
    }

    private fun setLocView() {
        Glide.with(requireActivity()).load(R.drawable.location_feature).into(locView!!.featureIcon)
        locView!!.featureTitle.text = getString(R.string.locate_my_phone)
        locView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_loc_your_phone)
        locView!!.featureBtn.text = getString(R.string.locate)
        locView!!.cardClick.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.lostPhoneLocFragment)
            }
        }
        locView!!.featureBtn.setOnClickListener {
            val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
            if (phone.isEmpty() || phone.equals("null", true)) {
                openProfileNUmDialog()
            } else {
                baseContext!!.navigateFragmentByAds(R.id.lostPhoneLocFragment)
            }
        }
        if (getScreenWidth() < 720 && getScreenHeight() < 1384) {
            locView!!.featureDescp.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14F)
        }
    }

    private fun setEraseView() {
        Glide.with(requireActivity()).load(R.drawable.erase_fature).into(eraseView!!.featureIcon)
        eraseView!!.featureTitle.text = getString(R.string.erase_my_phone)
        eraseView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_erase_your_phone)
        eraseView!!.featureBtn.text = getString(R.string.erase)
        eraseView!!.cardClick.setOnClickListener {
            if (!isDeviceAdmin() || !SharedPrefUtils.getBooleanData(
                    requireActivity(),
                    "isDevice"
                )
            ) {

                deviceAdminDialog()

            } else {
                val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                if (phone.isEmpty() || phone.equals("null", true)) {
                    openProfileNUmDialog()
                } else {
                    baseContext!!.navigateFragmentByAdsActivate(R.id.eraseDataFragment)
                }
            }
        }
        eraseView!!.featureBtn.setOnClickListener {
            if (!isDeviceAdmin() || !SharedPrefUtils.getBooleanData(
                    requireActivity(),
                    "isDevice"
                )
            ) {

                deviceAdminDialog()

            } else {
                val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                if (phone.isEmpty() || phone.equals("null", true)) {
                    openProfileNUmDialog()
                } else {
                    baseContext!!.navigateFragmentByAdsActivate(R.id.eraseDataFragment)
                }
            }
        }
    }

    private fun setLockView() {
        Glide.with(requireActivity()).load(R.drawable.lock_feature).into(lockView!!.featureIcon)
        lockView!!.featureTitle.text = getString(R.string.secure_my_phone)
        lockView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_lock_your_phone)
        lockView!!.featureBtn.text = getString(R.string.secure)
        lockView!!.cardClick.setOnClickListener {
            if (!isDeviceAdmin() || !SharedPrefUtils.getBooleanData(
                    requireActivity(),
                    "isDevice"
                )
            ) {

                deviceAdminDialog()

            } else {
                val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                if (phone.isEmpty() || phone.equals("null", true)) {
                    openProfileNUmDialog()
                } else {
                    baseContext!!.navigateFragmentByAdsActivate(R.id.lockPhoneFragment)
                }
            }
        }
        lockView!!.featureBtn.setOnClickListener {
            if (!isDeviceAdmin() || !SharedPrefUtils.getBooleanData(
                    requireActivity(),
                    "isDevice"
                )
            ) {

                deviceAdminDialog()

            } else {
                val phone = SharedPrefUtils.getStringData(requireActivity(), "phoneNum").toString()
                if (phone.isEmpty() || phone.equals("null", true)) {
                    openProfileNUmDialog()
                } else {
                    baseContext!!.navigateFragmentByAdsActivate(R.id.lockPhoneFragment)
                }
            }
        }

    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    addItemAdapter!!.switchOffTorch()
                    addItemAdapter!!.isTorchOn = false
                    exit()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }


    override fun onStop() {
        super.onStop()
        try {
            addItemAdapter!!.switchOffTorch()
            addItemAdapter!!.isTorchOn = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
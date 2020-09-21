@file:Suppress("DEPRECATION")

package lost.phone.finder.app.online.finder.fragments

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.Nullable
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.custom_feature_layout.view.*
import kotlinx.android.synthetic.main.feature_icon_layout.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.FamilyLocatorActivity
import lost.phone.finder.app.online.finder.activities.PrivateBrowserActivity
import lost.phone.finder.app.online.finder.adapters.ToolsAdapter
import lost.phone.finder.app.online.finder.adapters.ViewPagerAdapter
import lost.phone.finder.app.online.finder.models.Tools
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.RecyclerClickListener
import lost.phone.finder.app.online.finder.utils.RecyclerTouchListener


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

    private var camera: Camera? = null
    private var parameters: Camera.Parameters? = null
    private var mCameraManager: CameraManager? = null
    private var mCameraId: String? = null
    private var isTorchOn: Boolean? = null
    var level: Int = 0

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
        torchInit()
        initTool()

        return root!!
    }

    private fun torchInit() {
        isTorchOn = false
        mCameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            mCameraId = mCameraManager!!.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        getBatteryPercentage()

    }

    //Battery Percentage
    private fun getBatteryPercentage() {
        val batteryLevelReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context, intent: Intent) {
                context.unregisterReceiver(this)
                val currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level = -1
                if (currentLevel >= 0 && scale > 0) {
                    level = currentLevel * 100 / scale
                }
            }
        }
        val batteryLevelFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(batteryLevelReceiver, batteryLevelFilter)
    }

    private fun initTool() {
        val addItemAdapter = ToolsAdapter(toolsList(), requireActivity())
        val horizontalLayoutManagaer =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        root!!.recyclerView.layoutManager = horizontalLayoutManagaer
        root!!.recyclerView.itemAnimator = DefaultItemAnimator()
        root!!.recyclerView.adapter = addItemAdapter
        addItemAdapter.notifyDataSetChanged()
        root!!.recyclerViewIndicator.setRecyclerView(root!!.recyclerView)

        root!!.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                requireActivity(),
                root!!.recyclerView,
                object : RecyclerClickListener {
                    override fun onClick(view: View?, position: Int) {
                        if (position == 0) {
                            baseContext!!.navigateFragmentByAds(R.id.compassFragment)

                        } else if (position == 1) {
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
        )
    }

    private fun openFlash() {
        isTorchOn = if (isTorchOn!!) {
            switchOffTorch()
            false

        } else {
            switchOnTorch()
            true

        }
    }

    private fun switchOnTorch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCameraId?.let { mCameraManager?.setTorchMode(it, true) }
        } else {
            try {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera?.parameters = parameters
                camera?.startPreview()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    private fun switchOffTorch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCameraId?.let { mCameraManager?.setTorchMode(it, false) }
        } else {
            try {
                parameters?.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera?.parameters = parameters
                camera?.stopPreview()
            } catch (e: Exception) {
                e.printStackTrace()

            }

        }
    }

    private fun checkFlash() {
        val isFlashAvailable = requireActivity().packageManager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FLASH
        )
        if (!isFlashAvailable) {

            val alert = MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.MaterialAlertDialogTheme
            ).create()
            alert.setTitle(getString(R.string.error))
            alert.setMessage(getString(R.string.no_flash_light))
            alert.setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.ok)
            ) { dialog, which ->
                // closing the application
                dialog.dismiss()

            }
            alert.show()
            return
        }
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
           startActivity(Intent(requireActivity(),FamilyLocatorActivity::class.java))
        }
        familyView!!.featureBtn.setOnClickListener {
            startActivity(Intent(requireActivity(),FamilyLocatorActivity::class.java))

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
            baseContext!!.navigateFragmentByAds(R.id.lastHopeFragment)
        }
        messageView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.lastHopeFragment)

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
            baseContext!!.navigateFragment(R.id.ringPhoneFragment)
        }
        alarmView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragment(R.id.ringPhoneFragment)

        }
    }

    private fun setLocView() {
        Glide.with(requireActivity()).load(R.drawable.location_feature).into(locView!!.featureIcon)
        locView!!.featureTitle.text = getString(R.string.locate_my_phone)
        locView!!.featureDescp.text =
            getString(R.string.with_this_feature_you_can_remotely_loc_your_phone)
        locView!!.featureBtn.text = getString(R.string.locate)
        locView!!.cardClick.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.lostPhoneLocFragment)
        }
        locView!!.featureBtn.setOnClickListener {
            baseContext!!.navigateFragmentByAds(R.id.lostPhoneLocFragment)

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
                baseContext!!.navigateFragmentByAdsActivate(R.id.eraseDataFragment)
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
                baseContext!!.navigateFragmentByAdsActivate(R.id.eraseDataFragment)
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
                baseContext!!.navigateFragmentByAdsActivate(R.id.lockPhoneFragment)
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
                baseContext!!.navigateFragmentByAdsActivate(R.id.lockPhoneFragment)
            }
        }

    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    switchOffTorch()
                    isTorchOn = false
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }


    override fun onStop() {
        super.onStop()
        switchOffTorch()
        isTorchOn = false
    }


}
package lost.phone.finder.app.online.finder.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.drawer_custom_layout.view.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.PermissionsUtils


class MainActivity : BaseActivity(), NavController.OnDestinationChangedListener {
    private var headerView: View? = null
    private var appBarConfiguration: AppBarConfiguration? = null
    private var viewHome: View? = null
    private var viewProfile: View? = null
    private var viewNearby: View? = null
    private var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        /*   val data = intent.data
           if (data != null) {
               val path = intent.data!!.path
               Log.d(TAGI, "onCreate: $path")
               *//*  if (!path!!.contains("messages") || !path.contains(".com")) {
                  Log.d(TAGI, "onCreate: " + FilePathUtil.getPath(this@MainActivity, data))
              }*//*
        }*/
        if (!SharedPrefUtils.getBooleanData(this@MainActivity, "isTerms")) {
            startActivity(Intent(applicationContext, TermsAndConditionsActivity::class.java))
            finish()

        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                val permissionsUtils = PermissionsUtils().getInstance(this)
                if (permissionsUtils?.isAllPermissionAvailable()!!) {
                    Log.d("Test", "Permission")
                } else {
                    permissionsUtils.setActivity(this)
                    permissionsUtils.requestPermissionsIfDenied()
                }
            }
            navigationView = findViewById(R.id.nav_view)
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_profile,
                    R.id.nav_nearby,
                    R.id.nav_family,
                    R.id.nav_rate,
                    R.id.nav_share
                ), drawer_layout
            )
            setupActionBarWithNavController(navController, appBarConfiguration!!)
            navigationView!!.setupWithNavController(navController)
            drawer_layout!!.setViewScale(Gravity.START, 0.9f)
            drawer_layout!!.setViewElevation(Gravity.START, 20f)
            headerView = navigationView!!.getHeaderView(0)

            val actionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                drawer_layout!!,
                toolbar,
                R.string.open_drawer,
                R.string.close_drawer
            )
            drawer_layout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()
            toolbar.setNavigationIcon(R.drawable.menu_icon)
            /*      val drawable =
                 ResourcesCompat.getDrawable(resources, R.drawable.menu_icon, theme)
             actionBarDrawerToggle.setHomeAsUpIndicator(drawable)
             actionBarDrawerToggle.setDrawerIndicatorEnabled(false)*/
            navController.addOnDestinationChangedListener(this)


            addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            updateNavView()
            loadInterstial()
        }
    }

    private fun setViewToNav() {
        val homeItem = navigationView!!.menu.findItem(R.id.nav_home)
        homeItem.setActionView(R.layout.drawer_custom_layout)
        viewHome = homeItem.actionView

        val profileItem = navigationView!!.menu.findItem(R.id.nav_profile)
        profileItem.setActionView(R.layout.drawer_custom_layout)
        viewProfile = profileItem.actionView
        val nearbyItem = navigationView!!.menu.findItem(R.id.nav_nearby)
        nearbyItem.setActionView(R.layout.drawer_custom_layout)
        viewNearby = nearbyItem.actionView
    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration!!) || super.onSupportNavigateUp()
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.profile_menu, menu)
        this.menu = menu
        changeMenu()

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_menu -> {
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        setViewToNav()
        when (controller.currentDestination!!.id) {
            R.id.nav_home -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)
                /*     if (menu != null) {
                         menu!!.findItem(R.id.profile_menu).isVisible = true
                     }*/
                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.nav_profile -> {
//                menu!!.findItem(R.id.profile_menu).isVisible = false
                toolbar.setNavigationIcon(R.drawable.menu_icon)


                addMenuLayout(isHome = false, isProfile = true, isNearby = false)
            }
            R.id.nav_nearby -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = false, isProfile = false, isNearby = true)
            }
            R.id.lostPhoneLocFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.mapViewFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.lockPhoneFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.eraseDataFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.ringPhoneFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.lastHopeFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.hideAppFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.compassFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.networkProviderFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }
            R.id.backupRestoreFragment -> {
                toolbar.setNavigationIcon(R.drawable.menu_icon)

                addMenuLayout(isHome = true, isProfile = false, isNearby = false)
            }

        }
    }

    private fun addMenuLayout(isHome: Boolean, isProfile: Boolean, isNearby: Boolean) {
        setViewToNav()
        //Home menu

        Glide.with(this).load(R.drawable.home).into(viewHome!!.navIcon)
        viewHome!!.navText.text = getString(R.string.home)

        //profile menu

        Glide.with(this).load(R.drawable.profile).into(viewProfile!!.navIcon)
        viewProfile!!.navText.text = getString(R.string.profile)
        //nearby menu

        Glide.with(this).load(R.drawable.nearbyplaces).into(viewNearby!!.navIcon)
        viewNearby!!.navText.text = getString(R.string.nearby_places)

        //share menu
        val shareItem = navigationView!!.menu.findItem(R.id.nav_share)
        shareItem.setActionView(R.layout.drawer_custom_layout)
        val viewShare = shareItem.actionView
        Glide.with(this).load(R.drawable.share).into(viewShare.navIcon)
        viewShare.navText.text = getString(R.string.share)
        viewShare.setOnClickListener {
            closeDrawer()
            shareApp()
        }
        //rate menu
        val rateItem = navigationView!!.menu.findItem(R.id.nav_rate)
        rateItem.setActionView(R.layout.drawer_custom_layout)
        val viewRate = rateItem.actionView
        Glide.with(this).load(R.drawable.rate_us).into(viewRate.navIcon)
        viewRate.navText.text = getString(R.string.rate_us)
        viewRate.setOnClickListener {
            closeDrawer()
            rateUs()
        }
        //family menu
        val familyItem = navigationView!!.menu.findItem(R.id.nav_family)
        familyItem.setActionView(R.layout.drawer_custom_layout)
        val family = familyItem.actionView
        Glide.with(this).load(R.drawable.family_locator_icon).into(family.navIcon)
        family.navText.text = getString(R.string.family_locator1)
        family.setOnClickListener {
            closeDrawer()
            callFamilyLocatorActivity()
        }
        //browser menu
        val browserItem = navigationView!!.menu.findItem(R.id.nav_browser)
        browserItem.setActionView(R.layout.drawer_custom_layout)
        val browser = browserItem.actionView
        Glide.with(this).load(R.drawable.global).into(browser.navIcon)
        browser.navText.text = getString(R.string.browse_website)
        browser.setOnClickListener {
            closeDrawer()
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.site_url)))
            startActivity(browserIntent)
        }

        when {
            isHome -> {
                viewHome!!.view.visibility = View.VISIBLE
                viewProfile!!.view.visibility = View.INVISIBLE
                viewNearby!!.view.visibility = View.INVISIBLE
            }
            isProfile -> {
                viewHome!!.view.visibility = View.INVISIBLE
                viewProfile!!.view.visibility = View.VISIBLE
                viewNearby!!.view.visibility = View.INVISIBLE
            }
            isNearby -> {
                viewHome!!.view.visibility = View.INVISIBLE
                viewProfile!!.view.visibility = View.INVISIBLE
                viewNearby!!.view.visibility = View.VISIBLE
            }
        }
    }

    private fun callFamilyLocatorActivity() {
        if (!SharedPrefUtils.getBooleanData(this@MainActivity, "hideAds")) {
            if (interstitial.isLoaded) {
                if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    interstitial.show()
                } else {
                    Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                }
            } else {
                startActivity(Intent(this@MainActivity, FamilyLocatorActivity::class.java))

            }
            interstitial.adListener = object : AdListener() {
                override fun onAdClosed() {
                    requestNewInterstitial()
                    startActivity(Intent(this@MainActivity, FamilyLocatorActivity::class.java))
                }
            }
        } else {
            startActivity(Intent(this@MainActivity, FamilyLocatorActivity::class.java))

        }
    }

    private fun closeDrawer() {
        if (drawer_layout!!.isDrawerOpen(GravityCompat.START)) {
            drawer_layout!!.closeDrawer(GravityCompat.START)
        }
    }

    fun updateNavView() {
        if (isLoggedIn()) {
            headerView!!.name.text = auth.currentUser!!.displayName
            headerView!!.name.isSelected = true
            headerView!!.email.text = auth.currentUser!!.email
            headerView!!.email.isSelected = true
            Glide.with(this).load(auth.currentUser?.photoUrl).into(headerView!!.profileImage)

        } else {
            headerView!!.name.text = getString(R.string.app_name)
            headerView!!.email.text = getString(R.string.track_your_phone)
            Glide.with(this).load(R.mipmap.ic_launcher).into(headerView!!.profileImage)

        }
    }


}
@file:Suppress("DEPRECATION")

package device.spotter.finder.appss.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.activity_splash_screen.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.utils.Constants.TAGI

class SplashScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)
        val animation = AnimationUtils.loadAnimation(this, R.anim.transition)
        val animationText = AnimationUtils.loadAnimation(this, R.anim.transition_text)
        // assigning animations to the widgets
        splashImage.startAnimation(animation)
        splashText.startAnimation(animation)
        splashText2.startAnimation(animationText)
        SharedPrefUtils.saveData(applicationContext, "base_url", getString(R.string.site_url))
        loadInterstial()
        fetchBaseUrl()
        fetchLicKey()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAGI, "on R")
        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            if (!SharedPrefUtils.getBooleanData(this, "isFirst")) {
                startNewActivtySplash(MainActivity())
            } else {
                startNewActivtySplashAds(MainActivity())
            }
        }, 4000)
    }

    override fun onBackPressed() {
        this@SplashScreenActivity.finish()

    }
    //TODO: start activity splash

    private fun startNewActivtySplash(activity: Activity) {
        startActivity(Intent(applicationContext, activity.javaClass))
        this@SplashScreenActivity.finish()
    }

    //TODO: start activity splash  as ads
    private fun startNewActivtySplashAds(activity: Activity) {
        if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {

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
                if (interstitialAdFb!!.isAdLoaded){
                    interstitialAdFb!!.show()
                }else {
                    startActivity(Intent(applicationContext, activity.javaClass))
                    this@SplashScreenActivity.finish()
                }
                val interstitialAdListener: InterstitialAdListener =
                    object : InterstitialAdListener {
                        override fun onInterstitialDisplayed(ad: Ad?) {
                            // Interstitial ad displayed callback
                            Log.e(TAGI, "Interstitial ad displayed.")
                        }

                        override fun onInterstitialDismissed(ad: Ad?) {
                            // Interstitial dismissed callback
                            loadFbInter()
                            Log.e(TAGI, "Interstitial ad dismissed.")
                            startActivity(Intent(applicationContext, activity.javaClass))
                            this@SplashScreenActivity.finish()
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
                interstitialAdFb!!.loadAd(
                    interstitialAdFb!!.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build()
                )
            }
            interstitial.adListener = object : AdListener() {
                override fun onAdClosed() {
                    requestNewInterstitial()
                    startActivity(Intent(applicationContext, activity.javaClass))
                    this@SplashScreenActivity.finish()

                }
            }
        } else {
            startActivity(Intent(applicationContext, activity.javaClass))
            this@SplashScreenActivity.finish()
        }
    }

}

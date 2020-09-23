@file:Suppress("DEPRECATION")

package lost.phone.finder.app.online.finder.activities

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
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.activity_splash_screen.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI

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
        loadInterstial()
        fetchBaseUrl()

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
        }, 3000)
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
                startActivity(Intent(applicationContext, activity.javaClass))
                this@SplashScreenActivity.finish()

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

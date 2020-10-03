package device.spotter.finder.appss.activities

import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_play_ring_silent.*
import kotlinx.android.synthetic.main.banner.*
import device.spotter.finder.appss.R

class PlayRingSilentActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_ring_silent)
        setTitle(R.string.ring_silent_phone)
        ringSilentPhone()
        adView(adView)
        stop.setOnClickListener {
            try {
                if (ringtone?.isPlaying!!) {
                    Log.d("Test", "stopped")

                    ringtone!!.stop()
                    finish()
                }
                finish()
            } catch (e: Exception) {
                finish()
                e.printStackTrace()
            }
        }
    }


    override fun onBackPressed() {
        try {
            if (ringtone?.isPlaying!!) {
                Log.d("Test", "stopped")

                ringtone!!.stop()
                finish()
            }
            finish()
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        try {
            if (ringtone?.isPlaying!!) {
                Log.d("Test", "stopped")

                ringtone!!.stop()
                finish()
            }
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
        super.onDestroy()
    }
}

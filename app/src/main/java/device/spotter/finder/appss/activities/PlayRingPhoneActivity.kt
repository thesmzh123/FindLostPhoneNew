package device.spotter.finder.appss.activities

import android.media.RingtoneManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import kotlinx.android.synthetic.main.activity_play_ring_silent.*
import kotlinx.android.synthetic.main.banner.*
import device.spotter.finder.appss.R


class PlayRingPhoneActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_ring_silent)
        try {
            setTitle(R.string.play_ringtone)
            ringtone = RingtoneManager.getRingtone(
                applicationContext,
                Settings.System.DEFAULT_RINGTONE_URI
            )
            //send acknowledgement back to requestor
            if (!ringtone?.isPlaying!!) {
                ringtone?.play()
                Log.d("Test", "ring played")
            }
            Log.d("Test", "OK")
            adView(adView)
            stop.setOnClickListener {
                try {
                    if (ringtone?.isPlaying!!) {
                        Log.d("Test", "stopped")

                        ringtone!!.stop()
                        finish()
                    }
                    Log.d("Test", "OK")
                    finish()
                } catch (e: Exception) {
                    finish()
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }


    override fun onBackPressed() {
        try {
            if (ringtone?.isPlaying!!) {
                Log.d("Test", "stopped")

                ringtone!!.stop()
                finish()
            }
            Log.d("Test", "OK")
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
            Log.d("Test", "OK")
        } catch (e: Exception) {
            finish()
            e.printStackTrace()
        }
        super.onDestroy()
    }
}

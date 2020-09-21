package lost.phone.finder.app.online.finder.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_compass.view.*
import kotlinx.android.synthetic.main.main_header_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import java.util.*
import kotlin.math.roundToInt

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class CompassFragment : BaseFragment(), SensorEventListener {
    private var mAzimuth: Int = 0
    private var mSensorManager: SensorManager? = null
    private var mRotationV: Sensor? = null
    private var mAccelerometer: Sensor? = null
    private var mMagnetometer: Sensor? = null
    private var haveSensor: Boolean = false
    private var haveSensor2: Boolean = false
    private val rMat = FloatArray(9)
    private val orientation = FloatArray(3)
    private val mLastAccelerometer = FloatArray(3)
    private val mLastMagnetometer = FloatArray(3)
    private var mLastMagnetometerSet: Boolean = false
    private var mLastAccelerometerSet: Boolean = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_compass, container, false)
        root!!.titleText.text = getString(R.string.compass)
        Glide.with(requireActivity()).load(R.drawable.compass_icon).into(root!!.imageView)
        mSensorManager =
            requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (gpsTracker!!.canGetLocation()) {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(
                    gpsTracker!!.getLatitude(),
                    gpsTracker!!.getLongitude(),
                    1
                )
                val obj = addresses[0]
                root!!.countryName.text = obj.locality + ", " + obj.countryName
            } else {
                root!!.countryName.text =
                    "Lat: " + gpsTracker!!.getLatitude() + ", Long: " + gpsTracker!!.getLongitude()
            }
        } else {
            root!!.country.visibility = View.GONE
        }
        return root
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAGI, "onAccuracyChanged")
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        try {
            if (event != null) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rMat, event.values)
                    mAzimuth = (Math.toDegrees(
                        SensorManager.getOrientation(
                            rMat,
                            orientation
                        )[0].toDouble()
                    ) + 360).toInt() % 360
                }
            }

            if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.size)
                mLastAccelerometerSet = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.size)
                mLastMagnetometerSet = true
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer)
                SensorManager.getOrientation(rMat, orientation)
                mAzimuth = (Math.toDegrees(
                    SensorManager.getOrientation(
                        rMat,
                        orientation
                    )[0].toDouble()
                ) + 360).toInt() % 360
            }

            mAzimuth = mAzimuth.toDouble().roundToInt()
            root!!.imageViewCompass.rotation = (-mAzimuth).toFloat()

            var where = "NW"

            if (mAzimuth >= 350 || mAzimuth <= 10)
                where = "N"
            if (mAzimuth in 281..349)
                where = "NW"
            if (mAzimuth in 261..280)
                where = "W"
            if (mAzimuth in 191..260)
                where = "SW"
            if (mAzimuth in 171..190)
                where = "S"
            if (mAzimuth in 101..170)
                where = "SE"
            if (mAzimuth in 81..100)
                where = "E"
            if (mAzimuth in 11..80)
                where = "NE"

            val spannable = SpannableString("$mAzimuth° $where")
            spannable.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0, 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            root!!.angle.text = spannable

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCompass() {
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager!!.getDefaultSensor(
                    Sensor.TYPE_MAGNETIC_FIELD
                ) == null)
            ) {
                noSensorsAlert()
            } else {
                mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                mMagnetometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                haveSensor = mSensorManager!!.registerListener(
                    this,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI
                )
                haveSensor2 = mSensorManager!!.registerListener(
                    this,
                    mMagnetometer,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        } else {
            mRotationV = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            haveSensor =
                mSensorManager!!.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopCompass() {
        if (haveSensor) {
            mSensorManager!!.unregisterListener(this, mRotationV)
        } else {
            mSensorManager!!.unregisterListener(this, mAccelerometer)
            mSensorManager!!.unregisterListener(this, mMagnetometer)
        }
    }

    override fun onPause() {
        super.onPause()
        stopCompass()
    }


    override fun onResume() {
        super.onResume()
        startCompass()
    }

    private fun noSensorsAlert() {
        val alertDialog =
            MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialogTheme)
        alertDialog.setMessage(getString(R.string.no_compass_sensor))
            .setCancelable(false)
            .setNegativeButton(
                getString(R.string.ok)
            ) { dialog, id -> findNavController().navigateUp() }
        alertDialog.show()
    }
}
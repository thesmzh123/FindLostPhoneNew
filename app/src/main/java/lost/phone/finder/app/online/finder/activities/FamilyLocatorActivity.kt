@file:Suppress("DEPRECATION")

package lost.phone.finder.app.online.finder.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_family_locator.*
import kotlinx.android.synthetic.main.enter_phone_number_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.adapters.FamilyLocatorAdapter
import lost.phone.finder.app.online.finder.models.FamilyLocator
import lost.phone.finder.app.online.finder.utils.Constants.FAMILY_REQUEST_URL
import lost.phone.finder.app.online.finder.utils.Constants.PICK_CONTACT
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.RegisterAPI
import org.json.JSONArray
import org.json.JSONObject
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import java.io.BufferedReader
import java.io.InputStreamReader

class FamilyLocatorActivity : BaseActivity(), OnMapReadyCallback, LocationListener,
    OnMarkerClickListener {
    private var deleteDialogView: View? = null
    private var familyLocatorList: ArrayList<FamilyLocator>? = null
    private var mMap: GoogleMap? = null
    private var lati: Double? = null
    private var longi: Double? = null
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_locator)
        if (!InternetConnection().checkConnection(this)) {
            showToast(getString(R.string.no_internet))
            finish()
        }

        familyLocatorList = ArrayList()
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = null
        }
        addMember.visibility = View.VISIBLE
        addMember.setOnClickListener {
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    showNumberDialog()

                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        showNumberDialog()
                    }
                }
            } else {
                showNumberDialog()


            }
        }
        addMember1.setOnClickListener {
            if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    showNumberDialog()

                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        showNumberDialog()
                    }
                }
            } else {
                showNumberDialog()


            }
        }
        loadInterstial()
        loadFamilyList()

    }

    fun initMap(lati: Double, lonngi: Double, name: String) {
        mapLayout.visibility = View.VISIBLE
        val mapFragment = (this.supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        this.lati = lati
        longi = lonngi
        this.name = name
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ActivityCompat.checkSelfPermission(
                this@FamilyLocatorActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this@FamilyLocatorActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.isMyLocationEnabled = false
        mMap!!.clear()

        try {
            if (InternetConnection().checkConnection(this@FamilyLocatorActivity)) {
                loadMap(lati!!, longi!!, name!!)
            } else {
                showToast(getString(R.string.no_internet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadMap(lati: Double, lonngi: Double, name: String) {
        mMap!!.addMarker(
            MarkerOptions()
                .position(LatLng(lati, lonngi))
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        )
        mMap!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    lati,
                    lonngi
                ), 15f
            )
        )
        // Zoom in, animating the camera.
        mMap!!.animateCamera(CameraUpdateFactory.zoomIn())
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null)


    }

    override fun onLocationChanged(p0: Location) {
        Log.d(TAGI, "onLocationChanged: ")
    }


    override fun onStatusChanged(s: String?, i: Int, bundle: Bundle?) {}


    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }

    @SuppressLint("InflateParams")
    private fun showNumberDialog() {
        val factory = LayoutInflater.from(this@FamilyLocatorActivity)
        deleteDialogView =
            factory.inflate(R.layout.enter_phone_number_layout, null)
        val deleteDialog: AlertDialog =
            MaterialAlertDialogBuilder(this@FamilyLocatorActivity).create()
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setView(deleteDialogView)
        deleteDialog.setCancelable(false)
        deleteDialogView!!.send.setOnClickListener {
            if (deleteDialogView!!.editText_carrierNumber1.text!!.isEmpty()) {
                showToast(getString(R.string.fill_the_field))
            } else {
                if (InternetConnection().checkConnection(this@FamilyLocatorActivity)) {
                    var num = deleteDialogView!!.editText_carrierNumber1.text!!.toString()
                    num = num.replace(" ", "")
                    if (num.contains("+")) {
                        num = num.replace("+", "")
                    }
                    showDialog(getString(R.string.sending_family_memeber_request))
                    sendFamilyMemberRequest(num)
                    deleteDialog.dismiss()
                } else {
                    showToast(getString(R.string.no_internet))
                }


            }
        }
        deleteDialogView!!.cancel.setOnClickListener {
            deleteDialog.dismiss()
        }
        deleteDialogView!!.fetchContacts.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT)
        }
        deleteDialog.show()
        deleteDialog.window!!.decorView.setBackgroundResource(android.R.color.transparent)
    }

    private fun sendFamilyMemberRequest(num: String) {
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, mainUrl + FAMILY_REQUEST_URL,
            Response.Listener<String?> { response -> // response
                Log.d(TAGI, "onResponse: $response")
                hideDialog()
                val jsonObject = JSONObject(response!!)
                if (jsonObject.getBoolean("error")) {
                    showToast(jsonObject.getString("message"))
                } else {
                    showToast(jsonObject.getString("message"))

                }
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "onErrorResponse: " + error!!.message)
                hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["name"] = auth.currentUser!!.displayName.toString()
                params["phonenum"] = num
                params["phonenumSend"] =
                    SharedPrefUtils.getStringData(this@FamilyLocatorActivity, "phoneNum").toString()
                return params
            }
        }
        queue!!.add(postRequest)
    }


    override fun onBackPressed() {
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }



    private fun loadFamilyList() {
        showDialog(getString(R.string.fetch_family_list))
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.fetchFamilyList(
            SharedPrefUtils.getStringData(this@FamilyLocatorActivity, "phoneNum").toString()
            ,
            object : Callback<retrofit.client.Response> {
                override fun success(
                    result: retrofit.client.Response,
                    response: retrofit.client.Response
                ) {
                    //On success we will read the server's output using bufferedreader
                    //Creating a bufferedreader object
                    val reader: BufferedReader?

                    //An string to store output from the server
                    val output: String

                    try {
                        //Initializing buffered reader
                        reader = BufferedReader(InputStreamReader(result.body.`in`()))

                        //Reading the output in the string
                        output = reader.readLine()
                        Log.d(TAGI, "device: $output")

                        familyLocatorList!!.clear()
                        val jsonArray =
                            JSONArray(output)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject1 = jsonArray.getJSONObject(i)
                            familyLocatorList!!.add(
                                FamilyLocator(
                                    jsonObject1.getInt("friend_id"),
                                    jsonObject1.getString("phone_num")
                                    ,
                                    jsonObject1.getString("name"),
                                    jsonObject1.getInt("id")
                                    ,
                                    jsonObject1.getInt("uid"),
                                    jsonObject1.getString("devicename"),
                                    jsonObject1.getString("model"),
                                    jsonObject1.getString("token"),
                                    jsonObject1.getDouble("latitude"),
                                    jsonObject1.getDouble("longitude"),
                                    jsonObject1.getString("mac_address"),
                                    jsonObject1.getString("updateDate"),
                                    jsonObject1.getInt("pid")
                                )
                            )
                        }


                        val adapter =
                            FamilyLocatorAdapter(this@FamilyLocatorActivity, familyLocatorList!!)
                        recyclerView.adapter = adapter
                        checkEmptyState()
                        hideDialog()

                    } catch (e: Exception) {
                        Log.d(TAGI, "error: " + e.message)
                        e.printStackTrace()
                        hideDialog()
                        checkEmptyState()


                    }

                }

                override fun failure(error: RetrofitError) {
                    Log.d(TAGI, error.toString())
                    hideDialog()
                }
            }
        )
    }

    private fun checkEmptyState() {
        if (familyLocatorList!!.isEmpty()) {
            recyclerViewLayout.visibility = View.GONE
            addMember.visibility = View.VISIBLE

        } else {
            recyclerViewLayout.visibility = View.VISIBLE
            addMember.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.family_locator_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.friends_requests -> {
                if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                    if (interstitial.isLoaded) {
                        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            interstitial.show()
                        } else {
                            Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                        }
                    } else {
                        openActivity(true, FriendRequestActivity())

                    }
                    interstitial.adListener = object : AdListener() {
                        override fun onAdClosed() {
                            requestNewInterstitial()
                            openActivity(true, FriendRequestActivity())
                        }
                    }
                } else {
                    openActivity(true, FriendRequestActivity())



                }
                return true
            }
            R.id.requests_status -> {
                if (!SharedPrefUtils.getBooleanData(this, "hideAds")) {
                if (interstitial.isLoaded) {
                    if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        interstitial.show()
                    } else {
                        Log.d(TAGI, "App Is In Background Ad Is Not Going To Show")

                    }
                } else {
                    openActivity(false, FriendRequestActivity())

                }
                interstitial.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        requestNewInterstitial()
                        openActivity(false, FriendRequestActivity())
                    }
                }
            } else {
                openActivity(false, FriendRequestActivity())


            }


                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openActivity(b: Boolean, activity: Activity) {
        val intent = Intent(applicationContext, activity.javaClass)
        intent.putExtra("isFriendRequest", b)
        startActivity(intent)

    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_CONTACT -> if (resultCode == Activity.RESULT_OK) {
                val contactData: Uri? = data!!.data
                val c: Cursor = managedQuery(contactData, null, null, null, null)
                if (c.moveToFirst()) {
                    val id: String =
                        c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone: String =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhone.equals("1", ignoreCase = true)) {
                        val phones: Cursor? = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null
                        )

                        if (phones != null) {
                            phones.moveToFirst()

                            Log.d(
                                TAGI,
                                "onActivityResult: " + phones.getString(phones.getColumnIndex("data1"))
                            )
                            deleteDialogView!!.editText_carrierNumber1.setText(
                                phones.getString(phones.getColumnIndex("data1"))
                            )
                        }
                    }
                    Log.d(
                        TAGI,
                        "onActivityResult: " + c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    )
                }
            }
        }
    }
}
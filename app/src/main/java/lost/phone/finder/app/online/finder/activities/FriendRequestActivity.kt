package lost.phone.finder.app.online.finder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.find.lost.app.phone.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.activity_friend_request.*
import kotlinx.android.synthetic.main.banner.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.adapters.FamilyRequestAdapter
import lost.phone.finder.app.online.finder.models.Family
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
import lost.phone.finder.app.online.finder.utils.RegisterAPI
import org.json.JSONArray
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class FriendRequestActivity : BaseActivity() {
    var familyRequestList: ArrayList<Family>? = null
    var isFriendRequest: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)
        if (!InternetConnection().checkConnection(this)) {
            showToast(getString(R.string.no_internet))
            finish()
        }
        familyRequestList = ArrayList()
        if (intent.getBooleanExtra("b", false)) {
            isFriendRequest = intent.getBooleanExtra("b", false)
        }
        if (intent.getBooleanExtra("isFriendRequest", false)) {
            isFriendRequest = intent.getBooleanExtra("isFriendRequest", false)
        }
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            if (isFriendRequest) {
                supportActionBar!!.title = getString(R.string.friends_requests)
            } else {
                supportActionBar!!.title = getString(R.string.requests_status)

            }
            toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        }
        init(isFriendRequest)
        adView(adView)
    }

    fun init(friendRequest: Boolean) {
        if (friendRequest) {
            showDialog(getString(R.string.fetch_request_list))
            loadFriendRequestData()
        } else {
            showDialog(getString(R.string.fetch_pending_list))
            loadPendingRequest()
        }
    }

    private fun loadPendingRequest() {
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.fetchFamilyRequestPending(
            SharedPrefUtils.getStringData(this@FriendRequestActivity, "phoneNum").toString()
            ,
            object : Callback<Response> {
                override fun success(result: Response, response: Response) {
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
                        familyRequestList!!.clear()
                        val jsonArray =
                            JSONArray(output)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject1 = jsonArray.getJSONObject(i)
                            familyRequestList!!.add(
                                Family(
                                    jsonObject1.getString("name"),
                                    jsonObject1.getInt("pid")
                                    ,
                                    jsonObject1.getInt("uid"),
                                    jsonObject1.getString("phone_num")
                                    ,
                                    jsonObject1.getString("token"), isFriendRequest
                                )
                            )
                        }


                        hideDialog()
                        val adapter =
                            FamilyRequestAdapter(
                                this@FriendRequestActivity,
                                familyRequestList!!,
                                this@FriendRequestActivity
                            )
                        recyclerView.adapter = adapter
                        checkEmptyState()
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

    private fun loadFriendRequestData() {
        val restAdapter: RestAdapter = RestAdapter.Builder().setEndpoint(mainUrl).build()
        val api: RegisterAPI = restAdapter.create(RegisterAPI::class.java)
        api.fetchFamilyRequest(
            SharedPrefUtils.getStringData(this@FriendRequestActivity, "phoneNum").toString()
            ,
            object : Callback<Response> {
                override fun success(result: Response, response: Response) {
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
                        familyRequestList!!.clear()
                        val jsonArray =
                            JSONArray(output)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject1 = jsonArray.getJSONObject(i)
                            familyRequestList!!.add(
                                Family(
                                    jsonObject1.getString("name"),
                                    jsonObject1.getInt("pid")
                                    ,
                                    jsonObject1.getInt("uid"),
                                    jsonObject1.getString("phone_num")
                                    ,
                                    jsonObject1.getString("token"), isFriendRequest
                                )
                            )
                        }


                        hideDialog()
                        val adapter =
                            FamilyRequestAdapter(
                                this@FriendRequestActivity,
                                familyRequestList!!,
                                this@FriendRequestActivity
                            )
                        recyclerView.adapter = adapter
                        checkEmptyState()
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

    override fun onBackPressed() {
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun checkEmptyState() {
        if (familyRequestList!!.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyViewImage.visibility = View.VISIBLE
            if (isFriendRequest) {
                emptyView.text = getString(R.string.no_family_request_s_found)
                Glide.with(this@FriendRequestActivity).load(R.drawable.friend_request)
                    .into(emptyViewImage)
            } else {
                emptyView.text = getString(R.string.no_family_pending_found)
                Glide.with(this@FriendRequestActivity).load(R.drawable.no_pending_request)
                    .into(emptyViewImage)
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            emptyViewImage.visibility = View.GONE
        }
    }
}
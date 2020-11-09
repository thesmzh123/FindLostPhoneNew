package device.spotter.finder.appss.adapters

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import device.spotter.finder.appss.utils.InternetConnection
import com.find.lost.app.phone.utils.SharedPrefUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.family_request_list_layout.view.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.activities.BaseActivity
import device.spotter.finder.appss.activities.FriendRequestActivity
import device.spotter.finder.appss.models.Family
import device.spotter.finder.appss.utils.Constants.ACCEPT_REQUEST_URL
import device.spotter.finder.appss.utils.Constants.CANCEL_FAMILY_REQUEST_URL
import device.spotter.finder.appss.utils.Constants.CANCEL_PENDING_FAMILY_REQUEST_URL
import device.spotter.finder.appss.utils.Constants.TAGI
import kotlinx.android.synthetic.main.family_request_list_layout.view.cancel
import org.json.JSONObject

class FamilyRequestAdapter(
    val context: Context,
    private val familyRequestList: ArrayList<Family>,
    val friendRequestActivity: FriendRequestActivity
) :
    RecyclerView.Adapter<FamilyRequestAdapter.MyHolderView>() {

    class MyHolderView(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolderView {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.family_request_list_layout, parent, false)
        return MyHolderView(v)
    }

    override fun getItemCount(): Int {
        return familyRequestList.size
    }

    override fun onBindViewHolder(holder: MyHolderView, position: Int) {
        val family = familyRequestList[position]
        holder.itemView.name.text = family.name
        holder.itemView.phoneNum.text = family.phoneNumber
        if (family.isFriendRequest) {
            holder.itemView.accept.text = context.getString(R.string.accept)
        } else {
            holder.itemView.accept.text = context.getString(R.string.pending)
        }
        holder.itemView.accept.setOnClickListener {
            if (family.isFriendRequest) {
                if (InternetConnection().checkConnection(context)) {
                    disclaimerDIalog(family, position)
                } else {
                    context.getString(R.string.no_internet)
                }

            } else {
                Log.d(TAGI, "onBindViewHolder: pending")
            }
        }
        holder.itemView.cancel.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            if (family.isFriendRequest) {
                                if (InternetConnection().checkConnection(context)) {
                                    cancelRequest(family, position)
                                } else {
                                    context.getString(R.string.no_internet)
                                }

                            } else {
                                Log.d(TAGI, "onBindViewHolder: pending")
                                if (InternetConnection().checkConnection(context)) {
                                    cancelRequestPending(family, position)
                                } else {
                                    context.getString(R.string.no_internet)
                                }
                            }
                            dialog.dismiss()
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }

            val builder = MaterialAlertDialogBuilder(
                context,
                R.style.MaterialAlertDialogTheme
            )
            builder.setTitle("Cancel Request")
                .setMessage("Do you want to cancel this request?")
                .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getString(R.string.no), dialogClickListener).show()

        }
    }

    private fun disclaimerDIalog(
        family: Family,
        position: Int
    ) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        acceptRequest(family, position)


                        dialog.dismiss()
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }

        val builder = MaterialAlertDialogBuilder(
            context,
            R.style.MaterialAlertDialogTheme
        )
        builder.setTitle("Disclaimer")
            .setMessage("Accepting this request will mean that you will allow your family member to check your live location on map")
            .setPositiveButton(context.getString(R.string.accept), dialogClickListener)
            .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show()
    }

    private fun acceptRequest(family: Family, position: Int) {
        (context as BaseActivity).showDialog(context.getString(R.string.accept_request))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, context.mainUrl + ACCEPT_REQUEST_URL,
            Response.Listener<String?> { response -> // response
                Log.d(TAGI, "onResponse: $response")
                context.hideDialog()
                val jsonObject = JSONObject(response!!)
                if (jsonObject.getBoolean("error")) {
                    context.showToast(jsonObject.getString("message"))
                } else {
                    context.showToast(jsonObject.getString("message"))
                    familyRequestList.removeAt(position)
                    notifyItemChanged(position)
                    notifyItemRangeRemoved(0, familyRequestList.size)
                    friendRequestActivity.init(family.isFriendRequest)
                }
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "onErrorResponse: " + error!!.message)
                context.hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["sender_id"] = family.pid.toString()
                params["senderToken"] = family.token
                params["senderName"] =  SharedPrefUtils.getStringData(context, "username").toString()
                params["phonenum"] =
                    SharedPrefUtils.getStringData(context, "phoneNum").toString()
                return params
            }
        }
        context.queue!!.add(postRequest)
    }

    private fun cancelRequest(
        family: Family,
        position: Int
    ) {
        (context as BaseActivity).showDialog(context.getString(R.string.cancel_request))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, context.mainUrl + CANCEL_FAMILY_REQUEST_URL,
            Response.Listener<String?> { response -> // response
                Log.d(TAGI, "onResponse: $response")
                context.hideDialog()
                val jsonObject = JSONObject(response!!)
                if (jsonObject.getBoolean("error")) {
                    context.showToast(jsonObject.getString("message"))
                } else {
                    context.showToast(jsonObject.getString("message"))
                    familyRequestList.removeAt(position)
                    notifyItemChanged(position)
                    notifyItemRangeRemoved(0, familyRequestList.size)
                    friendRequestActivity.init(family.isFriendRequest)
                }
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "onErrorResponse: " + error!!.message)
                context.hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["sender_id"] = family.pid.toString()
                params["senderToken"] = family.token
                params["senderName"] = SharedPrefUtils.getStringData(context, "username").toString()
                params["phonenum"] =
                    SharedPrefUtils.getStringData(context, "phoneNum").toString()
                return params
            }
        }
        context.queue!!.add(postRequest)
    }

    private fun cancelRequestPending(
        family: Family,
        position: Int
    ) {
        (context as BaseActivity).showDialog(context.getString(R.string.cancel_request))
        val postRequest: StringRequest = object : StringRequest(
            Method.POST, context.mainUrl + CANCEL_PENDING_FAMILY_REQUEST_URL,
            Response.Listener<String?> { response -> // response
                Log.d(TAGI, "onResponse: $response")
                context.hideDialog()
                val jsonObject = JSONObject(response!!)
                if (jsonObject.getBoolean("error")) {
                    context.showToast(jsonObject.getString("message"))
                } else {
                    context.showToast(jsonObject.getString("message"))
                    familyRequestList.removeAt(position)
                    notifyItemChanged(position)
                    notifyItemRangeRemoved(0, familyRequestList.size)
                    friendRequestActivity.init(family.isFriendRequest)
                }
            },
            Response.ErrorListener { error -> // error
                Log.d(TAGI, "onErrorResponse: " + error!!.message)
                context.hideDialog()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["receiver_id"] = family.pid.toString()
                params["receiverToken"] = family.token
                params["receiverName"] = SharedPrefUtils.getStringData(context, "username").toString()
                params["phonenum"] =
                    SharedPrefUtils.getStringData(context, "phoneNum").toString()
                return params
            }
        }
        context.queue!!.add(postRequest)
    }
}
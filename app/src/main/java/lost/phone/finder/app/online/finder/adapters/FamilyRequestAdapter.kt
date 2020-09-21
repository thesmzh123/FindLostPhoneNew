package lost.phone.finder.app.online.finder.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.find.lost.app.phone.utils.SharedPrefUtils
import kotlinx.android.synthetic.main.family_request_list_layout.view.*
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.activities.BaseActivity
import lost.phone.finder.app.online.finder.activities.FriendRequestActivity
import lost.phone.finder.app.online.finder.models.Family
import lost.phone.finder.app.online.finder.utils.Constants.ACCEPT_REQUEST_URL
import lost.phone.finder.app.online.finder.utils.Constants.CANCEL_FAMILY_REQUEST_URL
import lost.phone.finder.app.online.finder.utils.Constants.CANCEL_PENDING_FAMILY_REQUEST_URL
import lost.phone.finder.app.online.finder.utils.Constants.TAGI
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
                acceptRequest(family, position)

            } else {
                Log.d(TAGI, "onBindViewHolder: pending")
            }
        }
        holder.itemView.cancel.setOnClickListener {
            if (family.isFriendRequest) {
                cancelRequest(family, position)

            } else {
                Log.d(TAGI, "onBindViewHolder: pending")
                cancelRequestPending(family, position)
            }
        }
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
                params["senderName"] = context.auth.currentUser!!.displayName.toString()
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
                params["senderName"] = context.auth.currentUser!!.displayName.toString()
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
                params["receiverName"] = context.auth.currentUser!!.displayName.toString()
                params["phonenum"] =
                    SharedPrefUtils.getStringData(context, "phoneNum").toString()
                return params
            }
        }
        context.queue!!.add(postRequest)
    }
}
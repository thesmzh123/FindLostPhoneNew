package device.spotter.finder.appss.utils

import retrofit.Callback
import retrofit.client.Response
import retrofit.http.Field
import retrofit.http.FormUrlEncoded
import retrofit.http.POST

interface RegisterAPI {
    @FormUrlEncoded
    @POST("/user_reg.php")
    fun insertUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("mac_address") macAddress: String,
        callback: Callback<Response>
    )

    @FormUrlEncoded
    @POST("/device_reg.php")
    fun insertDeviceReg(
        @Field("devicename") devicename: String,
        @Field("uid") uid: String,
        @Field("model") model: String,
        @Field("token") token: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("mac_address") mac_address: String,
        @Field("updateDate") updateDate: String,
        @Field("pid") pid: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/replace_device.php")
    fun replaceDevice(
        @Field("devicename") devicename: String,
        @Field("uid") uid: String,
        @Field("model") model: String,
        @Field("token") token: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("mac_address") mac_address: String,
        @Field("updateDate") updateDate: String,
        callback: Callback<Response>
    )

    @FormUrlEncoded
    @POST("/fetch_all_device.php")
    fun fetchAllDevices(
        @Field("uid") uid: String,
        callback: Callback<Response>
    )



    @FormUrlEncoded
    @POST("/update_location.php")
    fun updateLocation(
        @Field("uid") uid: String,
        @Field("mac_address") mac_address: String,
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("updateDate") updateDate: String,
        callback: Callback<Response>
    )

    @FormUrlEncoded
    @POST("/update_phone_num.php")
    fun updatePhone(
        @Field("uid") uid: String,
        @Field("phonenum") phonenum: String,
        @Field("token") token: String,
        @Field("mac_address") macAddress: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/update_num.php")
    fun updatePhoneNum(
        @Field("uid") uid: String,
        @Field("phonenum") phonenum: String,
        @Field("token") token: String,
        @Field("mac_address") macAddress: String,
        callback: Callback<Response>
    )

    @FormUrlEncoded
    @POST("/fetch_family_request_list.php")
    fun fetchFamilyRequest(
        @Field("phonenum") phonenum: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/fetch_pending_request.php")
    fun fetchFamilyRequestPending(
        @Field("phonenum") phonenum: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/fetch_family_list.php")
    fun fetchFamilyList(
        @Field("phonenum") phonenum: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/send_notification.php")
    fun sendNotification(
        @Field("uid") uid: String,
        @Field("mac_address") mac_address: String,
        @Field("title") title: String,
        @Field("body") body: String,
        callback: Callback<Response>
    )
    @FormUrlEncoded
    @POST("/send_last_hope.php")
    fun sendLastHope(
        @Field("uid") uid: String,
        @Field("mac_address") mac_address: String,
        @Field("title") title: String,
        @Field("body") body: String,
        callback: Callback<Response>
    )
}
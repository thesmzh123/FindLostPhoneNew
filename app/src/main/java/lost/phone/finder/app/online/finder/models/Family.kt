package lost.phone.finder.app.online.finder.models

data class Family(
    val name: String, val pid: Int, val uid: Int, val phoneNumber: String,
    val token: String,val isFriendRequest:Boolean
)
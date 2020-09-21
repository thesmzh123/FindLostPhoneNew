package lost.phone.finder.app.online.finder.models

data class FamilyLocator(
    val friendId: Int, val phoneNum: String,
    val name: String, val id: Int, val uid: Int, val deviceName: String,
    val model: String, val token: String, val latitude: Double, val longitude: Double,
    val macAddress: String, val updateDate: String, val pid: Int
)
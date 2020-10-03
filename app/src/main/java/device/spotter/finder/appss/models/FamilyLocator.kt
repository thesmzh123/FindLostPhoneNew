package device.spotter.finder.appss.models

data class FamilyLocator(
    val friendId: Int, val phoneNum: String,
    val name: String, val id: Int, val uid: Int, val deviceName: String,
    val model: String, val token: String, val latitude: String, val longitude: String,
    val macAddress: String, val updateDate: String, val pid: Int
)
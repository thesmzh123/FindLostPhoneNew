package lost.phone.finder.app.online.finder.models

data class Devices(
    val deviceName: String, val model: String,
    val token: String, val lat: String,
    val longi: String, val macAddress: String,
    val updateDate: String,
    var isChecked: Boolean
)
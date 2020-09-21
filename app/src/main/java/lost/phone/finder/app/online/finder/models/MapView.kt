package lost.phone.finder.app.online.finder.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class MapView(val lati: String, val longi: String,val deviceName:String) : Serializable
package lost.phone.finder.app.online.finder.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.find.lost.app.phone.utils.InternetConnection
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import lost.phone.finder.app.online.finder.R
import lost.phone.finder.app.online.finder.models.MapView
import java.util.*

@Suppress("UNCHECKED_CAST")
class MapViewFragment : BaseFragment(), OnMapReadyCallback, LocationListener,
    OnMarkerClickListener {
    private var mMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_map_view, container, false)
        arrayList = requireArguments().getSerializable("mapView") as ArrayList<MapView>
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = (this.childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        return root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.isMyLocationEnabled = false

        try {
            if (InternetConnection().checkConnection(requireActivity())) {
                loadMap(arrayList!!)
            } else {
                showToast(getString(R.string.no_internet))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun loadMap(arrayList: ArrayList<MapView>) {
        for (i in 0 until arrayList.size) {
            mMap!!.addMarker(
                MarkerOptions()
                    .position(LatLng(arrayList[i].lati.toDouble(), arrayList[i].longi.toDouble()))
                    .title(arrayList[i].deviceName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            )
            mMap!!.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        arrayList[i].lati.toDouble(),
                        arrayList[i].longi.toDouble()
                    ), 15f
                )
            )
            // Zoom in, animating the camera.
            mMap!!.animateCamera(CameraUpdateFactory.zoomIn())
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null)

        }
    }


    override fun onStatusChanged(s: String?, i: Int, bundle: Bundle?) {}


    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }
}
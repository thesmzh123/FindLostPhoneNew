package device.spotter.finder.appss.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.custom_curve_profile_layout.view.*
import kotlinx.android.synthetic.main.fragment_nearby_places.view.*
import device.spotter.finder.appss.R
import device.spotter.finder.appss.adapters.NearbyAdapter
import device.spotter.finder.appss.models.Nearby
import device.spotter.finder.appss.utils.Constants.TAGI
import device.spotter.finder.appss.utils.RecyclerClickListener
import device.spotter.finder.appss.utils.RecyclerTouchListener
import device.spotter.finder.appss.utils.SpacesItemDecoration

class NearbyPlacesFragment : BaseFragment() {
    private var nearbyList: ArrayList<Nearby>? = null
    private var spanCount = 3 // 3 columns
    private var spacing = 50 // 50px
    private var includeEdge = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_nearby_places, container, false)
        Glide.with(requireActivity()).load(R.drawable.nearbycell).into(root!!.imageView)
        nearbyList = ArrayList()
        val nearbyAdapter = NearbyAdapter(requireActivity(), nearbyList())
        root!!.recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val itemDecoration =
            SpacesItemDecoration(spanCount, spacing, includeEdge)
        root!!.recyclerView.addItemDecoration(itemDecoration)
        root!!.recyclerView.adapter = nearbyAdapter
        nearbyAdapter.notifyDataSetChanged()

        root!!.recyclerView.addOnItemTouchListener(
            RecyclerTouchListener(
                requireActivity(),
                root!!.recyclerView,
                object : RecyclerClickListener {
                    override fun onClick(view: View?, position: Int) {
                        when (position) {
                            0 -> {
                                getNearbyPlcae("airport")
                            }
                            1 -> {
                                getNearbyPlcae("atm")
                            }
                            2 -> {
                                getNearbyPlcae("bus stop")
                            }
                            3 -> {
                                getNearbyPlcae("hospital")
                            }
                            4 -> {
                                getNearbyPlcae("mosque")
                            }
                            5 -> {
                                getNearbyPlcae("school")
                            }
                            6 -> {
                                getNearbyPlcae("police")
                            }
                            7 -> {
                                getNearbyPlcae("park")
                            }
                            8 -> {
                                getNearbyPlcae("gym")
                            }
                            9 -> {
                                getNearbyPlcae("post office")
                            }
                            10 -> {
                                getNearbyPlcae("book shop")
                            }
                            11 -> {
                                getNearbyPlcae("bank")
                            }
                        }
                    }

                    override fun onLongClick(view: View?, position: Int) {
                        Log.d(TAGI, "onLongClick")


                    }
                })
        )
        return root
    }


    private fun nearbyList(): ArrayList<Nearby> {
        nearbyList?.add(Nearby(R.drawable.plane, getString(R.string.airport)))
        nearbyList?.add(Nearby(R.drawable.atm, getString(R.string.atm)))
        nearbyList?.add(Nearby(R.drawable.bus_stop, getString(R.string.bus_stop)))
        nearbyList?.add(Nearby(R.drawable.hospital, getString(R.string.hospital)))
        nearbyList?.add(Nearby(R.drawable.mosque, getString(R.string.mosque)))
        nearbyList?.add(Nearby(R.drawable.school, getString(R.string.school)))
        nearbyList?.add(Nearby(R.drawable.police, getString(R.string.police)))
        nearbyList?.add(Nearby(R.drawable.park, getString(R.string.park)))
        nearbyList?.add(Nearby(R.drawable.gym, getString(R.string.gym)))
        nearbyList?.add(Nearby(R.drawable.post_office, getString(R.string.post_office)))
        nearbyList?.add(Nearby(R.drawable.bookshop, getString(R.string.bookshop)))
        nearbyList?.add(Nearby(R.drawable.museum, getString(R.string.bank)))

        return nearbyList!!
    }
}
package lost.phone.finder.app.online.finder.utils

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import lost.phone.finder.app.online.finder.utils.Constants.TAGI

class RecyclerTouchListener(
    context: Context,
    val recyclerView: RecyclerView,
    val recyclerClickListener: RecyclerClickListener
) : OnItemTouchListener {
    private var gestureDetector: GestureDetector? = null

    init {
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    recyclerClickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        Log.d(TAGI, "onTouchEvent")
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val child = rv.findChildViewUnder(e.x, e.y)
        if (child != null && gestureDetector!!.onTouchEvent(e)) {
            recyclerClickListener.onClick(child, rv.getChildAdapterPosition(child))
        }
        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        Log.d(TAGI, "onRequestDisallowInterceptTouchEvent")

    }
}
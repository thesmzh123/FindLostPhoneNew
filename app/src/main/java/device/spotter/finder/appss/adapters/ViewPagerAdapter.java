package device.spotter.finder.appss.adapters;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private List<View> viewsList = new ArrayList<>();

    @Override
    public int getItemPosition(@NonNull Object object) {
        int index = viewsList.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = viewsList.get(position);
        container.addView(v);
        return v;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView(viewsList.get(position));
    }

    @Override
    public int getCount() {
        return viewsList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
    
    public void addView(View v) {
        int i = addView(v, viewsList.size());
        Log.d("Test", "addView: " + i);
    }


    public int addView(View v, int position) {
        viewsList.add(position, v);
        return position;
    }


  /*  public int removeView(ViewPager pager, View v) {
        return removeView(pager, viewsList.indexOf(v));
    }
*/

    public int removeView(ViewPager pager, int position) {

        pager.setAdapter(null);
        viewsList.remove(position);
        pager.setAdapter(this);

        return position;
    }


    public View getView(int position) {
        return viewsList.get(position);
    }

}

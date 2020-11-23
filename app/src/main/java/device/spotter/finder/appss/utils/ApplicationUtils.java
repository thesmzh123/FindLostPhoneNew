package device.spotter.finder.appss.utils;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.database.FirebaseDatabase;

public class ApplicationUtils extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

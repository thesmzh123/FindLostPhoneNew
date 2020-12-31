package device.spotter.finder.appss.utils;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.google.firebase.FirebaseApp;

public class ApplicationUtils extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

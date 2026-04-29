package com.skillsphere.app;

import android.app.Application;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class SkillSphereApp extends Application {
    private static final String TAG = "SkillSphereApp";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary with the correct credentials from your dashboard
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dctfwfmfy"); 
        
        try {
            MediaManager.init(this, config);
            Log.d(TAG, "Cloudinary Initialized successfully with cloud_name: dctfwfmfy");
        } catch (IllegalStateException e) {
            Log.w(TAG, "Cloudinary already initialized");
        }
    }
}

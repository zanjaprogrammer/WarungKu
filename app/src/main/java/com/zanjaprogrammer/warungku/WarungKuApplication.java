package com.zanjaprogrammer.warungku;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.zanjaprogrammer.warungku.ads.AdConfiguration;

/**
 * Application class for WarungKu app initialization
 */
public class WarungKuApplication extends Application {
    private static final String TAG = "WarungKuApplication";
    private boolean isAdMobInitialized = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize AdMob SDK
        initializeAdMob();
    }
    
    private void initializeAdMob() {
        try {
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    isAdMobInitialized = true;
                    Log.d(TAG, "AdMob SDK initialized successfully");
                    Log.d(TAG, "Initialization status: " + initializationStatus.toString());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e);
            isAdMobInitialized = false;
            // Continue app execution without ads
        }
    }
    
    public boolean isAdMobInitialized() {
        return isAdMobInitialized;
    }
}
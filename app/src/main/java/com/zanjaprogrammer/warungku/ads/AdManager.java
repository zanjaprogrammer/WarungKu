package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.util.Log;

import com.zanjaprogrammer.warungku.WarungKuApplication;

/**
 * Central coordinator for all ad-related operations
 */
public class AdManager {
    private static final String TAG = "AdManager";
    private static AdManager instance;
    
    private final Context context;
    private final AdConfiguration adConfiguration;
    private final BannerAdController bannerAdController;
    private final InterstitialAdController interstitialAdController;
    private final AdFrequencyManager adFrequencyManager;
    private final UserActivityTracker userActivityTracker;
    private final RevenueAnalytics revenueAnalytics;
    private final SystemMonitor systemMonitor;
    private final PrivacyManager privacyManager;
    private boolean isInitialized = false;
    
    private AdManager(Context context) {
        this.context = context.getApplicationContext();
        this.adConfiguration = new AdConfiguration();
        this.userActivityTracker = new UserActivityTracker(this.context);
        this.revenueAnalytics = new RevenueAnalytics(this.context);
        this.systemMonitor = new SystemMonitor(this.context);
        this.privacyManager = new PrivacyManager(this.context);
        this.adFrequencyManager = new AdFrequencyManager(this.context, userActivityTracker);
        this.bannerAdController = new BannerAdController(this.context, adConfiguration, adFrequencyManager, revenueAnalytics, privacyManager);
        this.interstitialAdController = new InterstitialAdController(this.context, adConfiguration, adFrequencyManager, userActivityTracker, revenueAnalytics, privacyManager);
    }
    
    public static synchronized AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void initialize() {
        if (isInitialized) {
            Log.d(TAG, "AdManager already initialized");
            return;
        }
        
        try {
            // Initialize privacy compliance first
            privacyManager.initialize(success -> {
                if (success) {
                    Log.d(TAG, "Privacy compliance initialized: " + privacyManager.getPrivacyStatus());
                    
                    // Check if AdMob SDK is initialized
                    if (context instanceof WarungKuApplication) {
                        WarungKuApplication app = (WarungKuApplication) context;
                        if (app.isAdMobInitialized()) {
                            Log.d(TAG, "AdManager initialized successfully");
                            isInitialized = true;
                        } else {
                            Log.w(TAG, "AdMob SDK not initialized, ads will be disabled");
                        }
                    } else {
                        Log.w(TAG, "Could not access WarungKuApplication, ads will be disabled");
                    }
                } else {
                    Log.w(TAG, "Privacy compliance initialization failed, ads will be disabled");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AdManager", e);
        }
    }
    
    public AdConfiguration getAdConfiguration() {
        return adConfiguration;
    }
    
    public BannerAdController getBannerAdController() {
        return bannerAdController;
    }
    
    public InterstitialAdController getInterstitialAdController() {
        return interstitialAdController;
    }
    
    public AdFrequencyManager getAdFrequencyManager() {
        return adFrequencyManager;
    }
    
    public UserActivityTracker getUserActivityTracker() {
        return userActivityTracker;
    }
    
    public RevenueAnalytics getRevenueAnalytics() {
        return revenueAnalytics;
    }
    
    public SystemMonitor getSystemMonitor() {
        return systemMonitor;
    }
    
    public PrivacyManager getPrivacyManager() {
        return privacyManager;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public void onActivityCreated(String activityName) {
        if (!isInitialized) return;
        
        Log.d(TAG, "Activity created: " + activityName);
        userActivityTracker.onActivityCreated(activityName);
        adFrequencyManager.onActivityCreated(activityName);
    }
    
    public void onActivityResumed(String activityName) {
        if (!isInitialized) return;
        
        Log.d(TAG, "Activity resumed: " + activityName);
        userActivityTracker.onActivityResumed(activityName);
        adFrequencyManager.onActivityResumed(activityName);
    }
    
    public void onActivityPaused(String activityName) {
        if (!isInitialized) return;
        
        Log.d(TAG, "Activity paused: " + activityName);
        userActivityTracker.onActivityPaused(activityName);
        adFrequencyManager.onActivityPaused(activityName);
    }
}
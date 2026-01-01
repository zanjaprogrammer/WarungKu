package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Tracks and controls ad display frequency
 */
public class AdFrequencyManager {
    private static final String TAG = "AdFrequencyManager";
    private static final String PREFS_NAME = "ad_frequency_prefs";
    
    // SharedPreferences keys
    private static final String KEY_SESSION_START_TIME = "session_start_time";
    private static final String KEY_INTERSTITIAL_ADS_SHOWN = "interstitial_ads_shown";
    private static final String KEY_LAST_INTERSTITIAL_TIME = "last_interstitial_time";
    private static final String KEY_DAILY_RESET_TIME = "daily_reset_time";
    private static final String KEY_IS_IN_COOLDOWN = "is_in_cooldown";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final AdConfiguration adConfiguration;
    private final UserActivityTracker userActivityTracker;
    
    // Current session data
    private long sessionStartTime;
    private int interstitialAdsShown;
    private long lastInterstitialTime;
    private long dailyResetTime;
    private boolean isInCooldown;
    
    public AdFrequencyManager(Context context, UserActivityTracker userActivityTracker) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.adConfiguration = new AdConfiguration();
        this.userActivityTracker = userActivityTracker;
        
        loadFrequencyData();
        checkDailyReset();
    }
    
    private void loadFrequencyData() {
        long currentTime = System.currentTimeMillis();
        
        sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, currentTime);
        interstitialAdsShown = prefs.getInt(KEY_INTERSTITIAL_ADS_SHOWN, 0);
        lastInterstitialTime = prefs.getLong(KEY_LAST_INTERSTITIAL_TIME, 0);
        dailyResetTime = prefs.getLong(KEY_DAILY_RESET_TIME, currentTime);
        isInCooldown = prefs.getBoolean(KEY_IS_IN_COOLDOWN, false);
        
        Log.d(TAG, "Loaded frequency data - Interstitial ads shown: " + interstitialAdsShown);
    }
    
    private void saveFrequencyData() {
        prefs.edit()
                .putLong(KEY_SESSION_START_TIME, sessionStartTime)
                .putInt(KEY_INTERSTITIAL_ADS_SHOWN, interstitialAdsShown)
                .putLong(KEY_LAST_INTERSTITIAL_TIME, lastInterstitialTime)
                .putLong(KEY_DAILY_RESET_TIME, dailyResetTime)
                .putBoolean(KEY_IS_IN_COOLDOWN, isInCooldown)
                .apply();
    }
    
    private void checkDailyReset() {
        long currentTime = System.currentTimeMillis();
        long timeSinceReset = currentTime - dailyResetTime;
        
        if (timeSinceReset >= TimeUnit.DAYS.toMillis(1)) {
            Log.d(TAG, "Performing daily reset of ad frequency counters");
            interstitialAdsShown = 0;
            isInCooldown = false;
            dailyResetTime = currentTime;
            saveFrequencyData();
        }
    }
    
    public boolean canShowInterstitialAd() {
        checkDailyReset();
        
        // Check if in cooldown
        if (isInCooldown) {
            long currentTime = System.currentTimeMillis();
            long cooldownDuration = TimeUnit.MINUTES.toMillis(adConfiguration.getInterstitialCooldownMinutes());
            
            if (currentTime - lastInterstitialTime < cooldownDuration) {
                Log.d(TAG, "Interstitial ad blocked - still in cooldown");
                return false;
            } else {
                // Cooldown expired
                isInCooldown = false;
                saveFrequencyData();
            }
        }
        
        // Check hourly limit
        if (interstitialAdsShown >= adConfiguration.getMaxInterstitialPerHour()) {
            Log.d(TAG, "Interstitial ad blocked - hourly limit reached");
            return false;
        }
        
        // Check if in critical operation
        if (userActivityTracker.isInCriticalOperation()) {
            Log.d(TAG, "Interstitial ad blocked - user in critical operation");
            return false;
        }
        
        return true;
    }
    
    public void recordInterstitialImpression() {
        long currentTime = System.currentTimeMillis();
        interstitialAdsShown++;
        lastInterstitialTime = currentTime;
        
        // Check if we've reached the limit
        if (interstitialAdsShown >= adConfiguration.getMaxInterstitialPerHour()) {
            isInCooldown = true;
            Log.d(TAG, "Interstitial ad limit reached, entering cooldown");
        }
        
        saveFrequencyData();
        Log.d(TAG, "Recorded interstitial impression - Total shown: " + interstitialAdsShown);
    }
    
    public void recordInterstitialClick() {
        Log.d(TAG, "Recorded interstitial click");
        // Additional analytics can be added here
    }
    
    public void recordInterstitialCompletion() {
        Log.d(TAG, "Recorded interstitial completion");
        // Additional analytics can be added here
    }
    
    public void recordBannerImpression(String activityName) {
        Log.d(TAG, "Recorded banner impression for " + activityName);
        // Additional analytics can be added here
    }
    
    public void recordBannerClick(String activityName) {
        Log.d(TAG, "Recorded banner click for " + activityName);
        // Additional analytics can be added here
    }
    
    public void onActivityCreated(String activityName) {
        // Activity tracking is now handled by UserActivityTracker
        Log.d(TAG, "Activity created: " + activityName);
    }
    
    public void onActivityResumed(String activityName) {
        // Activity tracking is now handled by UserActivityTracker
        Log.d(TAG, "Activity resumed: " + activityName);
    }
    
    public void onActivityPaused(String activityName) {
        // Activity tracking is now handled by UserActivityTracker
        Log.d(TAG, "Activity paused: " + activityName);
    }
    
    public boolean isInCriticalOperation() {
        return userActivityTracker.isInCriticalOperation();
    }
    
    public long getIdleTimeHours() {
        return userActivityTracker.getIdleTimeHours();
    }
    
    public String getCurrentActivity() {
        return userActivityTracker.getCurrentActivity();
    }
    
    public int getInterstitialAdsShown() {
        return interstitialAdsShown;
    }
    
    public boolean isInCooldown() {
        return isInCooldown;
    }
}
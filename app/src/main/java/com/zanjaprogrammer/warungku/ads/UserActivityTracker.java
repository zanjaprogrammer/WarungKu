package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Monitors user behavior for intelligent ad timing
 */
public class UserActivityTracker {
    private static final String TAG = "UserActivityTracker";
    private static final String PREFS_NAME = "user_activity_prefs";
    
    // SharedPreferences keys
    private static final String KEY_CURRENT_ACTIVITY = "current_activity";
    private static final String KEY_LAST_ACTIVITY_TIME = "last_activity_time";
    private static final String KEY_SESSION_START_TIME = "session_start_time";
    private static final String KEY_IS_IN_CRITICAL_OPERATION = "is_in_critical_operation";
    private static final String KEY_NAVIGATION_COUNT = "navigation_count";
    private static final String KEY_LAST_NAVIGATION_TIME = "last_navigation_time";
    private static final String KEY_SELLING_SESSION_ACTIVE = "selling_session_active";
    private static final String KEY_SCANNING_ACTIVE = "scanning_active";
    
    private final Context context;
    private final SharedPreferences prefs;
    
    // Current state
    private String currentActivity;
    private long lastActivityTime;
    private long sessionStartTime;
    private boolean isInCriticalOperation;
    private int navigationCount;
    private long lastNavigationTime;
    private boolean sellingSessionActive;
    private boolean scanningActive;
    
    public UserActivityTracker(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        loadActivityData();
    }
    
    private void loadActivityData() {
        long currentTime = System.currentTimeMillis();
        
        currentActivity = prefs.getString(KEY_CURRENT_ACTIVITY, "");
        lastActivityTime = prefs.getLong(KEY_LAST_ACTIVITY_TIME, currentTime);
        sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, currentTime);
        isInCriticalOperation = prefs.getBoolean(KEY_IS_IN_CRITICAL_OPERATION, false);
        navigationCount = prefs.getInt(KEY_NAVIGATION_COUNT, 0);
        lastNavigationTime = prefs.getLong(KEY_LAST_NAVIGATION_TIME, 0);
        sellingSessionActive = prefs.getBoolean(KEY_SELLING_SESSION_ACTIVE, false);
        scanningActive = prefs.getBoolean(KEY_SCANNING_ACTIVE, false);
        
        Log.d(TAG, "Loaded activity data - Current activity: " + currentActivity);
    }
    
    private void saveActivityData() {
        prefs.edit()
                .putString(KEY_CURRENT_ACTIVITY, currentActivity)
                .putLong(KEY_LAST_ACTIVITY_TIME, lastActivityTime)
                .putLong(KEY_SESSION_START_TIME, sessionStartTime)
                .putBoolean(KEY_IS_IN_CRITICAL_OPERATION, isInCriticalOperation)
                .putInt(KEY_NAVIGATION_COUNT, navigationCount)
                .putLong(KEY_LAST_NAVIGATION_TIME, lastNavigationTime)
                .putBoolean(KEY_SELLING_SESSION_ACTIVE, sellingSessionActive)
                .putBoolean(KEY_SCANNING_ACTIVE, scanningActive)
                .apply();
    }
    
    /**
     * Track activity creation
     */
    public void onActivityCreated(String activityName) {
        Log.d(TAG, "Activity created: " + activityName);
        
        String previousActivity = currentActivity;
        currentActivity = activityName;
        lastActivityTime = System.currentTimeMillis();
        
        // Track navigation
        if (!activityName.equals(previousActivity) && !previousActivity.isEmpty()) {
            trackNavigation(previousActivity, activityName);
        }
        
        // Update critical operation status
        updateCriticalOperationStatus(activityName);
        
        saveActivityData();
    }
    
    /**
     * Track activity resume
     */
    public void onActivityResumed(String activityName) {
        Log.d(TAG, "Activity resumed: " + activityName);
        
        String previousActivity = currentActivity;
        currentActivity = activityName;
        lastActivityTime = System.currentTimeMillis();
        
        // Track navigation if different from previous
        if (!activityName.equals(previousActivity) && !previousActivity.isEmpty()) {
            trackNavigation(previousActivity, activityName);
        }
        
        // Update critical operation status
        updateCriticalOperationStatus(activityName);
        
        saveActivityData();
    }
    
    /**
     * Track activity pause
     */
    public void onActivityPaused(String activityName) {
        Log.d(TAG, "Activity paused: " + activityName);
        
        lastActivityTime = System.currentTimeMillis();
        
        // Clear critical operation if leaving selling activity
        if ("SellActivity".equals(activityName)) {
            sellingSessionActive = false;
            updateCriticalOperationStatus("");
        }
        
        saveActivityData();
    }
    
    /**
     * Track navigation between activities
     */
    private void trackNavigation(String fromActivity, String toActivity) {
        Log.d(TAG, "Navigation: " + fromActivity + " -> " + toActivity);
        
        navigationCount++;
        lastNavigationTime = System.currentTimeMillis();
        
        // Reset navigation count daily
        checkDailyReset();
        
        saveActivityData();
    }
    
    /**
     * Update critical operation status based on activity
     */
    private void updateCriticalOperationStatus(String activityName) {
        boolean wasCritical = isInCriticalOperation;
        
        // Critical operations: selling, scanning
        isInCriticalOperation = "SellActivity".equals(activityName) || 
                               sellingSessionActive || 
                               scanningActive;
        
        if (wasCritical != isInCriticalOperation) {
            Log.d(TAG, "Critical operation status changed: " + isInCriticalOperation);
        }
    }
    
    /**
     * Start selling session (critical operation)
     */
    public void startSellingSession() {
        Log.d(TAG, "Selling session started");
        sellingSessionActive = true;
        isInCriticalOperation = true;
        saveActivityData();
    }
    
    /**
     * End selling session
     */
    public void endSellingSession() {
        Log.d(TAG, "Selling session ended");
        sellingSessionActive = false;
        updateCriticalOperationStatus(currentActivity);
        saveActivityData();
    }
    
    /**
     * Start scanning operation (critical operation)
     */
    public void startScanning() {
        Log.d(TAG, "Scanning started");
        scanningActive = true;
        isInCriticalOperation = true;
        saveActivityData();
    }
    
    /**
     * End scanning operation
     */
    public void endScanning() {
        Log.d(TAG, "Scanning ended");
        scanningActive = false;
        updateCriticalOperationStatus(currentActivity);
        saveActivityData();
    }
    
    /**
     * Check if user is currently in a critical operation
     */
    public boolean isInCriticalOperation() {
        return isInCriticalOperation;
    }
    
    /**
     * Get idle time in hours
     */
    public long getIdleTimeHours() {
        long currentTime = System.currentTimeMillis();
        long idleTimeMs = currentTime - lastActivityTime;
        return TimeUnit.MILLISECONDS.toHours(idleTimeMs);
    }
    
    /**
     * Get idle time in minutes
     */
    public long getIdleTimeMinutes() {
        long currentTime = System.currentTimeMillis();
        long idleTimeMs = currentTime - lastActivityTime;
        return TimeUnit.MILLISECONDS.toMinutes(idleTimeMs);
    }
    
    /**
     * Get current activity name
     */
    public String getCurrentActivity() {
        return currentActivity;
    }
    
    /**
     * Get navigation count for current session
     */
    public int getNavigationCount() {
        return navigationCount;
    }
    
    /**
     * Check if navigation trigger should fire (MainActivity -> ReportActivity)
     */
    public boolean shouldTriggerNavigationAd(String fromActivity, String toActivity) {
        return "MainActivity".equals(fromActivity) && "ReportActivity".equals(toActivity);
    }
    
    /**
     * Check if idle trigger should fire for StockActivity
     */
    public boolean shouldTriggerIdleAd(String activityName, long idleThresholdHours) {
        if (!"StockActivity".equals(activityName)) {
            return false;
        }
        
        long idleHours = getIdleTimeHours();
        return idleHours >= idleThresholdHours;
    }
    
    /**
     * Check if backup/restore trigger should fire
     */
    public boolean shouldTriggerBackupRestoreAd() {
        // This would be called when backup/restore functionality is accessed
        return true; // Always show ad before backup/restore operations
    }
    
    /**
     * Reset daily counters
     */
    private void checkDailyReset() {
        long currentTime = System.currentTimeMillis();
        long timeSinceSession = currentTime - sessionStartTime;
        
        if (timeSinceSession >= TimeUnit.DAYS.toMillis(1)) {
            Log.d(TAG, "Performing daily reset of activity counters");
            navigationCount = 0;
            sessionStartTime = currentTime;
        }
    }
    
    /**
     * Get activity state for debugging
     */
    public String getActivityState() {
        return String.format(
            "Activity: %s, Idle: %d min, Critical: %s, Navigation: %d",
            currentActivity,
            getIdleTimeMinutes(),
            isInCriticalOperation,
            navigationCount
        );
    }
}
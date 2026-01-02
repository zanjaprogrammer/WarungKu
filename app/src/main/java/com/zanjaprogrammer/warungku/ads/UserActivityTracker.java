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
     * Check if navigation trigger should fire
     * Strategic timing for better user experience
     */
    public boolean shouldTriggerNavigationAd(String fromActivity, String toActivity) {
        // AGGRESSIVE STRATEGY: More navigation triggers for maximum revenue
        
        // 1. After completing sales (going to reports to check results)
        if ("MainActivity".equals(fromActivity) && "ReportActivity".equals(toActivity)) {
            return true;
        }
        
        // 2. After stock management (going back to main dashboard)
        if ("StockActivity".equals(fromActivity) && "MainActivity".equals(toActivity)) {
            return true;
        }
        
        // 3. After completing transactions (SellActivity -> MainActivity)
        if ("SellActivity".equals(fromActivity) && "MainActivity".equals(toActivity)) {
            return true;
        }
        
        // NEW AGGRESSIVE TRIGGERS:
        
        // 4. Accessing financial data (MainActivity -> HistoryActivity)
        if ("MainActivity".equals(fromActivity) && "HistoryActivity".equals(toActivity)) {
            return true;
        }
        
        // 5. Going to sell products (MainActivity -> SellActivity)
        if ("MainActivity".equals(fromActivity) && "SellActivity".equals(toActivity)) {
            return true;
        }
        
        // 6. Checking reports from any activity
        if ("ReportActivity".equals(toActivity)) {
            return true;
        }
        
        // 7. Adding new products (any activity -> AddProductActivity)
        if ("AddProductActivity".equals(toActivity)) {
            return true;
        }
        
        // 8. Cross-navigation between core activities
        if (("SellActivity".equals(fromActivity) && "StockActivity".equals(toActivity)) ||
            ("StockActivity".equals(fromActivity) && "SellActivity".equals(toActivity))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if idle trigger should fire
     * Shows ads when user returns after being idle
     */
    public boolean shouldTriggerIdleAd(String activityName, long idleThresholdHours) {
        // Only trigger on main activities where user is likely to stay longer
        if (!("MainActivity".equals(activityName) || 
              "StockActivity".equals(activityName) || 
              "ReportActivity".equals(activityName))) {
            return false;
        }
        
        long idleHours = getIdleTimeHours();
        return idleHours >= idleThresholdHours;
    }
    
    /**
     * Check if backup/restore trigger should fire
     */
    public boolean shouldTriggerBackupRestoreAd() {
        // Show ad before accessing premium features like backup/restore
        return true; // Always show ad before backup/restore operations
    }
    
    /**
     * Check if session completion trigger should fire
     * Shows ads after user completes significant actions
     */
    public boolean shouldTriggerSessionCompletionAd() {
        // AGGRESSIVE STRATEGY: Reduced thresholds for more frequent ads
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        long sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration);
        
        // Show ad after 8+ minutes of active usage (reduced from 15 minutes)
        // OR after 3+ navigations (reduced from 5 navigations)
        return (sessionMinutes >= 8 && navigationCount >= 3) || 
               (sessionMinutes >= 5 && navigationCount >= 5);
    }
    
    /**
     * NEW: Check if frequent activity trigger should fire
     * Shows ads based on high activity patterns
     */
    public boolean shouldTriggerFrequentActivityAd() {
        long timeSinceLastNavigation = System.currentTimeMillis() - lastNavigationTime;
        long minutesSinceLastNav = TimeUnit.MILLISECONDS.toMinutes(timeSinceLastNavigation);
        
        // Show ad if user has been very active (many navigations in short time)
        return navigationCount >= 8 && minutesSinceLastNav <= 20;
    }
    
    /**
     * NEW: Check if extended session trigger should fire
     * Shows ads during long usage sessions
     */
    public boolean shouldTriggerExtendedSessionAd() {
        long sessionDuration = System.currentTimeMillis() - sessionStartTime;
        long sessionMinutes = TimeUnit.MILLISECONDS.toMinutes(sessionDuration);
        
        // Show ad every 12 minutes during extended sessions
        return sessionMinutes > 0 && (sessionMinutes % 12 == 0) && navigationCount >= 2;
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
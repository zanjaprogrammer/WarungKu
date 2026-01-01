package com.zanjaprogrammer.warungku.ads;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Monitors system resources for ad optimization
 */
public class SystemMonitor {
    private static final String TAG = "SystemMonitor";
    private static final double LOW_MEMORY_THRESHOLD = 0.15; // 15% available memory
    
    private final Context context;
    private final ActivityManager activityManager;
    
    public SystemMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    /**
     * Check if the system is under memory pressure
     */
    public boolean isMemoryPressureHigh() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            // Calculate available memory percentage
            double availableMemoryRatio = (double) memoryInfo.availMem / memoryInfo.totalMem;
            
            boolean isLowMemory = memoryInfo.lowMemory || availableMemoryRatio < LOW_MEMORY_THRESHOLD;
            
            if (isLowMemory) {
                Log.w(TAG, "High memory pressure detected - Available: " + 
                      (availableMemoryRatio * 100) + "%, Low memory flag: " + memoryInfo.lowMemory);
            }
            
            return isLowMemory;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check memory pressure", e);
            return false; // Assume no pressure if we can't check
        }
    }
    
    /**
     * Get memory usage information for logging
     */
    public String getMemoryInfo() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            
            long availableMB = memoryInfo.availMem / (1024 * 1024);
            long totalMB = memoryInfo.totalMem / (1024 * 1024);
            double availablePercent = (double) memoryInfo.availMem / memoryInfo.totalMem * 100;
            
            return String.format("Memory: %d/%d MB (%.1f%% available), Low: %s", 
                               availableMB, totalMB, availablePercent, memoryInfo.lowMemory);
        } catch (Exception e) {
            return "Memory info unavailable";
        }
    }
    
    /**
     * Check if the device is running low on storage
     */
    public boolean isStorageLow() {
        try {
            android.os.StatFs stat = new android.os.StatFs(context.getFilesDir().getPath());
            long availableBytes = stat.getAvailableBytes();
            long totalBytes = stat.getTotalBytes();
            
            double availableRatio = (double) availableBytes / totalBytes;
            
            // Consider storage low if less than 10% available or less than 100MB
            boolean isLow = availableRatio < 0.1 || availableBytes < (100 * 1024 * 1024);
            
            if (isLow) {
                Log.w(TAG, "Low storage detected - Available: " + (availableBytes / (1024 * 1024)) + " MB");
            }
            
            return isLow;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check storage", e);
            return false;
        }
    }
    
    /**
     * Check if the system is under resource pressure (memory or storage)
     */
    public boolean isResourcePressureHigh() {
        return isMemoryPressureHigh() || isStorageLow();
    }
    
    /**
     * Get recommended ad behavior based on system state
     */
    public AdBehaviorRecommendation getAdBehaviorRecommendation() {
        boolean highMemoryPressure = isMemoryPressureHigh();
        boolean lowStorage = isStorageLow();
        
        if (highMemoryPressure && lowStorage) {
            return AdBehaviorRecommendation.MINIMAL_ADS;
        } else if (highMemoryPressure || lowStorage) {
            return AdBehaviorRecommendation.REDUCED_ADS;
        } else {
            return AdBehaviorRecommendation.NORMAL_ADS;
        }
    }
    
    /**
     * Recommendations for ad behavior based on system resources
     */
    public enum AdBehaviorRecommendation {
        NORMAL_ADS,     // Normal ad loading and refresh
        REDUCED_ADS,    // Reduce refresh frequency, pause some ads
        MINIMAL_ADS     // Only essential ads, long refresh intervals
    }
}
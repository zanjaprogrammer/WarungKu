package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.util.Log;

/**
 * Utility class for managing AdMob configuration switching
 * Provides easy methods to switch between test and production configurations
 */
public class AdConfigManager {
    private static final String TAG = "AdConfigManager";
    
    private final Context context;
    private final AdConfigLoader configLoader;
    
    public AdConfigManager(Context context) {
        this.context = context.getApplicationContext();
        this.configLoader = new AdConfigLoader(context);
    }
    
    /**
     * Switch to test configuration (useful for development/testing)
     */
    public void switchToTestConfig() {
        Log.d(TAG, "Switching to TEST configuration");
        configLoader.forceConfiguration(AdConfigLoader.ConfigType.TEST);
        
        // Restart AdManager to apply new configuration
        restartAdManager();
    }
    
    /**
     * Switch to production configuration (for release builds)
     */
    public void switchToProductionConfig() {
        Log.d(TAG, "Switching to PRODUCTION configuration");
        configLoader.forceConfiguration(AdConfigLoader.ConfigType.PRODUCTION);
        
        // Restart AdManager to apply new configuration
        restartAdManager();
    }
    
    /**
     * Use automatic configuration detection (recommended)
     */
    public void useAutomaticConfig() {
        Log.d(TAG, "Switching to AUTOMATIC configuration detection");
        configLoader.clearManualOverride();
        
        // Restart AdManager to apply new configuration
        restartAdManager();
    }
    
    /**
     * Get current configuration information
     */
    public ConfigInfo getCurrentConfigInfo() {
        AdConfigLoader.AdConfig config = configLoader.loadConfiguration();
        return new ConfigInfo(
            config.configName,
            config.testMode,
            configLoader.getCurrentOverride(),
            config.appId,
            config.description
        );
    }
    
    /**
     * Check if currently using test configuration
     */
    public boolean isUsingTestConfig() {
        return configLoader.loadConfiguration().testMode;
    }
    
    /**
     * Check if manual override is active
     */
    public boolean hasManualOverride() {
        return configLoader.hasManualOverride();
    }
    
    /**
     * Restart AdManager to apply configuration changes
     */
    private void restartAdManager() {
        try {
            AdManager adManager = AdManager.getInstance(context);
            Log.d(TAG, "AdManager configuration updated: " + adManager.getConfigurationStatus());
        } catch (Exception e) {
            Log.e(TAG, "Failed to restart AdManager", e);
        }
    }
    
    /**
     * Configuration information class
     */
    public static class ConfigInfo {
        public final String configName;
        public final boolean testMode;
        public final String overrideType;
        public final String appId;
        public final String description;
        
        public ConfigInfo(String configName, boolean testMode, String overrideType, 
                         String appId, String description) {
            this.configName = configName;
            this.testMode = testMode;
            this.overrideType = overrideType;
            this.appId = appId;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return String.format("AdConfig{name='%s', testMode=%s, override='%s', appId='%s'}", 
                configName, testMode, overrideType, appId);
        }
    }
    
    /**
     * Log current configuration status
     */
    public void logCurrentStatus() {
        ConfigInfo info = getCurrentConfigInfo();
        Log.d(TAG, "=== AdMob Configuration Status ===");
        Log.d(TAG, "Config Name: " + info.configName);
        Log.d(TAG, "Test Mode: " + info.testMode);
        Log.d(TAG, "Override Type: " + info.overrideType);
        Log.d(TAG, "App ID: " + info.appId);
        Log.d(TAG, "Description: " + info.description);
        Log.d(TAG, "================================");
    }
}
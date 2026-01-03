package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for AdMob settings and ad unit IDs
 * Now loads configuration from JSON files with flexible switching
 */
public class AdConfiguration {
    private static final String TAG = "AdConfiguration";
    
    private final AdConfigLoader.AdConfig config;
    private final AdConfigLoader configLoader;
    
    public AdConfiguration(Context context) {
        this.configLoader = new AdConfigLoader(context);
        this.config = configLoader.loadConfiguration();
        
        Log.d(TAG, "AdConfiguration initialized with: " + config.configName);
        Log.d(TAG, "Test mode: " + config.testMode);
        Log.d(TAG, "Manual override: " + configLoader.getCurrentOverride());
    }
    
    // Legacy constructor for backward compatibility
    public AdConfiguration() {
        // Create default configuration for cases where context is not available
        Map<String, String> defaultBannerUnits = new HashMap<>();
        defaultBannerUnits.put("MainActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("StockActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("SellActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("ReportActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("SummaryActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("AddProductActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("HistoryActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("PaymentProofManagementActivity", "ca-app-pub-3940256099942544/6300978111");
        
        this.config = new AdConfigLoader.AdConfig(
            "Legacy Default Configuration",
            "ca-app-pub-3940256099942544~3347511713",
            defaultBannerUnits,
            "ca-app-pub-3940256099942544/1033173712",
            4, 15, 60, 1, true,
            "Legacy fallback configuration"
        );
        this.configLoader = null;
        
        Log.w(TAG, "Using legacy constructor - JSON configuration not available");
    }
    
    public String getAppId() {
        return config.appId;
    }
    
    public Map<String, String> getBannerAdUnitIds() {
        return config.bannerAdUnits;
    }
    
    public String getBannerAdUnitId(String activityName) {
        return config.bannerAdUnits.get(activityName);
    }
    
    public String getInterstitialAdUnitId() {
        return config.interstitialAdUnit;
    }
    
    public int getRefreshIntervalSeconds() {
        return config.refreshIntervalSeconds;
    }
    
    public int getMaxInterstitialPerHour() {
        return config.maxInterstitialPerHour;
    }
    
    public int getInterstitialCooldownMinutes() {
        return config.interstitialCooldownMinutes;
    }
    
    public int getIdleThresholdHours() {
        return config.idleThresholdHours;
    }
    
    public boolean isTestMode() {
        return config.testMode;
    }
    
    public String getConfigName() {
        return config.configName;
    }
    
    public String getDescription() {
        return config.description;
    }
    
    /**
     * Force a specific configuration (for testing/debugging)
     */
    public void forceConfiguration(AdConfigLoader.ConfigType configType) {
        if (configLoader != null) {
            configLoader.forceConfiguration(configType);
            Log.d(TAG, "Configuration forced to: " + configType.name());
        } else {
            Log.w(TAG, "Cannot force configuration - configLoader not available");
        }
    }
    
    /**
     * Clear manual override and use automatic detection
     */
    public void clearManualOverride() {
        if (configLoader != null) {
            configLoader.clearManualOverride();
            Log.d(TAG, "Manual override cleared");
        } else {
            Log.w(TAG, "Cannot clear override - configLoader not available");
        }
    }
    
    /**
     * Get configuration status for debugging
     */
    public String getConfigurationStatus() {
        if (configLoader != null) {
            return String.format("Config: %s | Override: %s | Test Mode: %s", 
                config.configName, 
                configLoader.getCurrentOverride(), 
                config.testMode);
        } else {
            return String.format("Config: %s | Test Mode: %s (Legacy)", 
                config.configName, 
                config.testMode);
        }
    }
}
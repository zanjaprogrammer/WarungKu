package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Loads AdMob configuration from JSON files with flexible switching capabilities
 */
public class AdConfigLoader {
    private static final String TAG = "AdConfigLoader";
    private static final String PREFS_NAME = "ad_config_prefs";
    private static final String KEY_FORCE_CONFIG = "force_config_type";
    private static final String KEY_MANUAL_OVERRIDE = "manual_override_enabled";
    
    public enum ConfigType {
        TEST("ads_config_test.json"),
        PRODUCTION("ads_config_production.json");
        
        private final String fileName;
        
        ConfigType(String fileName) {
            this.fileName = fileName;
        }
        
        public String getFileName() {
            return fileName;
        }
    }
    
    public static class AdConfig {
        public final String configName;
        public final String appId;
        public final Map<String, String> bannerAdUnits;
        public final String interstitialAdUnit;
        public final int maxInterstitialPerHour;
        public final int interstitialCooldownMinutes;
        public final int refreshIntervalSeconds;
        public final int idleThresholdHours;
        public final boolean testMode;
        public final String description;
        
        public AdConfig(String configName, String appId, Map<String, String> bannerAdUnits,
                       String interstitialAdUnit, int maxInterstitialPerHour,
                       int interstitialCooldownMinutes, int refreshIntervalSeconds,
                       int idleThresholdHours, boolean testMode, String description) {
            this.configName = configName;
            this.appId = appId;
            this.bannerAdUnits = bannerAdUnits;
            this.interstitialAdUnit = interstitialAdUnit;
            this.maxInterstitialPerHour = maxInterstitialPerHour;
            this.interstitialCooldownMinutes = interstitialCooldownMinutes;
            this.refreshIntervalSeconds = refreshIntervalSeconds;
            this.idleThresholdHours = idleThresholdHours;
            this.testMode = testMode;
            this.description = description;
        }
    }
    
    private final Context context;
    private final SharedPreferences prefs;
    
    public AdConfigLoader(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Load ad configuration with flexible switching logic
     */
    public AdConfig loadConfiguration() {
        ConfigType configType = determineConfigType();
        Log.d(TAG, "Loading configuration: " + configType.name());
        
        try {
            AdConfig config = loadConfigFromAssets(configType);
            Log.d(TAG, "Successfully loaded: " + config.configName);
            Log.d(TAG, "Test mode: " + config.testMode);
            Log.d(TAG, "App ID: " + config.appId);
            return config;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load " + configType.name() + " config, falling back to TEST", e);
            try {
                return loadConfigFromAssets(ConfigType.TEST);
            } catch (Exception fallbackError) {
                Log.e(TAG, "Failed to load fallback config", fallbackError);
                return createDefaultConfig();
            }
        }
    }
    
    /**
     * Determine which configuration to use based on multiple factors
     */
    private ConfigType determineConfigType() {
        // 1. Check for manual override (highest priority)
        if (prefs.getBoolean(KEY_MANUAL_OVERRIDE, false)) {
            String forcedConfig = prefs.getString(KEY_FORCE_CONFIG, "");
            if ("PRODUCTION".equals(forcedConfig)) {
                Log.d(TAG, "Using manual override: PRODUCTION");
                return ConfigType.PRODUCTION;
            } else if ("TEST".equals(forcedConfig)) {
                Log.d(TAG, "Using manual override: TEST");
                return ConfigType.TEST;
            }
        }
        
        // 2. Check build type (debug vs release)
        boolean isDebugBuild = (context.getApplicationInfo().flags & 
                               android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        
        if (isDebugBuild) {
            Log.d(TAG, "Debug build detected, using TEST configuration");
            return ConfigType.TEST;
        }
        
        // 3. Check if running on emulator (fallback for release builds in testing)
        if (isEmulator()) {
            Log.d(TAG, "Emulator detected, using TEST configuration");
            return ConfigType.TEST;
        }
        
        // 4. Default to production for release builds on real devices
        Log.d(TAG, "Release build on real device, using PRODUCTION configuration");
        return ConfigType.PRODUCTION;
    }
    
    /**
     * Load configuration from JSON asset file
     */
    private AdConfig loadConfigFromAssets(ConfigType configType) throws IOException, JSONException {
        String jsonString = loadJsonFromAssets(configType.getFileName());
        JSONObject json = new JSONObject(jsonString);
        
        // Parse banner ad units
        Map<String, String> bannerAdUnits = new HashMap<>();
        JSONObject bannerUnits = json.getJSONObject("bannerAdUnits");
        Iterator<String> keys = bannerUnits.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            bannerAdUnits.put(key, bannerUnits.getString(key));
        }
        
        // Parse ad settings
        JSONObject adSettings = json.getJSONObject("adSettings");
        
        return new AdConfig(
            json.getString("configName"),
            json.getString("appId"),
            bannerAdUnits,
            json.getString("interstitialAdUnit"),
            adSettings.getInt("maxInterstitialPerHour"),
            adSettings.getInt("interstitialCooldownMinutes"),
            adSettings.getInt("refreshIntervalSeconds"),
            adSettings.getInt("idleThresholdHours"),
            json.getBoolean("testMode"),
            json.optString("description", "")
        );
    }
    
    /**
     * Load JSON string from assets
     */
    private String loadJsonFromAssets(String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }
    
    /**
     * Create default configuration as fallback
     */
    private AdConfig createDefaultConfig() {
        Log.w(TAG, "Creating default fallback configuration");
        Map<String, String> defaultBannerUnits = new HashMap<>();
        defaultBannerUnits.put("MainActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("StockActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("SellActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("ReportActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("SummaryActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("AddProductActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("HistoryActivity", "ca-app-pub-3940256099942544/6300978111");
        defaultBannerUnits.put("PaymentProofManagementActivity", "ca-app-pub-3940256099942544/6300978111");
        
        return new AdConfig(
            "Default Fallback Configuration",
            "ca-app-pub-3940256099942544~3347511713",
            defaultBannerUnits,
            "ca-app-pub-3940256099942544/1033173712",
            4, 15, 60, 1, true,
            "Fallback configuration with test ad units"
        );
    }
    
    /**
     * Check if running on emulator
     */
    private boolean isEmulator() {
        return android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || android.os.Build.MANUFACTURER.contains("Genymotion")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(android.os.Build.PRODUCT);
    }
    
    /**
     * Manually force a specific configuration (for testing/debugging)
     */
    public void forceConfiguration(ConfigType configType) {
        Log.d(TAG, "Forcing configuration to: " + configType.name());
        prefs.edit()
            .putBoolean(KEY_MANUAL_OVERRIDE, true)
            .putString(KEY_FORCE_CONFIG, configType.name())
            .apply();
    }
    
    /**
     * Clear manual override and use automatic detection
     */
    public void clearManualOverride() {
        Log.d(TAG, "Clearing manual configuration override");
        prefs.edit()
            .putBoolean(KEY_MANUAL_OVERRIDE, false)
            .remove(KEY_FORCE_CONFIG)
            .apply();
    }
    
    /**
     * Check if manual override is active
     */
    public boolean hasManualOverride() {
        return prefs.getBoolean(KEY_MANUAL_OVERRIDE, false);
    }
    
    /**
     * Get current override type (if any)
     */
    public String getCurrentOverride() {
        if (hasManualOverride()) {
            return prefs.getString(KEY_FORCE_CONFIG, "NONE");
        }
        return "AUTO";
    }
}
package com.zanjaprogrammer.warungku.ads;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for AdMob settings and ad unit IDs
 */
public class AdConfiguration {
    // Test AdMob App ID - replace with real ID in production
    public static final String APP_ID = "ca-app-pub-3940256099942544~3347511713";
    
    // Test Ad Unit IDs - replace with real IDs in production
    public static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    public static final String TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    
    // Banner ad unit IDs for different activities
    private final Map<String, String> bannerAdUnitIds;
    private final String interstitialAdUnitId;
    private final int refreshIntervalSeconds;
    private final int maxInterstitialPerHour;
    private final int interstitialCooldownMinutes;
    private final int idleThresholdHours;
    
    public AdConfiguration() {
        this.bannerAdUnitIds = new HashMap<>();
        
        // BANNER STRATEGY: Show on ALL pages for maximum revenue
        // Banner ads are less intrusive and can be shown everywhere
        
        this.bannerAdUnitIds.put("MainActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("StockActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("SellActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("ReportActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("SummaryActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("AddProductActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("HistoryActivity", TEST_BANNER_AD_UNIT_ID);
        this.bannerAdUnitIds.put("PaymentProofManagementActivity", TEST_BANNER_AD_UNIT_ID);
        
        this.interstitialAdUnitId = TEST_INTERSTITIAL_AD_UNIT_ID;
        this.refreshIntervalSeconds = 60;
        
        // INTERSTITIAL AD TIMING STRATEGY - AGGRESSIVE REVENUE OPTIMIZATION
        // Maximize revenue while maintaining acceptable user experience
        this.maxInterstitialPerHour = 6;        // Increased from 2 to 6 (1 every 10 minutes)
        this.interstitialCooldownMinutes = 10;  // Reduced from 20 to 10 minutes
        this.idleThresholdHours = 1;            // Reduced from 2 to 1 hour (faster idle detection)
    }
    
    public AdConfiguration(Map<String, String> bannerAdUnitIds, String interstitialAdUnitId,
                          int refreshIntervalSeconds, int maxInterstitialPerHour,
                          int interstitialCooldownMinutes, int idleThresholdHours) {
        this.bannerAdUnitIds = bannerAdUnitIds;
        this.interstitialAdUnitId = interstitialAdUnitId;
        this.refreshIntervalSeconds = refreshIntervalSeconds;
        this.maxInterstitialPerHour = maxInterstitialPerHour;
        this.interstitialCooldownMinutes = interstitialCooldownMinutes;
        this.idleThresholdHours = idleThresholdHours;
    }
    
    public String getAppId() {
        return APP_ID;
    }
    
    public Map<String, String> getBannerAdUnitIds() {
        return bannerAdUnitIds;
    }
    
    public String getBannerAdUnitId(String activityName) {
        return bannerAdUnitIds.get(activityName);
    }
    
    public String getInterstitialAdUnitId() {
        return interstitialAdUnitId;
    }
    
    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }
    
    public int getMaxInterstitialPerHour() {
        return maxInterstitialPerHour;
    }
    
    public int getInterstitialCooldownMinutes() {
        return interstitialCooldownMinutes;
    }
    
    public int getIdleThresholdHours() {
        return idleThresholdHours;
    }
}
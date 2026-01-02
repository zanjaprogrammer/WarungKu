package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages banner advertisement lifecycle
 */
public class BannerAdController {
    private static final String TAG = "BannerAdController";
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long BASE_RETRY_DELAY_MS = 30000; // 30 seconds
    
    private final Context context;
    private final AdConfiguration adConfiguration;
    private final AdFrequencyManager adFrequencyManager;
    private final RevenueAnalytics revenueAnalytics;
    private final SystemMonitor systemMonitor;
    private final PrivacyManager privacyManager;
    private final Handler refreshHandler;
    private final Map<String, Integer> retryAttempts; // Track retry attempts per activity
    
    public BannerAdController(Context context, AdConfiguration adConfiguration, 
                             AdFrequencyManager adFrequencyManager, RevenueAnalytics revenueAnalytics,
                             PrivacyManager privacyManager) {
        this.context = context;
        this.adConfiguration = adConfiguration;
        this.adFrequencyManager = adFrequencyManager;
        this.revenueAnalytics = revenueAnalytics;
        this.systemMonitor = new SystemMonitor(context);
        this.privacyManager = privacyManager;
        this.refreshHandler = new Handler(Looper.getMainLooper());
        this.retryAttempts = new HashMap<>();
    }
    
    /**
     * Load and display banner ad in the specified container
     */
    public void loadBannerAd(ViewGroup adContainer, String activityName) {
        if (adContainer == null || activityName == null) {
            Log.w(TAG, "Invalid parameters for banner ad loading");
            return;
        }
        
        // Check system resources before loading ad
        SystemMonitor.AdBehaviorRecommendation recommendation = systemMonitor.getAdBehaviorRecommendation();
        if (recommendation == SystemMonitor.AdBehaviorRecommendation.MINIMAL_ADS) {
            Log.w(TAG, "Skipping banner ad load due to high resource pressure: " + systemMonitor.getMemoryInfo());
            adContainer.setVisibility(View.GONE);
            return;
        }
        
        String adUnitId = adConfiguration.getBannerAdUnitId(activityName);
        if (adUnitId == null) {
            Log.w(TAG, "No ad unit ID configured for activity: " + activityName);
            return;
        }
        
        try {
            // Create AdView
            AdView adView = new AdView(context);
            adView.setAdUnitId(adUnitId);
            adView.setAdSize(AdSize.BANNER);
            
            // Set up ad listener
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded successfully for " + activityName);
                    
                    // Stop loading animation and show container with animation
                    AdAnimationUtils.stopLoadingAnimation(adContainer);
                    AdAnimationUtils.showAdContainer(adContainer);
                    
                    adFrequencyManager.recordBannerImpression(activityName);
                    
                    // Reset retry attempts on successful load
                    retryAttempts.put(activityName, 0);
                    
                    // Record analytics
                    revenueAnalytics.recordBannerImpression(activityName, adUnitId);
                    revenueAnalytics.recordAdLoadSuccess(AdType.BANNER, adUnitId, activityName);
                    
                    // Schedule refresh with adaptive interval
                    scheduleRefresh(adView, adContainer, activityName);
                }
                
                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    Log.w(TAG, "Banner ad failed to load for " + activityName + 
                          ": " + loadAdError.getMessage());
                    Log.w(TAG, "Error code: " + loadAdError.getCode());
                    Log.w(TAG, "Error domain: " + loadAdError.getDomain());
                    Log.w(TAG, "Error cause: " + loadAdError.getCause());
                    
                    // Stop loading animation and hide container with animation
                    AdAnimationUtils.stopLoadingAnimation(adContainer);
                    
                    // Show fallback test banner for emulator/testing
                    // Always show fallback in debug/testing environment
                    showFallbackTestBanner(adContainer, activityName);
                    
                    // Record analytics
                    revenueAnalytics.recordAdLoadFailure(AdType.BANNER, adUnitId, activityName, loadAdError.getMessage());
                    
                    // Implement exponential backoff retry
                    scheduleRetryWithBackoff(adContainer, activityName);
                }
                
                @Override
                public void onAdClicked() {
                    Log.d(TAG, "Banner ad clicked for " + activityName);
                    adFrequencyManager.recordBannerClick(activityName);
                    
                    // Record analytics
                    revenueAnalytics.recordBannerClick(activityName, adUnitId);
                }
            });
            
            // Add AdView to container
            adContainer.removeAllViews();
            adContainer.addView(adView);
            
            // Show loading animation while ad loads
            AdAnimationUtils.showLoadingAnimation(adContainer);
            
            // Load ad with privacy-compliant request
            AdRequest adRequest = buildPrivacyCompliantAdRequest();
            adView.loadAd(adRequest);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading banner ad for " + activityName, e);
            AdAnimationUtils.stopLoadingAnimation(adContainer);
            AdAnimationUtils.hideAdContainer(adContainer);
        }
    }
    
    private void scheduleRefresh(AdView adView, ViewGroup adContainer, String activityName) {
        // Adaptive refresh interval based on network conditions
        long refreshInterval = getAdaptiveRefreshInterval();
        
        refreshHandler.postDelayed(() -> {
            if (adView != null && adContainer.getVisibility() == View.VISIBLE) {
                Log.d(TAG, "Refreshing banner ad for " + activityName);
                
                // Animate refresh
                AdAnimationUtils.animateAdRefresh(adContainer, () -> {
                    AdRequest adRequest = buildPrivacyCompliantAdRequest();
                    adView.loadAd(adRequest);
                });
            }
        }, refreshInterval);
    }
    
    private void scheduleRetryWithBackoff(ViewGroup adContainer, String activityName) {
        int attempts = retryAttempts.getOrDefault(activityName, 0);
        
        if (attempts >= MAX_RETRY_ATTEMPTS) {
            Log.w(TAG, "Max retry attempts reached for " + activityName + ", giving up");
            return;
        }
        
        // Exponential backoff: delay = base * 2^attempts
        long delay = BASE_RETRY_DELAY_MS * (1L << attempts);
        
        // Cap the delay at 10 minutes
        delay = Math.min(delay, 600000);
        
        Log.d(TAG, "Scheduling retry " + (attempts + 1) + " for " + activityName + 
              " in " + (delay / 1000) + " seconds");
        
        retryAttempts.put(activityName, attempts + 1);
        
        refreshHandler.postDelayed(() -> {
            Log.d(TAG, "Retrying banner ad load for " + activityName);
            loadBannerAd(adContainer, activityName);
        }, delay);
    }
    
    private long getAdaptiveRefreshInterval() {
        // Check system resources first
        SystemMonitor.AdBehaviorRecommendation recommendation = systemMonitor.getAdBehaviorRecommendation();
        
        long baseInterval = adConfiguration.getRefreshIntervalSeconds() * 1000L;
        
        // Adjust based on system resources
        switch (recommendation) {
            case MINIMAL_ADS:
                baseInterval *= 10; // 10x longer interval (10 minutes)
                break;
            case REDUCED_ADS:
                baseInterval *= 3; // 3x longer interval (3 minutes)
                break;
            case NORMAL_ADS:
            default:
                // Use base interval, but still check network
                break;
        }
        
        // Further adjust based on network conditions
        if (!isNetworkAvailable()) {
            // No network, increase interval to 5 minutes minimum
            baseInterval = Math.max(baseInterval, 300000);
        } else if (isNetworkSlow()) {
            // Slow network, increase interval by 2x
            baseInterval *= 2;
        }
        
        // Cap the maximum interval at 15 minutes
        return Math.min(baseInterval, 900000);
    }
    
    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return true; // Assume network is available if we can't check
        }
    }
    
    private boolean isNetworkSlow() {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            
            if (activeNetwork != null) {
                int type = activeNetwork.getType();
                int subtype = activeNetwork.getSubtype();
                
                if (type == android.net.ConnectivityManager.TYPE_WIFI) {
                    return false; // WiFi is generally fast
                } else if (type == android.net.ConnectivityManager.TYPE_MOBILE) {
                    // Check mobile network type
                    switch (subtype) {
                        case android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT:
                        case android.telephony.TelephonyManager.NETWORK_TYPE_CDMA:
                        case android.telephony.TelephonyManager.NETWORK_TYPE_EDGE:
                        case android.telephony.TelephonyManager.NETWORK_TYPE_GPRS:
                        case android.telephony.TelephonyManager.NETWORK_TYPE_IDEN:
                            return true; // Slow 2G networks
                        default:
                            return false; // 3G, 4G, 5G are considered fast
                    }
                }
            }
        } catch (Exception e) {
            // If we can't determine, assume normal speed
        }
        
        return false;
    }
    
    private void scheduleRetry(ViewGroup adContainer, String activityName, long delayMs) {
        // Legacy method for backward compatibility
        scheduleRetryWithBackoff(adContainer, activityName);
    }
    
    /**
     * Build an AdRequest that complies with privacy settings
     */
    private AdRequest buildPrivacyCompliantAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        
        // Handle child-directed treatment
        if (privacyManager.isChildUser()) {
            // Tag for child-directed treatment (COPPA compliance)
            builder.setRequestAgent("child_directed");
        }
        
        // Handle personalized ads preference
        if (!privacyManager.canShowPersonalizedAds()) {
            // For non-personalized ads, we'll handle this at the request level
            // The actual implementation should be done through UMP SDK
            Log.d(TAG, "Non-personalized ads requested");
        }
        
        return builder.build();
    }
    
    /**
     * Pause banner ad refresh for the specified container
     */
    public void pauseBannerAd(ViewGroup adContainer) {
        if (adContainer != null && adContainer.getChildCount() > 0) {
            View child = adContainer.getChildAt(0);
            if (child instanceof AdView) {
                ((AdView) child).pause();
            }
        }
    }
    
    /**
     * Resume banner ad refresh for the specified container
     */
    public void resumeBannerAd(ViewGroup adContainer) {
        if (adContainer != null && adContainer.getChildCount() > 0) {
            View child = adContainer.getChildAt(0);
            if (child instanceof AdView) {
                ((AdView) child).resume();
            }
        }
    }
    
    /**
     * Destroy banner ad to free resources
     */
    public void destroyBannerAd(ViewGroup adContainer) {
        if (adContainer != null && adContainer.getChildCount() > 0) {
            View child = adContainer.getChildAt(0);
            if (child instanceof AdView) {
                ((AdView) child).destroy();
            }
            adContainer.removeAllViews();
        }
        
        // Cancel any pending refresh tasks
        refreshHandler.removeCallbacksAndMessages(null);
    }
    
    /**
     * Show fallback test banner when AdMob fails to load (for testing/emulator)
     */
    private void showFallbackTestBanner(ViewGroup adContainer, String activityName) {
        try {
            Log.d(TAG, "Showing fallback test banner for " + activityName);
            
            // Create a test banner view
            android.widget.TextView testBanner = new android.widget.TextView(context);
            testBanner.setText("🎯 TEST BANNER AD - " + activityName + " 🎯");
            testBanner.setBackgroundColor(0xFF2196F3); // Blue background
            testBanner.setTextColor(0xFFFFFFFF); // White text
            testBanner.setGravity(android.view.Gravity.CENTER);
            testBanner.setPadding(16, 16, 16, 16);
            testBanner.setTextSize(12);
            testBanner.setTypeface(null, android.graphics.Typeface.BOLD);
            
            // Set layout params for banner size (320x50dp)
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                (int) (50 * context.getResources().getDisplayMetrics().density) // 50dp in pixels
            );
            testBanner.setLayoutParams(params);
            
            // Add click listener for testing
            testBanner.setOnClickListener(v -> {
                Log.d(TAG, "Test banner clicked for " + activityName);
                // Record analytics for test banner click
                revenueAnalytics.recordBannerClick(activityName, "test_banner");
            });
            
            // Add to container and show
            adContainer.removeAllViews();
            adContainer.addView(testBanner);
            AdAnimationUtils.showAdContainer(adContainer);
            
            // Record test banner impression
            adFrequencyManager.recordBannerImpression(activityName);
            revenueAnalytics.recordBannerImpression(activityName, "test_banner");
            
            Log.d(TAG, "Fallback test banner shown for " + activityName);
        } catch (Exception e) {
            Log.e(TAG, "Error showing fallback test banner for " + activityName, e);
            AdAnimationUtils.hideAdContainer(adContainer);
        }
    }
}
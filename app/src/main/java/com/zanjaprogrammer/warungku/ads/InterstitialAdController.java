package com.zanjaprogrammer.warungku.ads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * Controls interstitial advertisement display
 */
public class InterstitialAdController {
    private static final String TAG = "InterstitialAdController";
    
    private final Context context;
    private final AdConfiguration adConfiguration;
    private final AdFrequencyManager adFrequencyManager;
    private final UserActivityTracker userActivityTracker;
    private final RevenueAnalytics revenueAnalytics;
    private final PrivacyManager privacyManager;
    private InterstitialAd interstitialAd;
    private boolean isLoading = false;
    
    public InterstitialAdController(Context context, AdConfiguration adConfiguration,
                                  AdFrequencyManager adFrequencyManager, UserActivityTracker userActivityTracker,
                                  RevenueAnalytics revenueAnalytics, PrivacyManager privacyManager) {
        this.context = context;
        this.adConfiguration = adConfiguration;
        this.adFrequencyManager = adFrequencyManager;
        this.userActivityTracker = userActivityTracker;
        this.revenueAnalytics = revenueAnalytics;
        this.privacyManager = privacyManager;
        
        // Preload first interstitial ad
        preloadInterstitialAd();
    }
    
    /**
     * Preload interstitial ad in background
     */
    public void preloadInterstitialAd() {
        if (isLoading || interstitialAd != null) {
            Log.d(TAG, "Interstitial ad already loaded or loading");
            return;
        }
        
        String adUnitId = adConfiguration.getInterstitialAdUnitId();
        if (adUnitId == null) {
            Log.w(TAG, "No interstitial ad unit ID configured");
            return;
        }
        
        isLoading = true;
        AdRequest adRequest = buildPrivacyCompliantAdRequest();
        
        InterstitialAd.load(context, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                Log.d(TAG, "Interstitial ad loaded successfully");
                interstitialAd = ad;
                isLoading = false;
                
                // Record analytics
                revenueAnalytics.recordAdLoadSuccess(AdType.INTERSTITIAL, adUnitId, "preload");
                
                // Set up full screen content callback
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdClicked() {
                        Log.d(TAG, "Interstitial ad clicked");
                        adFrequencyManager.recordInterstitialClick();
                        revenueAnalytics.recordInterstitialClick(adUnitId, "unknown");
                    }
                    
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad dismissed");
                        interstitialAd = null;
                        adFrequencyManager.recordInterstitialCompletion();
                        revenueAnalytics.recordInterstitialCompletion(adUnitId, "unknown");
                        
                        // Preload next ad
                        preloadInterstitialAd();
                    }
                    
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.w(TAG, "Interstitial ad failed to show: " + adError.getMessage());
                        interstitialAd = null;
                        
                        // Preload next ad
                        preloadInterstitialAd();
                    }
                    
                    @Override
                    public void onAdImpression() {
                        Log.d(TAG, "Interstitial ad impression recorded");
                        adFrequencyManager.recordInterstitialImpression();
                        revenueAnalytics.recordInterstitialImpression(adUnitId, "unknown");
                    }
                    
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad showed full screen content");
                    }
                });
            }
            
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.w(TAG, "Interstitial ad failed to load: " + loadAdError.getMessage());
                interstitialAd = null;
                isLoading = false;
                
                // Record analytics
                revenueAnalytics.recordAdLoadFailure(AdType.INTERSTITIAL, adUnitId, "preload", loadAdError.getMessage());
            }
        });
    }
    
    /**
     * Show interstitial ad if conditions are met
     */
    public boolean showInterstitialAd(Activity activity, String trigger) {
        if (activity == null || trigger == null) {
            Log.w(TAG, "Invalid parameters for interstitial ad");
            return false;
        }
        
        // Check if we can show interstitial ad
        if (!adFrequencyManager.canShowInterstitialAd()) {
            Log.d(TAG, "Interstitial ad blocked by frequency manager");
            return false;
        }
        
        // Check if user is in critical operation
        if (userActivityTracker.isInCriticalOperation()) {
            Log.d(TAG, "Interstitial ad blocked - user in critical operation");
            return false;
        }
        
        if (interstitialAd == null) {
            Log.d(TAG, "No interstitial ad available to show");
            // Try to preload for next time
            preloadInterstitialAd();
            return false;
        }
        
        try {
            Log.d(TAG, "Showing interstitial ad for trigger: " + trigger);
            interstitialAd.show(activity);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error showing interstitial ad", e);
            interstitialAd = null;
            return false;
        }
    }
    
    /**
     * Check navigation-based trigger for MainActivity to ReportActivity
     */
    public boolean shouldShowForNavigation(String fromActivity, String toActivity) {
        return userActivityTracker.shouldTriggerNavigationAd(fromActivity, toActivity) &&
               adFrequencyManager.canShowInterstitialAd();
    }
    
    /**
     * Check idle-based trigger for StockActivity
     */
    public boolean shouldShowForIdleAccess(String activityName, long idleTimeHours) {
        return userActivityTracker.shouldTriggerIdleAd(activityName, idleTimeHours) &&
               adFrequencyManager.canShowInterstitialAd();
    }
    
    /**
     * Check backup/restore trigger
     */
    public boolean shouldShowForBackupRestore() {
        return userActivityTracker.shouldTriggerBackupRestoreAd() &&
               adFrequencyManager.canShowInterstitialAd();
    }
    
    /**
     * Get current interstitial ad availability
     */
    public boolean isInterstitialAdReady() {
        return interstitialAd != null;
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
            // Request non-personalized ads
            android.os.Bundle extras = new android.os.Bundle();
            extras.putString("npa", "1"); // Non-personalized ads
            builder.addNetworkExtrasBundle(com.google.android.gms.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        
        return builder.build();
    }
}
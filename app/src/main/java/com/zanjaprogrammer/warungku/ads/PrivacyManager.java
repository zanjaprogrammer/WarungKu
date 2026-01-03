package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;

import java.util.Locale;

/**
 * Manages privacy compliance and user consent for ads
 */
public class PrivacyManager {
    private static final String TAG = "PrivacyManager";
    private static final String PREFS_NAME = "privacy_prefs";
    private static final String KEY_CONSENT_STATUS = "consent_status";
    private static final String KEY_PERSONALIZED_ADS_ENABLED = "personalized_ads_enabled";
    private static final String KEY_USER_AGE_VERIFIED = "user_age_verified";
    private static final String KEY_IS_CHILD_USER = "is_child_user";
    
    private final Context context;
    private final SharedPreferences prefs;
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;
    
    public PrivacyManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.consentInformation = UserMessagingPlatform.getConsentInformation(context);
    }
    
    /**
     * Initialize privacy compliance and check consent status
     */
    public void initialize(PrivacyCallback callback) {
        Log.d(TAG, "Initializing privacy compliance");
        
        try {
            // Check if user is in EU (GDPR applies)
            if (isEUUser()) {
                // For now, skip GDPR consent form in ApplicationContext
                // This should be handled in Activity context
                Log.d(TAG, "EU user detected, but skipping consent form in ApplicationContext");
                callback.onPrivacyInitialized(true);
            } else {
                // Non-EU users, check for child safety
                if (isChildUser()) {
                    enableChildSafeMode();
                }
                callback.onPrivacyInitialized(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during privacy initialization", e);
            callback.onPrivacyInitialized(false);
        }
    }
    
    private void initializeGDPRConsent(PrivacyCallback callback) {
        // Skip GDPR consent form initialization in ApplicationContext
        // This should be handled in Activity context to avoid ClassCastException
        Log.d(TAG, "Skipping GDPR consent form - requires Activity context");
        callback.onPrivacyInitialized(true);
    }
    
    private void loadConsentForm(PrivacyCallback callback) {
        // Skip consent form loading in ApplicationContext
        Log.d(TAG, "Skipping consent form loading - requires Activity context");
        callback.onPrivacyInitialized(true);
    }
    
    private void showConsentForm(PrivacyCallback callback) {
        if (consentForm != null && context instanceof android.app.Activity) {
            consentForm.show(
                (android.app.Activity) context,
                formError -> {
                    if (formError != null) {
                        Log.e(TAG, "Consent form error: " + formError.getMessage());
                    }
                    
                    // Save consent status
                    saveConsentStatus();
                    callback.onPrivacyInitialized(true);
                }
            );
        } else {
            Log.w(TAG, "Cannot show consent form - form not loaded or context not an Activity");
            callback.onPrivacyInitialized(false);
        }
    }
    
    private void saveConsentStatus() {
        int consentStatus = consentInformation.getConsentStatus();
        prefs.edit().putInt(KEY_CONSENT_STATUS, consentStatus).apply();
        
        Log.d(TAG, "Consent status saved: " + consentStatus);
    }
    
    /**
     * Check if personalized ads are allowed
     */
    public boolean canShowPersonalizedAds() {
        // Check GDPR consent
        if (isEUUser()) {
            int consentStatus = consentInformation.getConsentStatus();
            if (consentStatus != ConsentInformation.ConsentStatus.OBTAINED) {
                return false;
            }
        }
        
        // Check user preference
        boolean personalizedAdsEnabled = prefs.getBoolean(KEY_PERSONALIZED_ADS_ENABLED, true);
        
        // Child users cannot have personalized ads
        if (isChildUser()) {
            return false;
        }
        
        return personalizedAdsEnabled;
    }
    
    /**
     * Set user preference for personalized ads
     */
    public void setPersonalizedAdsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PERSONALIZED_ADS_ENABLED, enabled).apply();
        Log.d(TAG, "Personalized ads preference set to: " + enabled);
    }
    
    /**
     * Set whether the user is a child (under 13)
     */
    public void setChildUser(boolean isChild) {
        prefs.edit()
            .putBoolean(KEY_IS_CHILD_USER, isChild)
            .putBoolean(KEY_USER_AGE_VERIFIED, true)
            .apply();
        
        if (isChild) {
            enableChildSafeMode();
        }
        
        Log.d(TAG, "Child user status set to: " + isChild);
    }
    
    /**
     * Check if the user is a child
     */
    public boolean isChildUser() {
        return prefs.getBoolean(KEY_IS_CHILD_USER, false);
    }
    
    /**
     * Check if user age has been verified
     */
    public boolean isUserAgeVerified() {
        return prefs.getBoolean(KEY_USER_AGE_VERIFIED, false);
    }
    
    /**
     * Enable child-safe ad targeting
     */
    private void enableChildSafeMode() {
        // Disable personalized ads for children
        setPersonalizedAdsEnabled(false);
        Log.d(TAG, "Child-safe mode enabled");
    }
    
    /**
     * Check if the user is in the EU (simplified check)
     */
    private boolean isEUUser() {
        // Simple check based on device locale
        // In production, you might want to use IP geolocation or other methods
        String country = Locale.getDefault().getCountry();
        
        // EU country codes (simplified list)
        String[] euCountries = {
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR",
            "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL",
            "PL", "PT", "RO", "SK", "SI", "ES", "SE"
        };
        
        for (String euCountry : euCountries) {
            if (euCountry.equals(country)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if we're in debug mode
     */
    private boolean isDebugMode() {
        return (context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }
    
    /**
     * Show privacy settings dialog
     */
    public void showPrivacySettings(android.app.Activity activity, PrivacySettingsCallback callback) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        builder.setTitle("Privacy Settings");
        
        // Create options
        String[] options = {
            "Personalized Ads: " + (canShowPersonalizedAds() ? "Enabled" : "Disabled"),
            "Age Verification: " + (isUserAgeVerified() ? "Verified" : "Not Verified"),
            "GDPR Consent: " + getConsentStatusText()
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Personalized ads toggle
                    togglePersonalizedAds(activity, callback);
                    break;
                case 1: // Age verification
                    showAgeVerificationDialog(activity, callback);
                    break;
                case 2: // GDPR consent
                    if (isEUUser() && consentForm != null) {
                        consentForm.show(activity, formError -> {
                            saveConsentStatus();
                            callback.onSettingsChanged();
                        });
                    }
                    break;
            }
        });
        
        builder.setNegativeButton("Close", null);
        builder.show();
    }
    
    private void togglePersonalizedAds(android.app.Activity activity, PrivacySettingsCallback callback) {
        boolean currentSetting = canShowPersonalizedAds();
        
        new android.app.AlertDialog.Builder(activity)
            .setTitle("Personalized Ads")
            .setMessage("Do you want to enable personalized ads? This will use your activity to show more relevant advertisements.")
            .setPositiveButton("Enable", (dialog, which) -> {
                setPersonalizedAdsEnabled(true);
                callback.onSettingsChanged();
            })
            .setNegativeButton("Disable", (dialog, which) -> {
                setPersonalizedAdsEnabled(false);
                callback.onSettingsChanged();
            })
            .show();
    }
    
    private void showAgeVerificationDialog(android.app.Activity activity, PrivacySettingsCallback callback) {
        new android.app.AlertDialog.Builder(activity)
            .setTitle("Age Verification")
            .setMessage("Are you 13 years of age or older?")
            .setPositiveButton("Yes (13+)", (dialog, which) -> {
                setChildUser(false);
                callback.onSettingsChanged();
            })
            .setNegativeButton("No (Under 13)", (dialog, which) -> {
                setChildUser(true);
                callback.onSettingsChanged();
            })
            .show();
    }
    
    private String getConsentStatusText() {
        if (!isEUUser()) {
            return "Not Required";
        }
        
        int status = consentInformation.getConsentStatus();
        switch (status) {
            case ConsentInformation.ConsentStatus.OBTAINED:
                return "Granted";
            case ConsentInformation.ConsentStatus.REQUIRED:
                return "Required";
            case ConsentInformation.ConsentStatus.NOT_REQUIRED:
                return "Not Required";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get privacy compliance summary for logging
     */
    public String getPrivacyStatus() {
        return String.format("Privacy Status - EU User: %s, Child: %s, Personalized Ads: %s, Consent: %s",
            isEUUser(), isChildUser(), canShowPersonalizedAds(), getConsentStatusText());
    }
    
    /**
     * Callback interface for privacy initialization
     */
    public interface PrivacyCallback {
        void onPrivacyInitialized(boolean success);
    }
    
    /**
     * Callback interface for privacy settings changes
     */
    public interface PrivacySettingsCallback {
        void onSettingsChanged();
    }
}
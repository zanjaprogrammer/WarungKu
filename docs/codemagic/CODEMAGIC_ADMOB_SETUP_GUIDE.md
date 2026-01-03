# Codemagic AdMob Setup Guide

## Overview
With the new JSON-based AdMob configuration system, Codemagic builds will automatically use the correct ad configuration based on build type. **No additional Codemagic configuration is needed** - the system is fully automated!

## How It Works

### **Automatic Configuration Selection**
- ✅ **Release builds** → Production AdMob configuration (`ads_config_production.json`)
- ✅ **Debug builds** → Test AdMob configuration (`ads_config_test.json`)
- ✅ **No manual intervention required** - fully automated

### **Build Process Flow**
1. **Codemagic starts build**
2. **AdMob validation step** checks production configuration
3. **Release build** automatically loads production JSON
4. **App uses real ad units** in production

## Pre-Build Setup Required

### **1. Update Production AdMob Configuration**
Before triggering Codemagic build, ensure your production configuration is ready:

**Edit:** `app/src/main/assets/ads_config_production.json`

```json
{
  "configName": "Production Configuration",
  "appId": "ca-app-pub-YOUR_PUBLISHER_ID~YOUR_APP_ID",
  "bannerAdUnits": {
    "MainActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_1",
    "StockActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_2",
    "SellActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_3",
    "ReportActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_4",
    "SummaryActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_5",
    "AddProductActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_6",
    "HistoryActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_7",
    "PaymentProofManagementActivity": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_BANNER_ID_8"
  },
  "interstitialAdUnit": "ca-app-pub-YOUR_PUBLISHER_ID/YOUR_INTERSTITIAL_ID",
  "adSettings": {
    "maxInterstitialPerHour": 4,
    "interstitialCooldownMinutes": 15,
    "refreshIntervalSeconds": 60,
    "idleThresholdHours": 1
  },
  "testMode": false,
  "description": "Real AdMob ad units for production release"
}
```

**⚠️ Important:** Replace all `YOUR_PUBLISHER_ID` and `YOUR_*_ID` with your actual AdMob ad unit IDs.

### **2. Commit Configuration Files**
```bash
git add app/src/main/assets/ads_config_production.json
git add app/src/main/assets/ads_config_test.json
git commit -m "Update AdMob production configuration"
git push
```

## Codemagic Build Validation

### **Enhanced codemagic.yaml Features**
The updated `codemagic.yaml` now includes:

#### **1. AdMob Configuration Validation**
```yaml
- name: Validate AdMob Configuration
  script: |
    # Checks if production config exists
    # Warns if placeholder values are still present
    # Validates configuration structure
```

#### **2. Build Summary**
```yaml
- name: Build Summary
  script: |
    # Shows which AdMob configuration will be used
    # Displays build artifact information
    # Confirms automatic configuration selection
```

### **Build Log Output**
During Codemagic build, you'll see:

```
🎯 Validating AdMob configuration for production build...
✅ Production AdMob config found
✅ Production AdMob config appears to be properly configured
📋 Production config details:
  "configName": "Production Configuration",
  "testMode": false,
  "description": "Real AdMob ad units for production release"
✅ Test AdMob config found (fallback available)
🎯 AdMob configuration validation complete

📋 WarungKu Build Summary
========================
🏗️ Build Type: Release (production AdMob config will be used automatically)
🎯 AdMob Config: Production JSON will be loaded for release builds
```

## Environment Variables (Optional)

### **AdMob App ID Validation**
You can optionally set in Codemagic UI:
- `ADMOB_APP_ID` - Your AdMob app ID for additional validation

### **Existing Variables (Required)**
- `CM_KEYSTORE` - Base64 encoded keystore for signing
- `CM_KEYSTORE_PASSWORD` - Keystore password
- `CM_KEY_ALIAS` - Key alias
- `CM_KEY_PASSWORD` - Key password
- `SUPABASE_URL` - Supabase project URL
- `SUPABASE_PUBLISHABLE_KEY` - Supabase publishable key
- `EMAIL_RECIPIENT` - Email for build notifications

## Build Artifacts

### **Release Build (with keystore)**
- ✅ `app-release.aab` - Android App Bundle (for Play Store)
- ✅ `app-release.apk` - APK file (for direct distribution)
- ✅ Uses **production AdMob configuration** automatically

### **Debug Build (always built)**
- ✅ `app-debug.apk` - Debug APK
- ✅ Uses **test AdMob configuration** automatically

## Troubleshooting

### **Build Fails with AdMob Validation Error**
```
❌ ERROR: Production AdMob config not found!
```
**Solution:** Ensure `app/src/main/assets/ads_config_production.json` exists and is committed to repository.

### **Warning: Placeholder Values Detected**
```
⚠️ WARNING: Production AdMob config contains placeholder values!
```
**Solution:** Replace all `XXXXXXXXXXXXXXXXX` placeholders with real AdMob ad unit IDs.

### **Ads Not Showing in Production**
1. **Check AdMob account** - Ensure ad units are active
2. **Verify ad unit IDs** - Confirm IDs in production JSON are correct
3. **Check app approval** - AdMob may need to approve your app
4. **Review logs** - Check device logs for AdMob errors

## Testing Production Configuration

### **Before Codemagic Build**
Test production configuration locally:

```java
// In any activity
AdConfigManager configManager = new AdConfigManager(this);
configManager.switchToProductionConfig();
configManager.logCurrentStatus();

// Test ads, then switch back
configManager.useAutomaticConfig();
```

### **After Codemagic Build**
1. **Install release APK** on test device
2. **Verify production ads** are loading
3. **Check configuration logs** in device logcat
4. **Test ad frequency** and user experience

## Security Notes

### **Safe to Commit**
- ✅ `ads_config_test.json` - Contains Google test ad units
- ✅ `ads_config_production.json` - Ad unit IDs are not sensitive

### **Keep Private (if needed)**
If you prefer to keep production ad unit IDs private:
1. Add `ads_config_production.json` to `.gitignore`
2. Set up as Codemagic environment variable
3. Create file during build process

## Summary

✅ **No Codemagic configuration changes needed** - System is fully automated
✅ **Release builds automatically use production ads** - No manual intervention
✅ **Debug builds automatically use test ads** - Safe for development
✅ **Validation built-in** - Codemagic will warn about configuration issues
✅ **Fallback protection** - Always falls back to test configuration if needed

The AdMob configuration system is designed to work seamlessly with Codemagic CI/CD pipeline with zero additional configuration required!
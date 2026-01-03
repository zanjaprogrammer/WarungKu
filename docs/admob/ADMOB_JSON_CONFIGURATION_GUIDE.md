# AdMob JSON Configuration Guide

## Overview
The AdMob configuration has been refactored to use flexible JSON-based configuration files, allowing easy switching between test and production ad units.

## Configuration Files

### 1. Test Configuration (`app/src/main/assets/ads_config_test.json`)
- Contains Google's test ad unit IDs
- Used for development and testing
- Safe to use during development (no real ad revenue)

### 2. Production Configuration (`app/src/main/assets/ads_config_production.json`)
- Contains your real AdMob ad unit IDs
- Used for production releases
- **IMPORTANT**: Replace placeholder IDs with your actual AdMob ad units

## Automatic Configuration Selection

The system automatically chooses the appropriate configuration based on:

1. **Manual Override** (Highest Priority)
   - If you manually force a specific configuration
   - Useful for testing specific scenarios

2. **Build Type Detection**
   - Debug builds → Test configuration
   - Release builds → Production configuration

3. **Emulator Detection**
   - Running on emulator → Test configuration
   - Running on real device → Production configuration

## How to Configure Production Ad Units

### Step 1: Get Your AdMob Ad Unit IDs
1. Go to [AdMob Console](https://apps.admob.com/)
2. Create your app if not already created
3. Create ad units for:
   - Banner ads (8 different activities)
   - Interstitial ads (1 unit for all activities)

### Step 2: Update Production Configuration
Edit `app/src/main/assets/ads_config_production.json`:

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

## Manual Configuration Control

### Using AdConfigManager (Programmatically)
```java
AdConfigManager configManager = new AdConfigManager(context);

// Switch to test configuration
configManager.switchToTestConfig();

// Switch to production configuration
configManager.switchToProductionConfig();

// Use automatic detection (recommended)
configManager.useAutomaticConfig();

// Check current configuration
AdConfigManager.ConfigInfo info = configManager.getCurrentConfigInfo();
Log.d("AdConfig", "Current config: " + info.toString());
```

### Using AdManager (Direct Access)
```java
AdManager adManager = AdManager.getInstance(context);

// Force specific configuration
adManager.forceAdConfiguration(AdConfigLoader.ConfigType.TEST);
adManager.forceAdConfiguration(AdConfigLoader.ConfigType.PRODUCTION);

// Clear override
adManager.clearConfigurationOverride();

// Check status
String status = adManager.getConfigurationStatus();
Log.d("AdManager", status);
```

## Configuration Priority (Highest to Lowest)

1. **Manual Override** - `forceConfiguration()`
2. **Build Type** - Debug vs Release
3. **Device Type** - Emulator vs Real Device
4. **Default Fallback** - Test configuration

## Benefits of This Approach

✅ **Flexible Switching**: Easy to switch between test/production
✅ **Automatic Detection**: Smart defaults based on build type and device
✅ **Manual Override**: Full control when needed for testing
✅ **Centralized Config**: All ad settings in one place
✅ **Version Control Safe**: Can gitignore production config if needed
✅ **Fallback Protection**: Always falls back to safe test configuration

## Testing Scenarios

### Development Testing
- Debug builds automatically use test configuration
- Emulator automatically uses test configuration
- Safe to test without affecting real ad revenue

### Production Testing
- Force production config on debug build: `configManager.switchToProductionConfig()`
- Test real ad units before release
- Switch back to auto: `configManager.useAutomaticConfig()`

### Release Builds
- Automatically uses production configuration on real devices
- Uses test configuration on emulators (for testing release builds)

## Troubleshooting

### Configuration Not Loading
- Check if JSON files exist in `app/src/main/assets/`
- Verify JSON syntax is valid
- Check logcat for configuration loading errors

### Wrong Configuration Being Used
- Check manual override status: `configManager.hasManualOverride()`
- Clear override: `configManager.useAutomaticConfig()`
- Check build type and device type detection

### Ad Units Not Working
- Verify ad unit IDs are correct in JSON files
- Check if using test vs production configuration
- Ensure AdMob account is properly set up

## Security Notes

⚠️ **Important**: 
- Test configuration is safe to commit to version control
- Consider gitignoring production configuration if it contains sensitive IDs
- Never commit real ad unit IDs to public repositories

## Configuration Status Logging

Add this to any activity to check current configuration:
```java
AdConfigManager configManager = new AdConfigManager(this);
configManager.logCurrentStatus();
```

This will log detailed configuration information to help with debugging.
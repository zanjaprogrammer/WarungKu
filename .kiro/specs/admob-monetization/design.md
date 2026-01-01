# AdMob Monetization Design Document

## Overview

The AdMob monetization system for WarungKu will integrate Google AdMob SDK to display banner and interstitial advertisements strategically throughout the application. The design prioritizes user experience by implementing conservative ad placement that doesn't interfere with core business workflows while maximizing revenue potential.

The system will feature intelligent ad frequency management, comprehensive error handling, and robust analytics tracking to ensure optimal performance and user satisfaction.

## Architecture

The monetization system follows a modular architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    UI Layer (Activities)                    │
│  MainActivity │ SellActivity │ StockActivity │ ReportActivity │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                   Ad Management Layer                       │
│  AdManager │ BannerAdController │ InterstitialAdController   │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                  Business Logic Layer                       │
│ AdFrequencyManager │ RevenueAnalytics │ UserActivityTracker │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                               │
│    SharedPreferences │ Local Analytics Storage              │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                   AdMob SDK Layer                           │
│        Google AdMob SDK │ Ad Unit Management                │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### AdManager
Central coordinator for all ad-related operations:
- Initializes AdMob SDK
- Manages ad unit IDs and configurations
- Coordinates between banner and interstitial controllers
- Handles global ad settings and privacy compliance

### BannerAdController
Manages banner advertisement lifecycle:
- Loads and displays banner ads in activities
- Handles banner refresh cycles (60-second intervals)
- Manages banner visibility and layout adjustments
- Implements retry logic for failed banner loads

### InterstitialAdController
Controls interstitial advertisement display:
- Preloads interstitial ads in background
- Manages display triggers based on user flow
- Implements frequency capping and timing rules
- Handles interstitial ad lifecycle events

### AdFrequencyManager
Tracks and controls ad display frequency:
- Monitors ads shown per session and per hour
- Implements cooling-off periods for interstitial ads
- Tracks user activity states (selling, idle, navigation)
- Persists frequency data across app sessions

### RevenueAnalytics
Collects and analyzes ad performance data:
- Tracks impressions, clicks, and completion rates
- Calculates revenue metrics and trends
- Generates exportable reports (CSV format)
- Monitors ad loading success/failure rates

### UserActivityTracker
Monitors user behavior for intelligent ad timing:
- Tracks idle time and activity patterns
- Identifies critical operations (selling, scanning)
- Manages activity state transitions
- Provides context for ad display decisions

## Data Models

### AdConfiguration
```kotlin
data class AdConfiguration(
    val appId: String,
    val bannerAdUnitIds: Map<String, String>, // activity -> ad unit ID
    val interstitialAdUnitId: String,
    val refreshIntervalSeconds: Int = 60,
    val maxInterstitialPerHour: Int = 3,
    val interstitialCooldownMinutes: Int = 30,
    val idleThresholdHours: Int = 1
)
```

### AdFrequencyData
```kotlin
data class AdFrequencyData(
    val sessionStartTime: Long,
    val interstitialAdsShown: Int,
    val lastInterstitialTime: Long,
    val dailyResetTime: Long,
    val isInCooldown: Boolean
)
```

### AdAnalyticsEvent
```kotlin
data class AdAnalyticsEvent(
    val timestamp: Long,
    val adType: AdType, // BANNER, INTERSTITIAL
    val eventType: AdEventType, // IMPRESSION, CLICK, LOAD_SUCCESS, LOAD_FAILURE
    val adUnitId: String,
    val activityName: String,
    val revenue: Double? = null
)
```

### UserActivityState
```kotlin
data class UserActivityState(
    val currentActivity: String,
    val lastActivityTime: Long,
    val isInCriticalOperation: Boolean, // selling, scanning
    val idleDurationMinutes: Long,
    val navigationCount: Int
)
```
## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

After reviewing the acceptance criteria, several properties can be consolidated to eliminate redundancy and provide comprehensive validation:

**Property Reflection:**
- Properties 1-4 (individual activity banner tests) can be consolidated into a single comprehensive banner display property
- Properties for interstitial error handling and banner error handling share similar patterns and can be unified
- Analytics tracking properties can be combined into broader tracking validation properties

**Property 1: AdMob initialization resilience**
*For any* app startup scenario, AdMob SDK initialization should either succeed or fail gracefully without causing application crashes
**Validates: Requirements 1.2, 1.3**

**Property 2: Offline functionality preservation**
*For any* network connectivity state, core application functionality should remain fully operational regardless of ad loading status
**Validates: Requirements 1.4**

**Property 3: Banner ad layout integrity**
*For any* activity with banner ads, the banner should not overlap with functional UI elements and should adjust layout properly when loading fails
**Validates: Requirements 2.5, 2.6**

**Property 4: Banner refresh consistency**
*For any* visible banner ad, it should refresh automatically every 60 seconds and load independently during activity navigation
**Validates: Requirements 2.7, 2.8**

**Property 5: Idle-based interstitial timing**
*For any* user session, when accessing StockActivity after being idle for more than 1 hour, an interstitial ad should be displayed
**Validates: Requirements 3.2**

**Property 6: Critical operation ad suppression**
*For any* user session, interstitial ads should never be displayed during active selling processes or product scanning operations
**Validates: Requirements 3.4, 4.4, 6.4**

**Property 7: Interstitial UI consistency**
*For any* displayed interstitial ad, it should provide a close button after exactly 5 seconds and show visual feedback when successfully loaded
**Validates: Requirements 3.5, 6.6**

**Property 8: Frequency limiting enforcement**
*For any* user session, the system should never display more than 3 interstitial ads per hour and should enforce a 30-minute cooldown after reaching the limit
**Validates: Requirements 3.6, 4.2**

**Property 9: Ad error handling resilience**
*For any* ad loading failure (banner or interstitial), the system should continue normal app flow without delays or crashes
**Validates: Requirements 3.7, 7.1, 7.3, 7.6**

**Property 10: Frequency data persistence**
*For any* ad frequency data, it should be stored locally and restored correctly after app restarts, with automatic 24-hour resets
**Validates: Requirements 4.1, 4.3, 4.5, 4.6**

**Property 11: Comprehensive analytics tracking**
*For any* ad event (impression, click, completion), the system should record accurate analytics data including revenue information
**Validates: Requirements 5.1, 5.2, 5.3, 5.6**

**Property 12: Analytics export functionality**
*For any* analytics data period, the system should generate accurate daily, weekly, and monthly summaries exportable in CSV format
**Validates: Requirements 5.4, 5.5**

**Property 13: Background preloading efficiency**
*For any* interstitial ad request, the system should preload ads in background and show loading indicators for banner ads
**Validates: Requirements 6.2, 6.3**

**Property 14: Adaptive retry behavior**
*For any* repeated ad loading failures, the system should implement exponential backoff and reduce refresh frequency under poor network conditions
**Validates: Requirements 7.2, 7.4, 7.5**

**Property 15: Privacy compliance enforcement**
*For any* user interaction, the system should properly handle GDPR consent, child-safe targeting, and personalized ad opt-out preferences
**Validates: Requirements 8.1, 8.2, 8.3**

## Error Handling

The monetization system implements comprehensive error handling at multiple levels:

### SDK Level Errors
- AdMob initialization failures are logged and tracked
- Network connectivity issues are handled gracefully
- Invalid ad unit configurations trigger fallback mechanisms
- SDK version compatibility issues are detected and reported

### Ad Loading Errors
- Banner ad failures trigger automatic retries with exponential backoff
- Interstitial ad failures skip the ad display and continue normal flow
- Timeout handling prevents indefinite loading states
- Memory pressure scenarios are detected and handled appropriately

### Frequency Management Errors
- Corrupted frequency data triggers reset to default state
- Time synchronization issues are handled with local timestamps
- SharedPreferences failures fall back to in-memory tracking
- Invalid frequency configurations use safe default values

### Analytics Errors
- Failed analytics writes are queued for retry
- Export failures provide user feedback and alternative formats
- Data corruption triggers automatic cleanup and recovery
- Storage space issues are detected and managed

## Testing Strategy

The AdMob monetization system will employ a dual testing approach combining unit tests and property-based tests to ensure comprehensive coverage and correctness validation.

### Unit Testing Approach
Unit tests will focus on:
- Specific integration points between AdMob SDK and application components
- Error handling scenarios with mocked failure conditions
- UI component behavior during ad loading and display states
- Analytics data formatting and export functionality
- Privacy compliance mechanisms and user preference handling

### Property-Based Testing Approach
Property-based tests will verify universal properties using **fast-check** library for JavaScript/TypeScript, configured to run a minimum of 100 iterations per test. Each property-based test will be tagged with comments explicitly referencing the correctness property from this design document using the format: **Feature: admob-monetization, Property {number}: {property_text}**

Property tests will validate:
- Ad frequency management across various user behavior patterns
- Error resilience under different failure scenarios
- Analytics accuracy across diverse ad interaction sequences
- Layout integrity across different screen sizes and orientations
- Timing behavior under various system load conditions

The combination of unit and property-based tests ensures that specific integration bugs are caught while universal correctness properties are verified across all possible input scenarios.
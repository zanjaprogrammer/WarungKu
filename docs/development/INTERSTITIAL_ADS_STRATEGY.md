# 🎯 Interstitial Ads Strategy - WarungKu

## ⏰ Time Limits & Frequency

### Current Configuration:
- **Max per Hour**: 2 ads (reduced from 3 for better UX)
- **Cooldown**: 20 minutes between ads (reduced from 30)
- **Idle Threshold**: 2 hours (increased from 1 for less intrusion)

### Rationale:
- **Balanced Revenue**: Still generates good revenue with 2 ads/hour
- **Better UX**: Shorter cooldown but fewer total ads
- **Smart Timing**: Longer idle threshold prevents annoying frequent users

## 🎯 Trigger Points

### 1. **Navigation Triggers** (Task Completion)
Shows ads when users complete meaningful tasks:

- **MainActivity → ReportActivity**: After sales, checking results
- **StockActivity → MainActivity**: After inventory management
- **SellActivity → MainActivity**: After completing transactions

**Why**: Users are in a natural break point after completing tasks.

### 2. **Idle Return Triggers**
Shows ads when users return after being idle:

- **Trigger Activities**: MainActivity, StockActivity, ReportActivity
- **Idle Time**: 2+ hours of inactivity
- **Logic**: Welcome back ads for returning users

**Why**: Users returning after long breaks are more tolerant of ads.

### 3. **Premium Feature Access**
Shows ads before accessing premium features:

- **Backup/Restore Operations**: Always show before these features
- **Export Functions**: Before data export operations

**Why**: Users accessing premium features expect some monetization.

### 4. **Session Completion** (New!)
Shows ads after extended usage sessions:

- **Session Duration**: 15+ minutes of active usage
- **Navigation Count**: 5+ screen transitions
- **Logic**: Reward engaged users with relevant ads

**Why**: Engaged users are more likely to interact with ads positively.

## 📊 Implementation Details

### Frequency Management:
```java
// AdConfiguration.java
maxInterstitialPerHour = 2;        // Max 2 ads per hour
interstitialCooldownMinutes = 20;  // 20 min cooldown
idleThresholdHours = 2;            // 2 hour idle threshold
```

### Smart Triggers:
```java
// UserActivityTracker.java
shouldTriggerNavigationAd()      // Task completion
shouldTriggerIdleAd()           // Return from idle
shouldTriggerBackupRestoreAd()  // Premium features
shouldTriggerSessionCompletionAd() // Extended sessions
```

## 🎨 User Experience Considerations

### ✅ **Good Timing**:
- After completing sales transactions
- Before accessing premium features
- When returning from long breaks
- After extended engaged sessions

### ❌ **Avoid Showing**:
- During active selling/checkout process
- While scanning barcodes
- During data entry
- In the middle of critical operations

### 🔄 **Fallback Strategy**:
- If ad fails to load, continue normal flow
- Preload next ad in background
- Never block user actions waiting for ads

## 📈 Revenue Optimization

### Current Strategy:
1. **Quality over Quantity**: Fewer, better-timed ads
2. **User Retention Focus**: Don't annoy users with too many ads
3. **Strategic Placement**: Show ads at natural break points
4. **Premium Monetization**: Ads before premium features

### Future Enhancements:
- A/B test different frequencies
- Seasonal adjustments (busy periods = fewer ads)
- User behavior analysis for personalized timing
- Reward ads (optional ads for benefits)

## 🔧 Configuration Options

To adjust the strategy, modify `AdConfiguration.java`:

```java
// Conservative (better UX)
maxInterstitialPerHour = 1;
interstitialCooldownMinutes = 30;
idleThresholdHours = 3;

// Aggressive (more revenue)
maxInterstitialPerHour = 3;
interstitialCooldownMinutes = 15;
idleThresholdHours = 1;

// Current Balanced
maxInterstitialPerHour = 2;
interstitialCooldownMinutes = 20;
idleThresholdHours = 2;
```

## 📱 Testing

### Test Scenarios:
1. **Navigation Flow**: MainActivity → ReportActivity (should show ad)
2. **Task Completion**: Complete a sale, return to main (should show ad)
3. **Idle Return**: Leave app for 2+ hours, return (should show ad)
4. **Frequency Limit**: Try to trigger 3 ads in 1 hour (3rd should be blocked)
5. **Cooldown**: Try to show ads within 20 minutes (should be blocked)

### Debug Logs:
- Check `AdFrequencyManager` logs for frequency blocking
- Check `UserActivityTracker` logs for trigger conditions
- Check `InterstitialAdController` logs for ad loading/showing

## 🎯 Success Metrics

### Revenue Metrics:
- **eCPM**: Effective cost per mille
- **Fill Rate**: Percentage of ad requests filled
- **CTR**: Click-through rate

### UX Metrics:
- **Session Duration**: Average time spent in app
- **Retention Rate**: Users returning after seeing ads
- **Task Completion**: Users completing sales after ads

### Balance Point:
- Target: 2 ads/hour with 90%+ user retention
- Monitor: If retention drops below 85%, reduce frequency
- Optimize: If eCPM is low, improve ad placement timing
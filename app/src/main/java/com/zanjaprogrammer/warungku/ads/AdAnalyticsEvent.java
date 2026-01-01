package com.zanjaprogrammer.warungku.ads;

/**
 * Represents an ad analytics event
 */
public class AdAnalyticsEvent {
    private final long timestamp;
    private final AdType adType;
    private final AdEventType eventType;
    private final String adUnitId;
    private final String activityName;
    private final Double revenue;
    
    public AdAnalyticsEvent(long timestamp, AdType adType, AdEventType eventType, 
                           String adUnitId, String activityName, Double revenue) {
        this.timestamp = timestamp;
        this.adType = adType;
        this.eventType = eventType;
        this.adUnitId = adUnitId;
        this.activityName = activityName;
        this.revenue = revenue;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public AdType getAdType() {
        return adType;
    }
    
    public AdEventType getEventType() {
        return eventType;
    }
    
    public String getAdUnitId() {
        return adUnitId;
    }
    
    public String getActivityName() {
        return activityName;
    }
    
    public Double getRevenue() {
        return revenue;
    }
}

/**
 * Types of ads
 */
enum AdType {
    BANNER, INTERSTITIAL
}

/**
 * Types of ad events
 */
enum AdEventType {
    IMPRESSION, CLICK, LOAD_SUCCESS, LOAD_FAILURE, COMPLETION, REVENUE
}
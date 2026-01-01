package com.zanjaprogrammer.warungku.ads;

import java.util.Map;

/**
 * Summary of analytics data for a specific time period
 */
public class AnalyticsSummary {
    private final long startTime;
    private final long endTime;
    private final int bannerImpressions;
    private final int bannerClicks;
    private final int interstitialImpressions;
    private final int interstitialClicks;
    private final int interstitialCompletions;
    private final int loadSuccesses;
    private final int loadFailures;
    private final double totalRevenue;
    private final Map<String, Integer> activityBreakdown;
    
    public AnalyticsSummary(long startTime, long endTime, 
                           int bannerImpressions, int bannerClicks,
                           int interstitialImpressions, int interstitialClicks, int interstitialCompletions,
                           int loadSuccesses, int loadFailures,
                           double totalRevenue, Map<String, Integer> activityBreakdown) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.bannerImpressions = bannerImpressions;
        this.bannerClicks = bannerClicks;
        this.interstitialImpressions = interstitialImpressions;
        this.interstitialClicks = interstitialClicks;
        this.interstitialCompletions = interstitialCompletions;
        this.loadSuccesses = loadSuccesses;
        this.loadFailures = loadFailures;
        this.totalRevenue = totalRevenue;
        this.activityBreakdown = activityBreakdown;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public int getBannerImpressions() {
        return bannerImpressions;
    }
    
    public int getBannerClicks() {
        return bannerClicks;
    }
    
    public int getInterstitialImpressions() {
        return interstitialImpressions;
    }
    
    public int getInterstitialClicks() {
        return interstitialClicks;
    }
    
    public int getInterstitialCompletions() {
        return interstitialCompletions;
    }
    
    public int getLoadSuccesses() {
        return loadSuccesses;
    }
    
    public int getLoadFailures() {
        return loadFailures;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public Map<String, Integer> getActivityBreakdown() {
        return activityBreakdown;
    }
    
    public double getBannerCTR() {
        return bannerImpressions > 0 ? (double) bannerClicks / bannerImpressions * 100.0 : 0.0;
    }
    
    public double getInterstitialCompletionRate() {
        return interstitialImpressions > 0 ? (double) interstitialCompletions / interstitialImpressions * 100.0 : 0.0;
    }
    
    public double getLoadSuccessRate() {
        int total = loadSuccesses + loadFailures;
        return total > 0 ? (double) loadSuccesses / total * 100.0 : 0.0;
    }
    
    public int getTotalImpressions() {
        return bannerImpressions + interstitialImpressions;
    }
    
    public int getTotalClicks() {
        return bannerClicks + interstitialClicks;
    }
}
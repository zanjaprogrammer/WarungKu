package com.zanjaprogrammer.warungku.ads;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Collects and analyzes ad performance data
 */
public class RevenueAnalytics {
    private static final String TAG = "RevenueAnalytics";
    private static final String PREFS_NAME = "revenue_analytics_prefs";
    private static final String KEY_ANALYTICS_DATA = "analytics_data";
    private static final String KEY_DAILY_SUMMARY = "daily_summary";
    
    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final List<AdAnalyticsEvent> analyticsEvents;
    
    public RevenueAnalytics(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.analyticsEvents = new ArrayList<>();
        
        loadAnalyticsData();
    }
    
    private void loadAnalyticsData() {
        String jsonData = prefs.getString(KEY_ANALYTICS_DATA, "[]");
        Type listType = new TypeToken<List<AdAnalyticsEvent>>(){}.getType();
        
        try {
            List<AdAnalyticsEvent> loadedEvents = gson.fromJson(jsonData, listType);
            if (loadedEvents != null) {
                analyticsEvents.addAll(loadedEvents);
                Log.d(TAG, "Loaded " + analyticsEvents.size() + " analytics events");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load analytics data", e);
        }
        
        // Clean old data (keep only last 30 days)
        cleanOldData();
    }
    
    private void saveAnalyticsData() {
        try {
            String jsonData = gson.toJson(analyticsEvents);
            prefs.edit().putString(KEY_ANALYTICS_DATA, jsonData).apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save analytics data", e);
        }
    }
    
    private void cleanOldData() {
        long thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30);
        analyticsEvents.removeIf(event -> event.getTimestamp() < thirtyDaysAgo);
        
        if (analyticsEvents.size() > 1000) {
            // Keep only the most recent 1000 events
            analyticsEvents.subList(0, analyticsEvents.size() - 1000).clear();
        }
    }
    
    /**
     * Record banner ad impression
     */
    public void recordBannerImpression(String activityName, String adUnitId) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            AdType.BANNER,
            AdEventType.IMPRESSION,
            adUnitId,
            activityName,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded banner impression for " + activityName);
    }
    
    /**
     * Record banner ad click
     */
    public void recordBannerClick(String activityName, String adUnitId) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            AdType.BANNER,
            AdEventType.CLICK,
            adUnitId,
            activityName,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded banner click for " + activityName);
    }
    
    /**
     * Record interstitial ad impression
     */
    public void recordInterstitialImpression(String adUnitId, String trigger) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            AdType.INTERSTITIAL,
            AdEventType.IMPRESSION,
            adUnitId,
            trigger,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded interstitial impression for trigger: " + trigger);
    }
    
    /**
     * Record interstitial ad click
     */
    public void recordInterstitialClick(String adUnitId, String trigger) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            AdType.INTERSTITIAL,
            AdEventType.CLICK,
            adUnitId,
            trigger,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded interstitial click for trigger: " + trigger);
    }
    
    /**
     * Record interstitial ad completion
     */
    public void recordInterstitialCompletion(String adUnitId, String trigger) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            AdType.INTERSTITIAL,
            AdEventType.COMPLETION,
            adUnitId,
            trigger,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded interstitial completion for trigger: " + trigger);
    }
    
    /**
     * Record ad loading success
     */
    public void recordAdLoadSuccess(AdType adType, String adUnitId, String context) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            adType,
            AdEventType.LOAD_SUCCESS,
            adUnitId,
            context,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded ad load success: " + adType + " for " + context);
    }
    
    /**
     * Record ad loading failure
     */
    public void recordAdLoadFailure(AdType adType, String adUnitId, String context, String errorMessage) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            adType,
            AdEventType.LOAD_FAILURE,
            adUnitId,
            context,
            null
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded ad load failure: " + adType + " for " + context + " - " + errorMessage);
    }
    
    /**
     * Record revenue (if available from AdMob)
     */
    public void recordRevenue(AdType adType, String adUnitId, String context, double revenue) {
        AdAnalyticsEvent event = new AdAnalyticsEvent(
            System.currentTimeMillis(),
            adType,
            AdEventType.REVENUE,
            adUnitId,
            context,
            revenue
        );
        
        analyticsEvents.add(event);
        saveAnalyticsData();
        
        Log.d(TAG, "Recorded revenue: " + revenue + " for " + adType + " in " + context);
    }
    
    /**
     * Get daily summary for a specific date
     */
    public AnalyticsSummary getDailySummary(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();
        long dayEnd = dayStart + TimeUnit.DAYS.toMillis(1);
        
        return generateSummary(dayStart, dayEnd);
    }
    
    /**
     * Get weekly summary for the week containing the specified date
     */
    public AnalyticsSummary getWeeklySummary(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();
        long weekEnd = weekStart + TimeUnit.DAYS.toMillis(7);
        
        return generateSummary(weekStart, weekEnd);
    }
    
    /**
     * Get monthly summary for the month containing the specified date
     */
    public AnalyticsSummary getMonthlySummary(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long monthStart = cal.getTimeInMillis();
        
        cal.add(Calendar.MONTH, 1);
        long monthEnd = cal.getTimeInMillis();
        
        return generateSummary(monthStart, monthEnd);
    }
    
    private AnalyticsSummary generateSummary(long startTime, long endTime) {
        int bannerImpressions = 0;
        int bannerClicks = 0;
        int interstitialImpressions = 0;
        int interstitialClicks = 0;
        int interstitialCompletions = 0;
        int loadSuccesses = 0;
        int loadFailures = 0;
        double totalRevenue = 0.0;
        
        Map<String, Integer> activityBreakdown = new HashMap<>();
        
        for (AdAnalyticsEvent event : analyticsEvents) {
            if (event.getTimestamp() >= startTime && event.getTimestamp() < endTime) {
                switch (event.getEventType()) {
                    case IMPRESSION:
                        if (event.getAdType() == AdType.BANNER) {
                            bannerImpressions++;
                            activityBreakdown.put(event.getActivityName(), 
                                activityBreakdown.getOrDefault(event.getActivityName(), 0) + 1);
                        } else {
                            interstitialImpressions++;
                        }
                        break;
                    case CLICK:
                        if (event.getAdType() == AdType.BANNER) {
                            bannerClicks++;
                        } else {
                            interstitialClicks++;
                        }
                        break;
                    case COMPLETION:
                        if (event.getAdType() == AdType.INTERSTITIAL) {
                            interstitialCompletions++;
                        }
                        break;
                    case LOAD_SUCCESS:
                        loadSuccesses++;
                        break;
                    case LOAD_FAILURE:
                        loadFailures++;
                        break;
                    case REVENUE:
                        if (event.getRevenue() != null) {
                            totalRevenue += event.getRevenue();
                        }
                        break;
                }
            }
        }
        
        return new AnalyticsSummary(
            startTime, endTime,
            bannerImpressions, bannerClicks,
            interstitialImpressions, interstitialClicks, interstitialCompletions,
            loadSuccesses, loadFailures,
            totalRevenue, activityBreakdown
        );
    }
    
    /**
     * Export analytics data to CSV format
     */
    public File exportToCSV(long startTime, long endTime) {
        try {
            File exportDir = new File(context.getExternalFilesDir(null), "analytics");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String fileName = "ad_analytics_" + dateFormat.format(new Date()) + ".csv";
            File csvFile = new File(exportDir, fileName);
            
            FileWriter writer = new FileWriter(csvFile);
            
            // Write CSV header
            writer.append("Timestamp,Date,AdType,EventType,AdUnitId,Activity,Revenue\n");
            
            SimpleDateFormat csvDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
            // Write data rows
            for (AdAnalyticsEvent event : analyticsEvents) {
                if (event.getTimestamp() >= startTime && event.getTimestamp() < endTime) {
                    writer.append(String.valueOf(event.getTimestamp())).append(",");
                    writer.append(csvDateFormat.format(new Date(event.getTimestamp()))).append(",");
                    writer.append(event.getAdType().toString()).append(",");
                    writer.append(event.getEventType().toString()).append(",");
                    writer.append(event.getAdUnitId()).append(",");
                    writer.append(event.getActivityName()).append(",");
                    writer.append(event.getRevenue() != null ? String.valueOf(event.getRevenue()) : "0.0");
                    writer.append("\n");
                }
            }
            
            writer.close();
            
            Log.d(TAG, "Analytics data exported to: " + csvFile.getAbsolutePath());
            return csvFile;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to export analytics data", e);
            return null;
        }
    }
    
    /**
     * Get current analytics summary for today
     */
    public AnalyticsSummary getTodaySummary() {
        return getDailySummary(System.currentTimeMillis());
    }
    
    /**
     * Get load success rate as percentage
     */
    public double getLoadSuccessRate(long startTime, long endTime) {
        int successes = 0;
        int failures = 0;
        
        for (AdAnalyticsEvent event : analyticsEvents) {
            if (event.getTimestamp() >= startTime && event.getTimestamp() < endTime) {
                if (event.getEventType() == AdEventType.LOAD_SUCCESS) {
                    successes++;
                } else if (event.getEventType() == AdEventType.LOAD_FAILURE) {
                    failures++;
                }
            }
        }
        
        int total = successes + failures;
        return total > 0 ? (double) successes / total * 100.0 : 0.0;
    }
    
    /**
     * Get click-through rate for banner ads
     */
    public double getBannerCTR(long startTime, long endTime) {
        int impressions = 0;
        int clicks = 0;
        
        for (AdAnalyticsEvent event : analyticsEvents) {
            if (event.getTimestamp() >= startTime && event.getTimestamp() < endTime && 
                event.getAdType() == AdType.BANNER) {
                if (event.getEventType() == AdEventType.IMPRESSION) {
                    impressions++;
                } else if (event.getEventType() == AdEventType.CLICK) {
                    clicks++;
                }
            }
        }
        
        return impressions > 0 ? (double) clicks / impressions * 100.0 : 0.0;
    }
    
    /**
     * Get completion rate for interstitial ads
     */
    public double getInterstitialCompletionRate(long startTime, long endTime) {
        int impressions = 0;
        int completions = 0;
        
        for (AdAnalyticsEvent event : analyticsEvents) {
            if (event.getTimestamp() >= startTime && event.getTimestamp() < endTime && 
                event.getAdType() == AdType.INTERSTITIAL) {
                if (event.getEventType() == AdEventType.IMPRESSION) {
                    impressions++;
                } else if (event.getEventType() == AdEventType.COMPLETION) {
                    completions++;
                }
            }
        }
        
        return impressions > 0 ? (double) completions / impressions * 100.0 : 0.0;
    }
}
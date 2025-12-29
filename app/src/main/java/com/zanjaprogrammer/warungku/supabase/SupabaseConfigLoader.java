package com.zanjaprogrammer.warungku.supabase;

import android.content.Context;
import android.content.res.Resources;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class untuk load Supabase configuration dari properties file
 */
public class SupabaseConfigLoader {
    private static final String CONFIG_FILE = "supabase_config.properties";
    
    private static String supabaseUrl;
    private static String supabasePublishableKey;
    private static String supabaseSecretKey;
    
    /**
     * Load configuration dari properties file
     */
    public static void loadConfig(Context context) {
        try {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier(
                "supabase_config",
                "raw",
                context.getPackageName()
            );
            
            if (resourceId == 0) {
                throw new RuntimeException("supabase_config.properties not found in res/raw");
            }
            
            InputStream inputStream = resources.openRawResource(resourceId);
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            
            supabaseUrl = properties.getProperty("SUPABASE_URL");
            supabasePublishableKey = properties.getProperty("SUPABASE_PUBLISHABLE_KEY");
            supabaseSecretKey = properties.getProperty("SUPABASE_SECRET_KEY");
            
            if (supabaseUrl == null || supabasePublishableKey == null) {
                throw new RuntimeException("Missing required Supabase configuration");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Supabase configuration", e);
        }
    }
    
    public static String getSupabaseUrl() {
        if (supabaseUrl == null) {
            throw new IllegalStateException("Config not loaded. Call loadConfig() first.");
        }
        return supabaseUrl;
    }
    
    public static String getSupabasePublishableKey() {
        if (supabasePublishableKey == null) {
            throw new IllegalStateException("Config not loaded. Call loadConfig() first.");
        }
        return supabasePublishableKey;
    }
    
    public static String getSupabaseSecretKey() {
        if (supabaseSecretKey == null) {
            throw new IllegalStateException("Config not loaded. Call loadConfig() first.");
        }
        return supabaseSecretKey;
    }
}


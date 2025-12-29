package com.zanjaprogrammer.warungku.supabase;

import android.content.Context;

import com.zanjaprogrammer.warungku.supabase.api.SupabaseAuthApi;
import com.zanjaprogrammer.warungku.supabase.api.SupabasePostgrestApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Supabase client menggunakan Retrofit
 */
public class SupabaseClient {
    private static SupabaseClient instance;
    private static Retrofit retrofit;
    private static SupabaseAuthApi authApi;
    private static SupabasePostgrestApi postgrestApi;
    
    private String supabaseUrl;
    private String supabaseKey;
    
    private SupabaseClient(Context context) {
        // Load config
        SupabaseConfigLoader.loadConfig(context);
        supabaseUrl = SupabaseConfigLoader.getSupabaseUrl();
        supabaseKey = SupabaseConfigLoader.getSupabasePublishableKey();
        
        // Setup Retrofit
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .build();
        
        retrofit = new Retrofit.Builder()
            .baseUrl(supabaseUrl + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        
        authApi = retrofit.create(SupabaseAuthApi.class);
        postgrestApi = retrofit.create(SupabasePostgrestApi.class);
    }
    
    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }
    
    public static SupabaseClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SupabaseClient not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }
    
    public SupabaseAuthApi getAuthApi() {
        return authApi;
    }
    
    public SupabasePostgrestApi getPostgrestApi() {
        return postgrestApi;
    }
    
    public String getSupabaseUrl() {
        return supabaseUrl;
    }
    
    public String getSupabaseKey() {
        return supabaseKey;
    }
}


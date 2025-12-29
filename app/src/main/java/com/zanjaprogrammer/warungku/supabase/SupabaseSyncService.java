package com.zanjaprogrammer.warungku.supabase;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.supabase.api.SupabasePostgrestApi;
import com.zanjaprogrammer.warungku.utils.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Background service untuk sync data antara local Room database dan Supabase
 * Menggantikan FirestoreSyncService
 */
public class SupabaseSyncService extends Service {
    private static final String TAG = "SupabaseSyncService";
    
    private SupabaseClient supabaseClient;
    private SupabasePostgrestApi postgrestApi;
    private SupabaseAuthManager authManager;
    private DataRepository repository;
    private String warungId;
    private String accessToken;
    
    @Override
    public void onCreate() {
        super.onCreate();
        supabaseClient = SupabaseClient.getInstance(getApplication());
        postgrestApi = supabaseClient.getPostgrestApi();
        authManager = SupabaseAuthManager.getInstance(getApplication());
        repository = new DataRepository(getApplication());
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Load warungId and accessToken fresh from authManager
        warungId = null;
        accessToken = null;
        
        // First try: get from current state
        if (authManager.getCurrentWarung() != null) {
            warungId = authManager.getCurrentWarungId();
            accessToken = authManager.getAccessToken();
        }
        
        // Second try: get warungId from currentUser if currentWarung is null
        if (warungId == null && authManager.getCurrentUser() != null) {
            warungId = authManager.getCurrentUser().warungId;
            accessToken = authManager.getAccessToken();
        }
        
        // Third try: load from cache
        if (warungId == null || accessToken == null) {
            authManager.loadUserFromCache();
            // Try get from currentWarung first
            if (authManager.getCurrentWarung() != null) {
                warungId = authManager.getCurrentWarungId();
            } else if (authManager.getCurrentUser() != null) {
                // If currentWarung is null but currentUser exists, use warungId from user
                warungId = authManager.getCurrentUser().warungId;
            }
            accessToken = authManager.getAccessToken();
        }
        
        if (warungId == null || warungId.isEmpty()) {
            Log.w(TAG, "WarungId is null, cannot sync. User may not be logged in.");
            Log.w(TAG, "Current user: " + (authManager.getCurrentUser() != null ? authManager.getCurrentUser().email : "null"));
            Log.w(TAG, "Current user warungId: " + (authManager.getCurrentUser() != null ? authManager.getCurrentUser().warungId : "null"));
            Log.w(TAG, "Current warung: " + (authManager.getCurrentWarung() != null ? authManager.getCurrentWarung().warungId : "null"));
            stopSelf();
            return START_NOT_STICKY;
        }
        
        if (accessToken == null || accessToken.isEmpty()) {
            Log.w(TAG, "Access token is null, cannot sync");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.d(TAG, "Network not available, skipping sync");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        Log.d(TAG, "Starting sync for warung: " + warungId);
        
        // Run sync in background thread
        new Thread(() -> {
            try {
                syncProducts();
                syncCashFlows();
                Log.d(TAG, "Sync completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
            } finally {
                stopSelf();
            }
        }).start();
        
        return START_NOT_STICKY;
    }
    
    /**
     * Sync products: Local → Supabase
     */
    private void syncProducts() {
        if (warungId == null || accessToken == null) return;
        
        Log.d(TAG, "Starting products sync...");
        
        CountDownLatch latch = new CountDownLatch(1);
        final List<Product>[] unsyncedProducts = new List[1];
        
        // Get unsynced products from local database
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                unsyncedProducts[0] = repository.getUnsyncedProducts();
                
                if (unsyncedProducts[0].isEmpty()) {
                    Log.d(TAG, "No products to sync");
                    latch.countDown();
                    return;
                }
                
                Log.d(TAG, "Syncing " + unsyncedProducts[0].size() + " products");
                
                String authHeader = "Bearer " + accessToken;
                String apiKey = supabaseClient.getSupabaseKey();
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);
                
                // Sync each product
                for (Product product : unsyncedProducts[0]) {
                    // Use upsert: if cloud_id exists, update; otherwise insert
                    if (product.cloudId != null && !product.cloudId.isEmpty()) {
                        // Update existing product
                        Map<String, Object> productData = convertProductToMap(product, warungId, true);
                        // Update existing product
                        Call<List<Map<String, Object>>> updateCall = postgrestApi.updateProduct(
                            apiKey,
                            authHeader,
                            "return=representation",
                            "eq." + product.cloudId,
                            productData
                        );
                        
                        CountDownLatch productLatch = new CountDownLatch(1);
                        updateCall.enqueue(new Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    successCount.incrementAndGet();
                                    Log.d(TAG, "Product updated: " + product.name);
                                } else {
                                    failCount.incrementAndGet();
                                    Log.e(TAG, "Failed to update product: " + product.name + ", code: " + response.code());
                                }
                                productLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                failCount.incrementAndGet();
                                Log.e(TAG, "Error updating product: " + product.name, t);
                                productLatch.countDown();
                            }
                        });
                        
                        try {
                            productLatch.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while updating product", e);
                        }
                    } else {
                        // Insert new product
                        Map<String, Object> productData = convertProductToMap(product, warungId, false);
                        Call<List<Map<String, Object>>> insertCall = postgrestApi.insertProduct(
                            apiKey,
                            authHeader,
                            "return=representation",
                            productData
                        );
                        
                        CountDownLatch productLatch = new CountDownLatch(1);
                        insertCall.enqueue(new Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    // Get the inserted product ID
                                    Map<String, Object> insertedProduct = response.body().get(0);
                                    Object insertedId = insertedProduct.get("id");
                                    if (insertedId != null) {
                                        product.cloudId = insertedId.toString();
                                    }
                                    successCount.incrementAndGet();
                                    Log.d(TAG, "Product inserted: " + product.name);
                                } else {
                                    failCount.incrementAndGet();
                                    Log.e(TAG, "Failed to insert product: " + product.name + ", code: " + response.code());
                                }
                                productLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                failCount.incrementAndGet();
                                Log.e(TAG, "Error inserting product: " + product.name, t);
                                productLatch.countDown();
                            }
                        });
                        
                        try {
                            productLatch.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while inserting product", e);
                        }
                    }
                }
                
                // Update sync status in local database
                long currentTime = System.currentTimeMillis();
                for (Product product : unsyncedProducts[0]) {
                    product.synced = true;
                    product.lastSyncedAt = currentTime;
                    repository.updateProduct(product);
                }
                
                Log.d(TAG, "Products sync completed. Success: " + successCount.get() + ", Failed: " + failCount.get());
            } catch (Exception e) {
                Log.e(TAG, "Error syncing products", e);
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(60, TimeUnit.SECONDS); // Wait max 60 seconds
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for products sync", e);
        }
    }
    
    /**
     * Sync cash flows: Local → Supabase
     */
    private void syncCashFlows() {
        if (warungId == null || accessToken == null) return;
        
        Log.d(TAG, "Starting cash flows sync...");
        
        CountDownLatch latch = new CountDownLatch(1);
        final List<CashFlow>[] unsyncedCashFlows = new List[1];
        
        // Get unsynced cash flows from local database
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                unsyncedCashFlows[0] = repository.getUnsyncedCashFlows();
                
                if (unsyncedCashFlows[0].isEmpty()) {
                    Log.d(TAG, "No cash flows to sync");
                    latch.countDown();
                    return;
                }
                
                Log.d(TAG, "Syncing " + unsyncedCashFlows[0].size() + " cash flows");
                
                String authHeader = "Bearer " + accessToken;
                String apiKey = supabaseClient.getSupabaseKey();
                String userId = authManager.getCurrentUserId();
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failCount = new AtomicInteger(0);
                
                // Sync each cash flow
                for (CashFlow cashFlow : unsyncedCashFlows[0]) {
                    // Use upsert: if cloud_id exists, update; otherwise insert
                    if (cashFlow.cloudId != null && !cashFlow.cloudId.isEmpty()) {
                        // Update existing cash flow
                        Map<String, Object> cashFlowData = convertCashFlowToMap(cashFlow, warungId, userId, true);
                        Call<List<Map<String, Object>>> updateCall = postgrestApi.updateCashFlow(
                            apiKey,
                            authHeader,
                            "return=representation",
                            "eq." + cashFlow.cloudId,
                            cashFlowData
                        );
                        
                        CountDownLatch cashFlowLatch = new CountDownLatch(1);
                        updateCall.enqueue(new Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    successCount.incrementAndGet();
                                    Log.d(TAG, "Cash flow updated: " + cashFlow.description);
                                } else {
                                    failCount.incrementAndGet();
                                    Log.e(TAG, "Failed to update cash flow: " + cashFlow.description + ", code: " + response.code());
                                }
                                cashFlowLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                failCount.incrementAndGet();
                                Log.e(TAG, "Error updating cash flow: " + cashFlow.description, t);
                                cashFlowLatch.countDown();
                            }
                        });
                        
                        try {
                            cashFlowLatch.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while updating cash flow", e);
                        }
                    } else {
                        // Insert new cash flow
                        Map<String, Object> cashFlowData = convertCashFlowToMap(cashFlow, warungId, userId, false);
                        
                        Call<List<Map<String, Object>>> insertCall = postgrestApi.insertCashFlow(
                            apiKey,
                            authHeader,
                            "return=representation",
                            cashFlowData
                        );
                        
                        CountDownLatch cashFlowLatch = new CountDownLatch(1);
                        insertCall.enqueue(new Callback<List<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                    // Get the inserted cash flow ID
                                    Map<String, Object> insertedCashFlow = response.body().get(0);
                                    Object insertedId = insertedCashFlow.get("id");
                                    if (insertedId != null) {
                                        cashFlow.cloudId = insertedId.toString();
                                    }
                                    successCount.incrementAndGet();
                                    Log.d(TAG, "Cash flow inserted: " + cashFlow.description);
                                } else {
                                    failCount.incrementAndGet();
                                    Log.e(TAG, "Failed to insert cash flow: " + cashFlow.description + ", code: " + response.code());
                                }
                                cashFlowLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                                failCount.incrementAndGet();
                                Log.e(TAG, "Error inserting cash flow: " + cashFlow.description, t);
                                cashFlowLatch.countDown();
                            }
                        });
                        
                        try {
                            cashFlowLatch.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Interrupted while inserting cash flow", e);
                        }
                    }
                }
                
                // Update sync status in local database
                long currentTime = System.currentTimeMillis();
                for (CashFlow cashFlow : unsyncedCashFlows[0]) {
                    cashFlow.synced = true;
                    cashFlow.lastSyncedAt = currentTime;
                    repository.updateCashFlow(cashFlow);
                }
                
                Log.d(TAG, "Cash flows sync completed. Success: " + successCount.get() + ", Failed: " + failCount.get());
            } catch (Exception e) {
                Log.e(TAG, "Error syncing cash flows", e);
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(60, TimeUnit.SECONDS); // Wait max 60 seconds
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for cash flows sync", e);
        }
    }
    
    /**
     * Convert local Product entity to Supabase products table format (snake_case)
     * @param isUpdate true if this is an update operation, false for insert
     */
    private Map<String, Object> convertProductToMap(Product product, String warungId, boolean isUpdate) {
        Map<String, Object> map = new HashMap<>();
        
        // Only include id for update operations
        if (isUpdate && product.cloudId != null && !product.cloudId.isEmpty()) {
            try {
                map.put("id", Integer.parseInt(product.cloudId));
            } catch (NumberFormatException e) {
                // If cloudId is not a number, don't include it
                Log.w(TAG, "Invalid cloudId for product: " + product.cloudId);
            }
        }
        
        map.put("warung_id", warungId);
        map.put("name", product.name);
        map.put("sell_price", product.sellPrice);
        if (product.buyPrice != null) {
            map.put("buy_price", product.buyPrice);
        }
        map.put("current_stock", product.currentStock);
        map.put("min_stock", product.minStock);
        map.put("sales_count", product.salesCount);
        map.put("is_favorite", product.isFavorite);
        map.put("last_sold_timestamp", product.lastSoldTimestamp);
        if (product.barcode != null) {
            map.put("barcode", product.barcode);
        }
        // Don't send synced and last_synced_at to Supabase - these are local-only fields
        // Supabase doesn't have these columns in the schema
        
        return map;
    }
    
    /**
     * Convert local CashFlow entity to Supabase cash_flows table format (snake_case)
     * @param isUpdate true if this is an update operation, false for insert
     */
    private Map<String, Object> convertCashFlowToMap(CashFlow cashFlow, String warungId, String userId, boolean isUpdate) {
        Map<String, Object> map = new HashMap<>();
        
        // Only include id for update operations
        if (isUpdate && cashFlow.cloudId != null && !cashFlow.cloudId.isEmpty()) {
            try {
                map.put("id", Integer.parseInt(cashFlow.cloudId));
            } catch (NumberFormatException e) {
                // If cloudId is not a number, don't include it
                Log.w(TAG, "Invalid cloudId for cash flow: " + cashFlow.cloudId);
            }
        }
        
        map.put("warung_id", warungId);
        map.put("type", cashFlow.type);
        map.put("amount", cashFlow.amount);
        if (cashFlow.description != null) {
            map.put("description", cashFlow.description);
        }
        map.put("timestamp", cashFlow.timestamp);
        
        // Convert local product_id to cloud product_id
        if (cashFlow.productId != null) {
            // Get product from local database to find cloudId
            Product product = AppDatabase.getDatabase(getApplication())
                    .productDao()
                    .getProductById(cashFlow.productId);
            
            if (product != null && product.cloudId != null && !product.cloudId.isEmpty()) {
                try {
                    // Convert cloudId (String) to Integer for Supabase
                    Integer cloudProductId = Integer.parseInt(product.cloudId);
                    map.put("product_id", cloudProductId);
                    Log.d(TAG, "Mapped local product_id " + cashFlow.productId + " to cloud product_id " + cloudProductId);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid cloudId for product: " + product.cloudId + ", skipping product_id");
                }
            } else {
                Log.w(TAG, "Product not found or not synced yet for local product_id: " + cashFlow.productId + ", skipping product_id");
            }
        }
        
        if (cashFlow.profit != null) {
            map.put("profit", cashFlow.profit);
        }
        if (userId != null) {
            map.put("user_id", userId);
        }
        // Don't send synced and last_synced_at to Supabase - these are local-only fields
        // Supabase doesn't have these columns in the schema
        
        return map;
    }
}


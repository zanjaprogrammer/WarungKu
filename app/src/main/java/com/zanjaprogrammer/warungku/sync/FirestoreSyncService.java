package com.zanjaprogrammer.warungku.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.zanjaprogrammer.warungku.auth.AuthManager;
import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.utils.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Background service untuk sync data antara local Room database dan Firestore
 */
public class FirestoreSyncService extends Service {
    private static final String TAG = "FirestoreSyncService";
    
    private FirebaseFirestore firestore;
    private AuthManager authManager;
    private DataRepository repository;
    private String warungId;
    
    @Override
    public void onCreate() {
        super.onCreate();
        firestore = FirebaseFirestore.getInstance();
        authManager = AuthManager.getInstance(getApplication());
        repository = new DataRepository(getApplication());
        
        if (authManager.getCurrentWarung() != null) {
            warungId = authManager.getCurrentWarungId();
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (warungId == null) {
            Log.w(TAG, "WarungId is null, cannot sync");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Log.d(TAG, "Network not available, skipping sync");
            stopSelf();
            return START_NOT_STICKY;
        }
        
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
     * Sync products: Local → Cloud
     */
    private void syncProducts() {
        if (warungId == null) return;
        
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
                
                WriteBatch batch = firestore.batch();
                int batchCount = 0;
                final int BATCH_LIMIT = 500; // Firestore batch limit
                
                for (Product product : unsyncedProducts[0]) {
                    String productId = product.cloudId != null ? product.cloudId : String.valueOf(product.id);
                    // Use subcollection: products/{warungId}/items/{productId}
                    String documentPath = "products/" + warungId + "/items/" + productId;
                    
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("name", product.name);
                    productData.put("sellPrice", product.sellPrice);
                    productData.put("buyPrice", product.buyPrice);
                    productData.put("currentStock", product.currentStock);
                    productData.put("minStock", product.minStock);
                    productData.put("salesCount", product.salesCount);
                    productData.put("isFavorite", product.isFavorite);
                    productData.put("lastSoldTimestamp", product.lastSoldTimestamp);
                    productData.put("barcode", product.barcode);
                    productData.put("createdAt", System.currentTimeMillis());
                    productData.put("updatedAt", System.currentTimeMillis());
                    
                    batch.set(firestore.document(documentPath), productData);
                    batchCount++;
                    
                    // Commit batch if limit reached
                    if (batchCount >= BATCH_LIMIT) {
                        commitBatch(batch, "products");
                        batch = firestore.batch();
                        batchCount = 0;
                    }
                }
                
                // Commit remaining batch
                if (batchCount > 0) {
                    commitBatch(batch, "products");
                }
                
                // Update sync status in local database
                long currentTime = System.currentTimeMillis();
                for (Product product : unsyncedProducts[0]) {
                    product.synced = true;
                    product.lastSyncedAt = currentTime;
                    if (product.cloudId == null) {
                        product.cloudId = String.valueOf(product.id);
                    }
                    repository.updateProduct(product);
                }
                
                Log.d(TAG, "Products sync completed");
            } catch (Exception e) {
                Log.e(TAG, "Error syncing products", e);
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(30, TimeUnit.SECONDS); // Wait max 30 seconds
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for products sync", e);
        }
    }
    
    /**
     * Sync cash flows: Local → Cloud
     */
    private void syncCashFlows() {
        if (warungId == null) return;
        
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
                
                WriteBatch batch = firestore.batch();
                int batchCount = 0;
                final int BATCH_LIMIT = 500;
                
                for (CashFlow cashFlow : unsyncedCashFlows[0]) {
                    String cashFlowId = cashFlow.cloudId != null ? cashFlow.cloudId : String.valueOf(cashFlow.id);
                    // Use subcollection: cash_flows/{warungId}/transactions/{cashFlowId}
                    String documentPath = "cash_flows/" + warungId + "/transactions/" + cashFlowId;
                    
                    Map<String, Object> cashFlowData = new HashMap<>();
                    cashFlowData.put("type", cashFlow.type);
                    cashFlowData.put("amount", cashFlow.amount);
                    cashFlowData.put("description", cashFlow.description);
                    cashFlowData.put("timestamp", cashFlow.timestamp);
                    cashFlowData.put("productId", cashFlow.productId);
                    cashFlowData.put("profit", cashFlow.profit);
                    cashFlowData.put("userId", authManager.getCurrentUserId());
                    cashFlowData.put("createdAt", System.currentTimeMillis());
                    
                    batch.set(firestore.document(documentPath), cashFlowData);
                    batchCount++;
                    
                    // Commit batch if limit reached
                    if (batchCount >= BATCH_LIMIT) {
                        commitBatch(batch, "cash_flows");
                        batch = firestore.batch();
                        batchCount = 0;
                    }
                }
                
                // Commit remaining batch
                if (batchCount > 0) {
                    commitBatch(batch, "cash_flows");
                }
                
                // Update sync status in local database
                long currentTime = System.currentTimeMillis();
                for (CashFlow cashFlow : unsyncedCashFlows[0]) {
                    cashFlow.synced = true;
                    cashFlow.lastSyncedAt = currentTime;
                    if (cashFlow.cloudId == null) {
                        cashFlow.cloudId = String.valueOf(cashFlow.id);
                    }
                    repository.updateCashFlow(cashFlow);
                }
                
                Log.d(TAG, "Cash flows sync completed");
            } catch (Exception e) {
                Log.e(TAG, "Error syncing cash flows", e);
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(30, TimeUnit.SECONDS); // Wait max 30 seconds
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while waiting for cash flows sync", e);
        }
    }
    
    /**
     * Commit Firestore batch
     */
    private void commitBatch(WriteBatch batch, String collectionName) {
        try {
            java.util.concurrent.CountDownLatch commitLatch = new java.util.concurrent.CountDownLatch(1);
            batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch committed for " + collectionName);
                    commitLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error committing batch for " + collectionName, e);
                    commitLatch.countDown();
                });
            
            // Wait for commit to complete (max 10 seconds)
            commitLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error committing batch for " + collectionName, e);
        }
    }
}


package com.zanjaprogrammer.warungku.utils;

import android.app.Application;
import android.util.Log;
import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Fixed version for generating highly profitable dummy data
 */
public class DummyDataGeneratorFixed {
    private static final String TAG = "DummyDataGeneratorFixed";
    
    // Dummy product names
    private static final String[] PRODUCT_NAMES = {
        "Indomie Goreng", "Indomie Soto", "Indomie Rendang", "Indomie Ayam Bawang",
        "Pop Mie", "Mie Sedap", "Mie Sedaap Goreng", "Mie Sedaap Soto",
        "Teh Botol", "Teh Pucuk", "Teh Gelas", "Teh Kotak",
        "Aqua", "Le Minerale", "Vit", "Club",
        "Roti Tawar", "Roti Manis", "Kue Kering", "Biskuit",
        "Susu UHT", "Susu Kental Manis", "Susu Bubuk", "Yogurt",
        "Snack Ringan", "Kerupuk", "Kacang", "Permen",
        "Sabun Mandi", "Shampoo", "Pasta Gigi", "Sikat Gigi",
        "Minyak Goreng", "Gula Pasir", "Garam", "Kecap",
        "Sambal ABC", "Sambal Terasi", "Bumbu Masak", "Bawang Merah"
    };
    
    /**
     * Clear all existing data first
     */
    public static void clearAllData(Application application) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(application);
                db.productDao().deleteAll();
                db.cashFlowDao().deleteAll();
                Log.d(TAG, "All data cleared");
            } catch (Exception e) {
                Log.e(TAG, "Error clearing data", e);
            }
        });
    }
    
    /**
     * Generate profitable products
     */
    public static void generateProfitableProducts(Application application, int count) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                DataRepository repository = new DataRepository(application);
                Random random = new Random();
                
                for (int i = 0; i < count && i < PRODUCT_NAMES.length; i++) {
                    String name = PRODUCT_NAMES[i];
                    
                    // High profit margins (40-70% margin)
                    double buyPrice = 1000 + (random.nextInt(30) * 500); // 1000 - 15000
                    double profitMargin = 0.40 + (random.nextDouble() * 0.30); // 40-70% margin
                    double sellPrice = buyPrice * (1 + profitMargin);
                    
                    // High stock levels
                    int currentStock = random.nextInt(200) + 50; // 50-250
                    int minStock = 10;
                    
                    Product product = new Product(name, sellPrice, buyPrice, currentStock, minStock);
                    product.salesCount = 0;
                    product.isFavorite = random.nextBoolean();
                    product.lastSoldTimestamp = 0;
                    
                    repository.insertProduct(product);
                }
                
                Log.d(TAG, "Generated " + count + " profitable products");
            } catch (Exception e) {
                Log.e(TAG, "Error generating products", e);
            }
        });
    }
    
    /**
     * Generate super profitable year data with minimal expenses
     */
    public static void generateSuperProfitableYearData(Application application) {
        // Clear existing data first
        clearAllData(application);
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // Wait for clear
                
                // Generate products
                generateProfitableProducts(application, 35);
                
                Thread.sleep(2000); // Wait for products
                
                DataRepository repository = new DataRepository(application);
                AppDatabase db = AppDatabase.getDatabase(application);
                
                List<Product> products = db.productDao().getAllProductsSync();
                if (products == null || products.isEmpty()) {
                    Log.w(TAG, "No products found");
                    return;
                }
                
                Random random = new Random();
                Calendar cal = Calendar.getInstance();
                
                // Start from January 1st
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                long startOfYear = cal.getTimeInMillis();
                long currentTime = System.currentTimeMillis();
                int daysInYear = (int) ((currentTime - startOfYear) / (24 * 60 * 60 * 1000)) + 1;
                
                Log.d(TAG, "Generating data for " + daysInYear + " days");
                
                for (int day = 0; day < daysInYear; day++) {
                    cal.setTimeInMillis(startOfYear);
                    cal.add(Calendar.DAY_OF_YEAR, day);
                    
                    // Generate 10-25 sales per day (high volume)
                    int transactions = random.nextInt(16) + 10;
                    
                    for (int t = 0; t < transactions; t++) {
                        // Random time during business hours
                        int hour = random.nextInt(12) + 8; // 8 AM to 8 PM
                        int minute = random.nextInt(60);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        
                        long timestamp = cal.getTimeInMillis();
                        
                        // Random product and quantity
                        Product product = products.get(random.nextInt(products.size()));
                        int quantity = random.nextInt(8) + 1; // 1-8 items
                        
                        double amount = product.sellPrice * quantity;
                        double profit = (product.sellPrice - product.buyPrice) * quantity;
                        
                        CashFlow cashFlow = new CashFlow("IN", amount, 
                            "Jual: " + product.name + " (" + quantity + ")", 
                            timestamp, product.id, profit);
                        
                        repository.insertCashFlow(cashFlow);
                        
                        // Update product
                        product.currentStock = Math.max(0, product.currentStock - quantity);
                        product.salesCount += quantity;
                        product.lastSoldTimestamp = timestamp;
                        repository.updateProduct(product);
                    }
                    
                    // VERY MINIMAL expenses - only once every 10 days on average
                    if (random.nextDouble() < 0.1) { // Only 10% chance
                        cal.set(Calendar.HOUR_OF_DAY, 14); // 2 PM
                        
                        // Very small expense (5k-20k)
                        double expenseAmount = (random.nextInt(4) + 1) * 5000; // 5k-20k
                        
                        CashFlow expense = new CashFlow("OUT", expenseAmount, 
                            "Listrik", cal.getTimeInMillis(), null, 0.0);
                        
                        repository.insertCashFlow(expense);
                    }
                }
                
                Log.d(TAG, "Generated super profitable year data successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error generating super profitable data", e);
            }
        });
    }
}
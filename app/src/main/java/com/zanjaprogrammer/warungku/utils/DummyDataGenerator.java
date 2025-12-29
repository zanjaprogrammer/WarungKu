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
 * Utility class untuk generate dummy data untuk testing
 */
public class DummyDataGenerator {
    private static final String TAG = "DummyDataGenerator";
    
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
     * Generate dummy products
     */
    public static void generateProducts(Application application, int count) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                DataRepository repository = new DataRepository(application);
                Random random = new Random();
                List<String> usedNames = new ArrayList<>();
                
                for (int i = 0; i < count && i < PRODUCT_NAMES.length; i++) {
                    String name = PRODUCT_NAMES[i];
                    if (usedNames.contains(name)) {
                        name = name + " " + (i + 1);
                    }
                    usedNames.add(name);
                    
                    // Random prices with good profit margin (30-50% margin)
                    double buyPrice = 1000 + (random.nextInt(50) * 500); // 1000 - 25000
                    double profitMargin = 0.30 + (random.nextDouble() * 0.20); // 30-50% margin
                    double sellPrice = buyPrice * (1 + profitMargin); // Always profitable
                    
                    // Random stock
                    int currentStock = random.nextInt(100) + 10; // 10-110
                    int minStock = Math.max(5, currentStock / 4); // 25% dari current stock, min 5
                    
                    // Random barcode (optional)
                    String barcode = null;
                    if (random.nextBoolean()) {
                        barcode = String.valueOf(1000000000000L + random.nextInt(999999999));
                    }
                    
                    Product product = new Product(name, sellPrice, buyPrice, currentStock, minStock);
                    product.salesCount = 0;
                    product.isFavorite = random.nextBoolean();
                    product.lastSoldTimestamp = 0;
                    product.barcode = barcode;
                    
                    repository.insertProduct(product);
                }
                
                Log.d(TAG, "Generated " + count + " dummy products");
            } catch (Exception e) {
                Log.e(TAG, "Error generating products", e);
            }
        });
    }
    
    /**
     * Generate dummy cash flow (sales history)
     */
    public static void generateCashFlow(Application application, int daysBack, int transactionsPerDay) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                DataRepository repository = new DataRepository(application);
                AppDatabase db = AppDatabase.getDatabase(application);
                
                // Get all products
                List<Product> products = db.productDao().getAllProductsSync();
                if (products == null || products.isEmpty()) {
                    Log.w(TAG, "No products found. Generate products first.");
                    return;
                }
                
                Random random = new Random();
                Calendar cal = Calendar.getInstance();
                
                // Generate transactions for the last N days
                for (int day = 0; day < daysBack; day++) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.DAY_OF_YEAR, -day);
                    
                    // Track daily income to ensure expenses don't exceed it
                    double dailyIncome = 0;
                    
                    // Generate transactions for this day
                    int transactions = random.nextInt(transactionsPerDay) + 1; // 1 to transactionsPerDay
                    
                    for (int t = 0; t < transactions; t++) {
                        // Random time during the day
                        int hour = random.nextInt(14) + 8; // 8 AM to 9 PM
                        int minute = random.nextInt(60);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        
                        long timestamp = cal.getTimeInMillis();
                        
                        // Random product
                        Product product = products.get(random.nextInt(products.size()));
                        
                        // Random quantity
                        int quantity = random.nextInt(5) + 1; // 1-5
                        
                        // Calculate profit (always positive since sellPrice > buyPrice)
                        double buyPrice = product.buyPrice != null ? product.buyPrice : product.sellPrice * 0.7; // Fallback to 30% margin
                        double profit = (product.sellPrice - buyPrice) * quantity;
                        double amount = product.sellPrice * quantity;
                        
                        // Track daily income
                        dailyIncome += amount;
                        
                        // Create cash flow entry
                        CashFlow cashFlow = new CashFlow("IN", amount, 
                            "Jual: " + product.name + " (" + quantity + ")", 
                            timestamp, product.id, profit);
                        
                        repository.insertCashFlow(cashFlow);
                        
                        // Update product stock and sales count
                        product.currentStock = Math.max(0, product.currentStock - quantity);
                        product.salesCount += quantity;
                        product.lastSoldTimestamp = timestamp;
                        repository.updateProduct(product);
                    }
                    
                    // Add expenses, but ensure total expenses < 30% of daily income (to show profit)
                    // Only add expenses if there's enough income
                    if (dailyIncome > 0 && random.nextDouble() < 0.7) { // 70% chance of having expenses
                        double maxDailyExpense = dailyIncome * 0.30; // Max 30% of income as expenses
                        int expenseCount = random.nextInt(2) + 1; // 1-2 expenses per day (reduced)
                        
                        double totalExpenses = 0;
                        for (int e = 0; e < expenseCount && totalExpenses < maxDailyExpense; e++) {
                            cal.set(Calendar.HOUR_OF_DAY, random.nextInt(14) + 8);
                            cal.set(Calendar.MINUTE, random.nextInt(60));
                            
                            String[] expenseTypes = {
                                "Beli Stok", "Operasional", "Listrik", "Air", 
                                "Internet", "Transport", "Lainnya"
                            };
                            
                            String expenseType = expenseTypes[random.nextInt(expenseTypes.length)];
                            
                            // Calculate expense amount (smaller, proportional to income)
                            double remainingBudget = maxDailyExpense - totalExpenses;
                            double expenseAmount;
                            if (e == expenseCount - 1) {
                                // Last expense: use remaining budget
                                expenseAmount = Math.min(remainingBudget, dailyIncome * 0.15); // Max 15% per expense
                            } else {
                                // Other expenses: random but within budget
                                expenseAmount = Math.min(
                                    (random.nextInt(10) + 5) * 5000, // 25k - 75k (reduced from 50k-250k)
                                    remainingBudget / (expenseCount - e)
                                );
                            }
                            
                            // Ensure expense is reasonable (min 10k, max 15% of daily income)
                            expenseAmount = Math.max(10000, Math.min(expenseAmount, dailyIncome * 0.15));
                            
                            if (expenseAmount > 0 && totalExpenses + expenseAmount <= maxDailyExpense) {
                                totalExpenses += expenseAmount;
                                
                                CashFlow expense = new CashFlow("OUT", expenseAmount, 
                                    expenseType, cal.getTimeInMillis(), null, 0.0);
                                
                                repository.insertCashFlow(expense);
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Generated cash flow for " + daysBack + " days");
            } catch (Exception e) {
                Log.e(TAG, "Error generating cash flow", e);
            }
        });
    }
    
    /**
     * Generate all dummy data (products + cash flow)
     */
    public static void generateAllDummyData(Application application) {
        generateProducts(application, 30); // Generate 30 products
        // Wait a bit for products to be inserted, then generate cash flow
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second
                generateCashFlow(application, 30, 10); // Last 30 days, 1-10 transactions per day
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for products", e);
            }
        });
    }
    
    /**
     * Clear all data (for testing)
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
}


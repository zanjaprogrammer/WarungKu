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
     * Generate dummy products with better profit margins
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
                    
                    // Better profit margins (35-60% margin instead of 30-50%)
                    double buyPrice = 1000 + (random.nextInt(40) * 500); // 1000 - 20000 (slightly lower buy price)
                    double profitMargin = 0.35 + (random.nextDouble() * 0.25); // 35-60% margin (increased)
                    double sellPrice = buyPrice * (1 + profitMargin); // Always profitable with better margins
                    
                    // Higher stock levels for more sales potential
                    int currentStock = random.nextInt(150) + 20; // 20-170 (increased from 10-110)
                    int minStock = Math.max(5, currentStock / 5); // 20% dari current stock, min 5
                    
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
                
                Log.d(TAG, "Generated " + count + " profitable dummy products");
            } catch (Exception e) {
                Log.e(TAG, "Error generating products", e);
            }
        });
    }
    
    /**
     * Generate dummy cash flow (sales history) - Optimized for profitable business
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
                    
                    // Generate MORE transactions for better income (increased from original)
                    int transactions = random.nextInt(transactionsPerDay) + transactionsPerDay/2; // More consistent sales
                    
                    // Add weekend/weekday variation for realism
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        transactions = (int) (transactions * 1.3); // 30% more sales on weekends
                    }
                    
                    for (int t = 0; t < transactions; t++) {
                        // Random time during the day (extended hours for more sales)
                        int hour = random.nextInt(16) + 7; // 7 AM to 10 PM (extended)
                        int minute = random.nextInt(60);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        
                        long timestamp = cal.getTimeInMillis();
                        
                        // Random product (favor popular items)
                        Product product = products.get(random.nextInt(products.size()));
                        
                        // Increased quantity for higher income
                        int quantity = random.nextInt(8) + 1; // 1-8 (increased from 1-5)
                        
                        // Calculate profit (always positive since sellPrice > buyPrice)
                        double buyPrice = product.buyPrice != null ? product.buyPrice : product.sellPrice * 0.65; // Better margin (35% instead of 30%)
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
                    
                    // REDUCED expenses to ensure much higher profit margins
                    // Only add expenses if there's significant income
                    if (dailyIncome > 50000 && random.nextDouble() < 0.5) { // Only 50% chance of expenses (reduced from 70%)
                        double maxDailyExpense = dailyIncome * 0.15; // Max 15% of income as expenses (reduced from 30%)
                        int expenseCount = random.nextInt(2) + 1; // 1-2 expenses per day
                        
                        double totalExpenses = 0;
                        for (int e = 0; e < expenseCount && totalExpenses < maxDailyExpense; e++) {
                            cal.set(Calendar.HOUR_OF_DAY, random.nextInt(14) + 8);
                            cal.set(Calendar.MINUTE, random.nextInt(60));
                            
                            String[] expenseTypes = {
                                "Beli Stok", "Operasional", "Listrik", "Air", 
                                "Internet", "Transport", "Lainnya"
                            };
                            
                            String expenseType = expenseTypes[random.nextInt(expenseTypes.length)];
                            
                            // Much smaller expense amounts
                            double remainingBudget = maxDailyExpense - totalExpenses;
                            double expenseAmount;
                            if (e == expenseCount - 1) {
                                // Last expense: use remaining budget
                                expenseAmount = Math.min(remainingBudget, dailyIncome * 0.08); // Max 8% per expense (reduced)
                            } else {
                                // Other expenses: much smaller amounts
                                expenseAmount = Math.min(
                                    (random.nextInt(6) + 2) * 5000, // 10k - 35k (much reduced from 25k-75k)
                                    remainingBudget / (expenseCount - e)
                                );
                            }
                            
                            // Ensure expense is reasonable (min 5k, max 8% of daily income)
                            expenseAmount = Math.max(5000, Math.min(expenseAmount, dailyIncome * 0.08));
                            
                            if (expenseAmount > 0 && totalExpenses + expenseAmount <= maxDailyExpense) {
                                totalExpenses += expenseAmount;
                                
                                CashFlow expense = new CashFlow("OUT", expenseAmount, 
                                    expenseType, cal.getTimeInMillis(), null, 0.0);
                                
                                repository.insertCashFlow(expense);
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Generated profitable cash flow for " + daysBack + " days");
            } catch (Exception e) {
                Log.e(TAG, "Error generating cash flow", e);
            }
        });
    }
    
    /**
     * Generate all dummy data (products + cash flow) - Optimized for profitable year
     */
    public static void generateAllDummyData(Application application) {
        generateProducts(application, 30); // Generate 30 products with better margins
        // Wait a bit for products to be inserted, then generate cash flow
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second
                // Generate data for full year with higher transaction volume
                generateCashFlow(application, 365, 15); // Full year, 5-15 transactions per day (increased)
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for products", e);
            }
        });
    }
    
    /**
     * Generate highly profitable dummy data for current year specifically
     * This ensures the yearly report shows strong profitability
     */
    public static void generateProfitableYearData(Application application) {
        generateProducts(application, 35); // More products for variety
        
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Thread.sleep(1500); // Wait for products to be inserted
                
                DataRepository repository = new DataRepository(application);
                AppDatabase db = AppDatabase.getDatabase(application);
                
                List<Product> products = db.productDao().getAllProductsSync();
                if (products == null || products.isEmpty()) {
                    Log.w(TAG, "No products found for profitable year data generation.");
                    return;
                }
                
                Random random = new Random();
                Calendar cal = Calendar.getInstance();
                
                // Start from January 1st of current year
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                long startOfYear = cal.getTimeInMillis();
                long currentTime = System.currentTimeMillis();
                
                // Calculate days from start of year to now
                int daysInYear = (int) ((currentTime - startOfYear) / (24 * 60 * 60 * 1000)) + 1;
                
                for (int day = 0; day < daysInYear; day++) {
                    cal.setTimeInMillis(startOfYear);
                    cal.add(Calendar.DAY_OF_YEAR, day);
                    
                    double dailyIncome = 0;
                    
                    // Seasonal variations for realism
                    int month = cal.get(Calendar.MONTH);
                    double seasonalMultiplier = 1.0;
                    
                    // Higher sales during certain months
                    if (month == Calendar.DECEMBER || month == Calendar.JANUARY) {
                        seasonalMultiplier = 1.4; // Holiday season
                    } else if (month == Calendar.JUNE || month == Calendar.JULY) {
                        seasonalMultiplier = 1.2; // Mid-year boost
                    } else if (month == Calendar.APRIL || month == Calendar.MAY) {
                        seasonalMultiplier = 1.1; // Spring increase
                    }
                    
                    // Weekend boost
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        seasonalMultiplier *= 1.3;
                    }
                    
                    // Generate 8-20 transactions per day (high volume)
                    int baseTransactions = random.nextInt(13) + 8;
                    int transactions = (int) (baseTransactions * seasonalMultiplier);
                    
                    for (int t = 0; t < transactions; t++) {
                        // Business hours: 7 AM to 10 PM
                        int hour = random.nextInt(15) + 7;
                        int minute = random.nextInt(60);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        
                        long timestamp = cal.getTimeInMillis();
                        
                        // Select product (favor popular items)
                        Product product = products.get(random.nextInt(products.size()));
                        
                        // Higher quantities for better income
                        int quantity = random.nextInt(10) + 1; // 1-10 items
                        
                        double buyPrice = product.buyPrice != null ? product.buyPrice : product.sellPrice * 0.6; // 40% margin minimum
                        double profit = (product.sellPrice - buyPrice) * quantity;
                        double amount = product.sellPrice * quantity;
                        
                        dailyIncome += amount;
                        
                        CashFlow cashFlow = new CashFlow("IN", amount, 
                            "Jual: " + product.name + " (" + quantity + ")", 
                            timestamp, product.id, profit);
                        
                        repository.insertCashFlow(cashFlow);
                        
                        // Update product metrics
                        product.currentStock = Math.max(0, product.currentStock - quantity);
                        product.salesCount += quantity;
                        product.lastSoldTimestamp = timestamp;
                        repository.updateProduct(product);
                    }
                    
                    // Very minimal and controlled expenses to ensure profit
                    if (dailyIncome > 200000 && random.nextDouble() < 0.05) { // Only 5% chance of expenses, higher income threshold
                        // Much smaller expense amounts - max 5% of daily income
                        double maxExpense = dailyIncome * 0.05; // Max 5% of income (very conservative)
                        double expenseAmount = Math.min(
                            (random.nextInt(3) + 1) * 5000, // 5k-15k (much smaller amounts)
                            maxExpense
                        );
                        
                        // Ensure expense never exceeds 3% of daily income
                        expenseAmount = Math.min(expenseAmount, dailyIncome * 0.03);
                        
                        String[] expenseTypes = {"Listrik", "Internet", "Transport"};
                        String expenseType = expenseTypes[random.nextInt(expenseTypes.length)];
                        
                        cal.set(Calendar.HOUR_OF_DAY, random.nextInt(8) + 10); // 10 AM - 6 PM
                        
                        CashFlow expense = new CashFlow("OUT", expenseAmount, 
                            expenseType, cal.getTimeInMillis(), null, 0.0);
                        
                        repository.insertCashFlow(expense);
                    }
                }
                
                Log.d(TAG, "Generated highly profitable year data for " + daysInYear + " days");
            } catch (Exception e) {
                Log.e(TAG, "Error generating profitable year data", e);
            }
        });
    }
    
    /**
     * Simple method to generate clean test data for trend chart
     * 1M profit, 300K expenses across a week with natural randomness
     */
    public static void generateSimpleTestData(Application application) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Clear all existing data first
                AppDatabase db = AppDatabase.getDatabase(application);
                db.productDao().deleteAll();
                db.cashFlowDao().deleteAll();
                
                Thread.sleep(500); // Wait for clear
                
                DataRepository repository = new DataRepository(application);
                Calendar cal = Calendar.getInstance();
                Random random = new Random();
                
                // Start from 7 days ago
                cal.add(Calendar.DAY_OF_YEAR, -7);
                cal.set(Calendar.HOUR_OF_DAY, 8);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                
                // Target totals
                double totalIncomeTarget = 1000000.0;
                double totalExpenseTarget = 300000.0;
                
                // Generate random daily distributions that add up to targets
                double[] dailyIncomes = new double[7];
                double[] dailyExpenses = new double[7];
                
                // Generate random income distribution (70K - 200K per day)
                double remainingIncome = totalIncomeTarget;
                for (int i = 0; i < 6; i++) {
                    double minIncome = 70000;
                    double maxIncome = Math.min(200000, remainingIncome - (6-i) * 70000);
                    dailyIncomes[i] = minIncome + random.nextDouble() * (maxIncome - minIncome);
                    remainingIncome -= dailyIncomes[i];
                }
                dailyIncomes[6] = remainingIncome; // Last day gets remainder
                
                // Generate random expense distribution (20K - 80K per day)
                double remainingExpense = totalExpenseTarget;
                for (int i = 0; i < 6; i++) {
                    double minExpense = 20000;
                    double maxExpense = Math.min(80000, remainingExpense - (6-i) * 20000);
                    dailyExpenses[i] = minExpense + random.nextDouble() * (maxExpense - minExpense);
                    remainingExpense -= dailyExpenses[i];
                }
                dailyExpenses[6] = remainingExpense; // Last day gets remainder
                
                // Generate transactions for each day
                for (int day = 0; day < 7; day++) {
                    // Multiple income transactions per day (2-5 transactions)
                    int incomeTransactions = random.nextInt(4) + 2;
                    double remainingDailyIncome = dailyIncomes[day];
                    
                    for (int t = 0; t < incomeTransactions; t++) {
                        double amount;
                        if (t == incomeTransactions - 1) {
                            amount = remainingDailyIncome; // Last transaction gets remainder
                        } else {
                            amount = remainingDailyIncome * (0.1 + random.nextDouble() * 0.4); // 10-50% of remaining
                            remainingDailyIncome -= amount;
                        }
                        
                        // Random time during business hours
                        int hour = random.nextInt(12) + 8; // 8 AM to 8 PM
                        int minute = random.nextInt(60);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        
                        CashFlow income = new CashFlow("IN", amount, 
                            "Penjualan #" + (t + 1), 
                            cal.getTimeInMillis(), null, amount * 0.3);
                        
                        repository.insertCashFlow(income);
                    }
                    
                    // Random expense transactions (0-3 per day, some days might have no expenses)
                    if (dailyExpenses[day] > 1000 && random.nextDouble() < 0.8) { // 80% chance of having expenses
                        int expenseTransactions = random.nextInt(3) + 1;
                        double remainingDailyExpense = dailyExpenses[day];
                        
                        for (int t = 0; t < expenseTransactions; t++) {
                            double amount;
                            if (t == expenseTransactions - 1) {
                                amount = remainingDailyExpense;
                            } else {
                                amount = remainingDailyExpense * (0.2 + random.nextDouble() * 0.6); // 20-80% of remaining
                                remainingDailyExpense -= amount;
                            }
                            
                            // Random time during day
                            int hour = random.nextInt(10) + 9; // 9 AM to 7 PM
                            int minute = random.nextInt(60);
                            cal.set(Calendar.HOUR_OF_DAY, hour);
                            cal.set(Calendar.MINUTE, minute);
                            
                            String[] expenseTypes = {"Operasional", "Listrik", "Internet", "Transport", "Supplies"};
                            String expenseType = expenseTypes[random.nextInt(expenseTypes.length)];
                            
                            CashFlow expense = new CashFlow("OUT", amount, 
                                expenseType, 
                                cal.getTimeInMillis(), null, 0.0);
                            
                            repository.insertCashFlow(expense);
                        }
                    }
                    
                    // Move to next day
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 8);
                }
                
                Log.d(TAG, "Generated natural test data: 1M income, 300K expenses over 7 days with randomness");
            } catch (Exception e) {
                Log.e(TAG, "Error generating natural test data", e);
            }
        });
    }
}


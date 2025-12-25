package com.zanjaprogrammer.warungku.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.dao.ProductDao;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.Product;

import java.util.List;

public class DataRepository {
    private final ProductDao productDao;
    private final CashFlowDao cashFlowDao;
    private final LiveData<List<Product>> allProducts;
    private final LiveData<List<CashFlow>> allHistory;

    public DataRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        cashFlowDao = db.cashFlowDao();
        allProducts = productDao.getAllProducts();
        allHistory = cashFlowDao.getAllHistory();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<CashFlow>> getAllHistory() {
        return allHistory;
    }

    public LiveData<Double> getCurrentBalance() {
        return cashFlowDao.getCurrentBalance();
    }

    public LiveData<Double> getTotalIncome() {
        return cashFlowDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpense() {
        return cashFlowDao.getTotalExpense();
    }

    public LiveData<Double> getIncomeInRange(long start, long end) {
        return cashFlowDao.getIncomeInRange(start, end);
    }

    public LiveData<Double> getExpenseInRange(long start, long end) {
        return cashFlowDao.getExpenseInRange(start, end);
    }

    public LiveData<Double> getProfitInRange(long start, long end) {
        return cashFlowDao.getProfitInRange(start, end);
    }

    public LiveData<List<Product>> getShoppingList() {
        return productDao.getShoppingList();
    }

    public void insertProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insert(product));
    }

    public void updateProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }

    public void insertCashFlow(CashFlow cashFlow) {
        AppDatabase.databaseWriteExecutor.execute(() -> cashFlowDao.insert(cashFlow));
    }

    public void sellProduct(Product product, int quantity) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            product.currentStock -= quantity;
            product.salesCount += quantity;
            productDao.update(product);

            double amount = product.sellPrice * quantity;
            Double profit = (product.buyPrice != null) ? (product.sellPrice - product.buyPrice) * quantity : 0.0;

            CashFlow flow = new CashFlow("IN", amount, "Jual " + product.name + " (" + quantity + ")",
                    System.currentTimeMillis(), product.id, profit);
            cashFlowDao.insert(flow);
        });
    }

    public void addProductStock(Product product, int quantity) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            product.currentStock += quantity;
            productDao.update(product);

            double cost = (product.buyPrice != null ? product.buyPrice : 0.0) * quantity;
            if (cost > 0) {
                CashFlow flow = new CashFlow("OUT", cost, "Tambah Stok: " + product.name + " (" + quantity + ")",
                        System.currentTimeMillis(), product.id, 0.0);
                cashFlowDao.insert(flow);
            }
        });
    }

    public void adjustProductStock(Product product, int newStock) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            product.currentStock = newStock;
            productDao.update(product);
        });
    }

    public void checkout(List<com.zanjaprogrammer.warungku.data.model.CartItem> items) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            StringBuilder desc = new StringBuilder("Jual: ");
            double totalAmount = 0;
            double totalProfit = 0;

            for (com.zanjaprogrammer.warungku.data.model.CartItem item : items) {
                Product product = item.product;
                int quantity = item.quantity;

                // Update stock
                product.currentStock -= quantity;
                product.salesCount += quantity;
                productDao.update(product);

                // Accumulate totals
                totalAmount += product.sellPrice * quantity;
                double profit = (product.buyPrice != null) ? (product.sellPrice - product.buyPrice) * quantity : 0.0;
                totalProfit += profit;

                desc.append(product.name).append(" (").append(quantity).append("), ");
            }

            // Remove trailing comma
            String finalDesc = desc.toString();
            if (finalDesc.endsWith(", ")) {
                finalDesc = finalDesc.substring(0, finalDesc.length() - 2);
            }

            CashFlow flow = new CashFlow("IN", totalAmount, finalDesc,
                    System.currentTimeMillis(), null, totalProfit);
            cashFlowDao.insert(flow);
        });
    }
}

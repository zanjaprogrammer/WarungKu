package com.zanjaprogrammer.warungku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.appbar.MaterialToolbar;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;
import android.content.Intent;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private TextView tvNetProfit, tvInitialCapital, tvTotalIncome, tvTotalExpense, tvRoiPercentage, tvTotalStockPurchase, tvProgressPercentage;
    private TextView tvCartCount, tvCartTotal;
    private MaterialCardView cardNoCapitalWarning, cardBreakEvenAnnouncement, cardCartSummary;
    private ProgressBar progressBarCapitalReturn;
    private DataRepository repository;
    private AppViewModel viewModel;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "WarungKuPrefs";
    private static final String KEY_CAPITAL = "initial_capital";

    private long currentStart, currentEnd;
    private LiveData<Double> incomeLive, expenseLive, profitLive, stockPurchaseLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WarungKu", "SummaryActivity onCreate started");
        setContentView(R.layout.activity_summary);

        repository = new DataRepository(getApplication());
        viewModel = AppViewModel.getInstance(getApplication());
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // Untuk testing: uncomment baris di bawah untuk auto-generate test data saat pertama kali buka
        // generateTestData();

        initViews();
        setupFilters();
        loadInitialCapital();
        setupCartObserver();

        // Default filter: Today
        updateTimeRange(RangeType.DAY);
    }

    private void initViews() {
        tvNetProfit = findViewById(R.id.tvNetProfit);
        tvInitialCapital = findViewById(R.id.tvInitialCapital);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvRoiPercentage = findViewById(R.id.tvRoiPercentage);
        tvTotalStockPurchase = findViewById(R.id.tvTotalStockPurchase);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        cardNoCapitalWarning = findViewById(R.id.cardNoCapitalWarning);
        cardBreakEvenAnnouncement = findViewById(R.id.cardBreakEvenAnnouncement);
        cardCartSummary = findViewById(R.id.cardCartSummary);
        progressBarCapitalReturn = findViewById(R.id.progressBarCapitalReturn);
        tvCartCount = findViewById(R.id.tvCartCount);
        tvCartTotal = findViewById(R.id.tvCartTotal);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_generate_test_data) {
                generateTestData();
                return true;
            }
            return false;
        });
        // Toolbar back button removed per user request

        findViewById(R.id.btnSetCapital).setOnClickListener(v -> showCapitalDialog());
        findViewById(R.id.btnSetCapitalFromWarning).setOnClickListener(v -> showCapitalDialog());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            viewModel.checkout();
            Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show();
        });

        updateCapitalWarningVisibility();
        setupBottomNavigation();
    }
    
    private void setupCartObserver() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));
        
        viewModel.getCartItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                cardCartSummary.setVisibility(View.VISIBLE);
                // Hitung total quantity (bukan jumlah tipe produk)
                int totalQty = 0;
                for (com.zanjaprogrammer.warungku.data.model.CartItem item : items) {
                    totalQty += item.quantity;
                }
                tvCartCount.setText(totalQty + " Barang");
            } else {
                cardCartSummary.setVisibility(View.GONE);
            }
        });

        viewModel.getCartTotal().observe(this, total -> {
            tvCartTotal.setText(formatter.format(total != null ? total : 0));
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_summary);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new Intent(this, SellActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_stock) {
                startActivity(new Intent(this, StockActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_money) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return id == R.id.nav_summary;
        });
    }

    private void setupFilters() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;
            int id = checkedIds.get(0);
            if (id == R.id.chipDay)
                updateTimeRange(RangeType.DAY);
            else if (id == R.id.chipWeek)
                updateTimeRange(RangeType.WEEK);
            else if (id == R.id.chipMonth)
                updateTimeRange(RangeType.MONTH);
            else if (id == R.id.chipYear)
                updateTimeRange(RangeType.YEAR);
        });
    }

    private enum RangeType {
        DAY, WEEK, MONTH, YEAR
    }

    private void updateTimeRange(RangeType type) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        currentEnd = System.currentTimeMillis();

        switch (type) {
            case DAY:
                currentStart = cal.getTimeInMillis();
                break;
            case WEEK:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                currentStart = cal.getTimeInMillis();
                break;
            case MONTH:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                currentStart = cal.getTimeInMillis();
                break;
            case YEAR:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                currentStart = cal.getTimeInMillis();
                break;
        }

        observeData();
    }

    private void observeData() {
        // Remove old observers if any
        if (incomeLive != null) {
            incomeLive.removeObservers(this);
            expenseLive.removeObservers(this);
            profitLive.removeObservers(this);
            if (stockPurchaseLive != null) {
                stockPurchaseLive.removeObservers(this);
            }
        }

        incomeLive = repository.getIncomeInRange(currentStart, currentEnd);
        expenseLive = repository.getExpenseInRange(currentStart, currentEnd);
        profitLive = repository.getProfitInRange(currentStart, currentEnd);
        stockPurchaseLive = repository.getTotalStockPurchaseInRange(currentStart, currentEnd);

        incomeLive.observe(this, this::updateCalculations);
        expenseLive.observe(this, this::updateCalculations);
        profitLive.observe(this, this::updateCalculations);
        stockPurchaseLive.observe(this, this::updateCalculations);
    }

    private void updateCalculations(Double dummy) {
        double income = incomeLive.getValue() != null ? incomeLive.getValue() : 0.0;
        double expense = expenseLive.getValue() != null ? expenseLive.getValue() : 0.0;
        double profit = profitLive.getValue() != null ? profitLive.getValue() : 0.0;
        double stockPurchase = stockPurchaseLive != null && stockPurchaseLive.getValue() != null ? stockPurchaseLive.getValue() : 0.0;
        double capital = getCapital();

        double netProfit = income - expense;

        tvTotalIncome.setText(formatCurrency(income));
        tvTotalExpense.setText(formatCurrency(expense));
        tvNetProfit.setText(formatCurrency(netProfit));
        tvTotalStockPurchase.setText(formatCurrency(stockPurchase));

        // Update perkembangan modal (total uang, bukan persentase)
        tvRoiPercentage.setText(formatCurrency(netProfit));

        // Update progress bar pengembalian modal
        if (capital > 0) {
            double progressPercent = (netProfit / capital) * 100;
            // Batasi maksimal 100%
            if (progressPercent > 100) {
                progressPercent = 100;
            }
            if (progressPercent < 0) {
                progressPercent = 0;
            }
            
            int progressInt = (int) progressPercent;
            progressBarCapitalReturn.setProgress(progressInt);
            tvProgressPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", progressPercent));
            
            // Tampilkan announcement jika sudah balik modal (>= 100%)
            if (progressPercent >= 100 && netProfit > 0) {
                cardBreakEvenAnnouncement.setVisibility(View.VISIBLE);
            } else {
                cardBreakEvenAnnouncement.setVisibility(View.GONE);
            }
        } else {
            progressBarCapitalReturn.setProgress(0);
            tvProgressPercentage.setText("0%");
            cardBreakEvenAnnouncement.setVisibility(View.GONE);
        }
    }

    private void loadInitialCapital() {
        tvInitialCapital.setText(formatCurrency(getCapital()));
        updateCapitalWarningVisibility();
    }

    private void updateCapitalWarningVisibility() {
        if (cardNoCapitalWarning != null) {
            double capital = getCapital();
            if (capital <= 0) {
                cardNoCapitalWarning.setVisibility(View.VISIBLE);
            } else {
                cardNoCapitalWarning.setVisibility(View.GONE);
            }
        }
    }

    private double getCapital() {
        return (double) prefs.getFloat(KEY_CAPITAL, 0f);
    }

    private void showCapitalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Modal Awal");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf((int) getCapital()));
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            try {
                float capital = Float.parseFloat(input.getText().toString());
                prefs.edit().putFloat(KEY_CAPITAL, capital).apply();
                loadInitialCapital();
                updateCapitalWarningVisibility();
                updateCalculations(0.0);
            } catch (Exception e) {
                Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return format.format(amount);
    }
    
    /**
     * Generate test data dengan timestamp berbeda untuk testing filter waktu
     * Hanya untuk development/testing
     */
    private void generateTestData() {
        Calendar cal = Calendar.getInstance();
        long now = System.currentTimeMillis();
        
        // Clear existing test data (optional - bisa di-comment jika ingin keep data)
        // repository.deleteTestData(); // Jika ada method ini
        
        // Data untuk hari ini
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 30);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 50000, "Test: Jual Hari Ini", cal.getTimeInMillis(), null, 10000.0));
        
        // Data untuk 2 hari lalu
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -2);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 30000, "Test: Jual 2 Hari Lalu", cal.getTimeInMillis(), null, 5000.0));
        
        // Data untuk 1 minggu lalu (7 hari)
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 40000, "Test: Jual 1 Minggu Lalu", cal.getTimeInMillis(), null, 8000.0));
        
        // Data untuk 2 minggu lalu (14 hari)
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -14);
        cal.set(Calendar.HOUR_OF_DAY, 11);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 35000, "Test: Jual 2 Minggu Lalu", cal.getTimeInMillis(), null, 7000.0));
        
        // Data untuk 1 bulan lalu (30 hari)
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -30);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 60000, "Test: Jual 1 Bulan Lalu", cal.getTimeInMillis(), null, 12000.0));
        
        // Data untuk 2 bulan lalu (60 hari)
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -60);
        cal.set(Calendar.HOUR_OF_DAY, 13);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 45000, "Test: Jual 2 Bulan Lalu", cal.getTimeInMillis(), null, 9000.0));
        
        // Data untuk 1 tahun lalu (365 hari)
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_YEAR, -365);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "IN", 70000, "Test: Jual 1 Tahun Lalu", cal.getTimeInMillis(), null, 14000.0));
        
        // Data pengeluaran untuk hari ini
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "OUT", 20000, "Test: Pengeluaran Hari Ini", cal.getTimeInMillis(), null, 0.0));
        
        // Data pengeluaran untuk 1 minggu lalu
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, -7);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        repository.insertCashFlow(new com.zanjaprogrammer.warungku.data.entity.CashFlow(
            "OUT", 15000, "Test: Pengeluaran 1 Minggu Lalu", cal.getTimeInMillis(), null, 0.0));
        
        Toast.makeText(this, "Test data berhasil dibuat!", Toast.LENGTH_LONG).show();
        
        // Refresh data
        observeData();
    }
}

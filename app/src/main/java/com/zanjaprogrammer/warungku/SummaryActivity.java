package com.zanjaprogrammer.warungku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.appbar.MaterialToolbar;
import com.zanjaprogrammer.warungku.data.DataRepository;
import android.content.Intent;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class SummaryActivity extends AppCompatActivity {

    private TextView tvNetProfit, tvInitialCapital, tvTotalIncome, tvTotalExpense, tvRoiPercentage;
    private DataRepository repository;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "WarungKuPrefs";
    private static final String KEY_CAPITAL = "initial_capital";

    private long currentStart, currentEnd;
    private LiveData<Double> incomeLive, expenseLive, profitLive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("WarungKu", "SummaryActivity onCreate started");
        setContentView(R.layout.activity_summary);

        repository = new DataRepository(getApplication());
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        setupFilters();
        loadInitialCapital();

        // Default filter: Today
        updateTimeRange(RangeType.DAY);
    }

    private void initViews() {
        tvNetProfit = findViewById(R.id.tvNetProfit);
        tvInitialCapital = findViewById(R.id.tvInitialCapital);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvRoiPercentage = findViewById(R.id.tvRoiPercentage);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // Toolbar back button removed per user request

        findViewById(R.id.btnSetCapital).setOnClickListener(v -> showCapitalDialog());

        setupBottomNavigation();
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
        }

        incomeLive = repository.getIncomeInRange(currentStart, currentEnd);
        expenseLive = repository.getExpenseInRange(currentStart, currentEnd);
        profitLive = repository.getProfitInRange(currentStart, currentEnd);

        incomeLive.observe(this, this::updateCalculations);
        expenseLive.observe(this, this::updateCalculations);
        profitLive.observe(this, this::updateCalculations);
    }

    private void updateCalculations(Double dummy) {
        double income = incomeLive.getValue() != null ? incomeLive.getValue() : 0.0;
        double expense = expenseLive.getValue() != null ? expenseLive.getValue() : 0.0;
        double profit = profitLive.getValue() != null ? profitLive.getValue() : 0.0;
        double capital = getCapital();

        double netProfit = income - expense;

        tvTotalIncome.setText(formatCurrency(income));
        tvTotalExpense.setText(formatCurrency(expense));
        tvNetProfit.setText(formatCurrency(netProfit));

        if (capital > 0) {
            double roi = (netProfit / capital) * 100;
            tvRoiPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", roi));
        } else {
            tvRoiPercentage.setText("-%");
        }
    }

    private void loadInitialCapital() {
        tvInitialCapital.setText(formatCurrency(getCapital()));
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
}

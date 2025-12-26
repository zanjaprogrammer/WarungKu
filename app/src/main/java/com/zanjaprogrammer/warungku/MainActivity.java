package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.zanjaprogrammer.warungku.databinding.ActivityMainBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppViewModel viewModel;
    private final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Use singleton instance untuk persist cart across activities
        viewModel = AppViewModel.getInstance(getApplication());

        observeData();
        setupListeners();
    }

    private void observeData() {
        viewModel.getIncome().observe(this, income -> {
            binding.tvIncome.setText(formatter.format(income != null ? income : 0));
        });

        viewModel.getExpense().observe(this, expense -> {
            binding.tvExpense.setText(formatter.format(expense != null ? expense : 0));
        });

        viewModel.getBalance().observe(this, balance -> {
            binding.tvBalance.setText(formatter.format(balance != null ? balance : 0));
        });

        viewModel.getShoppingList().observe(this, list -> {
            binding.tvLowStock.setText(String.valueOf(list != null ? list.size() : 0));
        });

        // Observe Cart (Home)
        viewModel.getCartItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                binding.cardCartSummary.setVisibility(android.view.View.VISIBLE);
                // Hitung total quantity (bukan jumlah tipe produk)
                int totalQty = 0;
                for (com.zanjaprogrammer.warungku.data.model.CartItem item : items) {
                    totalQty += item.quantity;
                }
                binding.tvCartCount.setText(totalQty + " Barang");
            } else {
                binding.cardCartSummary.setVisibility(android.view.View.GONE);
            }
        });

        viewModel.getCartTotal().observe(this, total -> {
            binding.tvCartTotal.setText(formatter.format(total != null ? total : 0));
        });
    }

    private void setupListeners() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d("WarungKu", "Nav item clicked: " + id);
            if (id == R.id.nav_sell) {
                startActivity(new android.content.Intent(this, SellActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_stock) {
                startActivity(new android.content.Intent(this, StockActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_money) {
                startActivity(new android.content.Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_summary) {
                Log.d("WarungKu", "Navigating to SummaryActivity");
                startActivity(new android.content.Intent(this, SummaryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });

        binding.btnSellLarge.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SellActivity.class));
            overridePendingTransition(0, 0);
        });

        binding.btnAddStockFast.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, StockActivity.class));
            overridePendingTransition(0, 0);
        });

        binding.btnExpenseFast.setOnClickListener(v -> {
            showExpenseDialog();
        });

        binding.cardStockSummary.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ShoppingListActivity.class));
            overridePendingTransition(0, 0);
        });

        binding.btnCheckout.setOnClickListener(v -> {
            viewModel.checkout();
            android.widget.Toast.makeText(this, "Transaksi Berhasil!", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure home icon is selected when returning to MainActivity
        // Use post() to ensure the view is fully laid out
        if (binding != null) {
            binding.bottomNavigation.post(() -> {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            });
        }
    }

    private void showExpenseDialog() {
        android.widget.EditText etDesc = new android.widget.EditText(this);
        etDesc.setHint("Kebutuhan (misal: Listrik)");
        android.widget.EditText etPrice = new android.widget.EditText(this);
        etPrice.setHint("Jumlah (Rp)");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(etDesc);
        layout.addView(etPrice);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Catat Uang Keluar")
                .setView(layout)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String desc = etDesc.getText().toString().trim();
                    String sPrice = etPrice.getText().toString().trim();
                    if (!desc.isEmpty() && !sPrice.isEmpty()) {
                        double price = Double.parseDouble(sPrice);
                        com.zanjaprogrammer.warungku.data.entity.CashFlow flow = new com.zanjaprogrammer.warungku.data.entity.CashFlow(
                                "OUT", price, desc,
                                System.currentTimeMillis(), null, 0.0);
                        viewModel.insertCashFlow(flow);
                        android.widget.Toast.makeText(this, "Pengeluaran catat", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}

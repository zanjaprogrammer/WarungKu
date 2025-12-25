package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.zanjaprogrammer.warungku.adapters.HistoryAdapter;
import com.zanjaprogrammer.warungku.databinding.ActivityHistoryBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private AppViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        setupRecyclerView();

        viewModel.getAllHistory().observe(this, history -> {
            adapter.setItems(history);
        });

        viewModel.getBalance().observe(this, balance -> {
            binding.tvBalance.setText(java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"))
                    .format(balance != null ? balance : 0));
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_money);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_sell) {
                startActivity(new android.content.Intent(this, SellActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_stock) {
                startActivity(new android.content.Intent(this, StockActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_summary) {
                startActivity(new android.content.Intent(this, SummaryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });

        binding.btnRecordExpense.setOnClickListener(v -> showExpenseDialog());
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
                        android.widget.Toast.makeText(this, "Pengeluaran dicatat", android.widget.Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_money);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }
}

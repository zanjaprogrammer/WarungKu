package com.zanjaprogrammer.warungku;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.zanjaprogrammer.warungku.adapters.ProductStockAdapter;
import com.zanjaprogrammer.warungku.databinding.ActivityStockBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

public class StockActivity extends AppCompatActivity {

    private ActivityStockBinding binding;
    private AppViewModel viewModel;
    private ProductStockAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        setupRecyclerView();

        viewModel.getAllProducts().observe(this, products -> {
            adapter.setProducts(products);
        });

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddProductActivity.class);
            startActivity(intent);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_stock);
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
            } else if (id == R.id.nav_money) {
                startActivity(new android.content.Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_stock);
    }

    private void setupRecyclerView() {
        adapter = new ProductStockAdapter(product -> {
            showStockActionDialog(product);
        });
        binding.rvStock.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStock.setAdapter(adapter);
    }

    private void showStockActionDialog(com.zanjaprogrammer.warungku.data.entity.Product product) {
        String[] options = { "Tambah Stok", "Penyesuaian Stok (Override)" };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(product.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddStockDialog(product);
                    } else {
                        showAdjustStockDialog(product);
                    }
                })
                .show();
    }

    private void showAddStockDialog(com.zanjaprogrammer.warungku.data.entity.Product product) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Jumlah yang ditambah");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tambah Stok: " + product.name)
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        viewModel.addProductStock(product, Integer.parseInt(val));
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showAdjustStockDialog(com.zanjaprogrammer.warungku.data.entity.Product product) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(product.currentStock));
        input.setHint("Stok baru");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sesuaikan Stok: " + product.name)
                .setView(input)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        viewModel.adjustProductStock(product, Integer.parseInt(val));
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }
}

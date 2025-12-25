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
            } else if (id == R.id.nav_summary) {
                startActivity(new android.content.Intent(this, SummaryActivity.class));
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
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_stock_action, null);
        dialog.setContentView(view);

        android.widget.TextView tvProductName = view.findViewById(R.id.tvProductName);
        tvProductName.setText(product.name);

        view.findViewById(R.id.btnRestock).setOnClickListener(v -> {
            dialog.dismiss();
            showRestockBottomSheet(product);
        });

        view.findViewById(R.id.btnAdjustStock).setOnClickListener(v -> {
            dialog.dismiss();
            showAdjustStockDialog(product);
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmDialog(product);
        });

        dialog.show();
    }

    private void showRestockBottomSheet(com.zanjaprogrammer.warungku.data.entity.Product product) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_restock, null);
        dialog.setContentView(view);

        android.widget.TextView tvProductName = view.findViewById(R.id.tvRestockProductName);
        tvProductName.setText("Restock: " + product.name);

        com.google.android.material.textfield.TextInputEditText etQuantity = view.findViewById(R.id.etRestockQuantity);
        com.google.android.material.textfield.TextInputEditText etBuyPrice = view.findViewById(R.id.etRestockBuyPrice);
        com.google.android.material.textfield.TextInputLayout tilBuyPrice = view.findViewById(R.id.tilBuyPrice);
        android.widget.TextView tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        android.widget.RadioGroup rgPriceType = view.findViewById(R.id.rgPriceType);
        android.widget.RadioButton rbPerItem = view.findViewById(R.id.rbPerItem);
        android.widget.RadioButton rbTotal = view.findViewById(R.id.rbTotal);

        // Set default values
        etQuantity.setText("1");
        final boolean[] isPerItemMode = {true};

        // Update hint and calculate total
        final java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"));
        
        final java.lang.Runnable updateTotalPrice = new java.lang.Runnable() {
            @Override
            public void run() {
                try {
                    String qtyStr = etQuantity.getText().toString().trim();
                    String priceStr = etBuyPrice.getText().toString().trim();
                    
                    if (qtyStr.isEmpty() || priceStr.isEmpty()) {
                        tvTotalPrice.setText("Total: Rp 0");
                        return;
                    }

                    int qty = Integer.parseInt(qtyStr);
                    double price = Double.parseDouble(priceStr);

                    if (qty <= 0 || price <= 0) {
                        tvTotalPrice.setText("Total: Rp 0");
                        return;
                    }

                    double total;
                    if (isPerItemMode[0]) {
                        total = price * qty;
                    } else {
                        total = price;
                    }

                    tvTotalPrice.setText("Total: " + formatter.format(total));
                } catch (Exception e) {
                    tvTotalPrice.setText("Total: Rp 0");
                }
            }
        };
        
        android.text.TextWatcher priceWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalPrice.run();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        android.text.TextWatcher qtyWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalPrice.run();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        };

        etQuantity.addTextChangedListener(qtyWatcher);
        etBuyPrice.addTextChangedListener(priceWatcher);

        rgPriceType.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPerItem) {
                    isPerItemMode[0] = true;
                    tilBuyPrice.setHint("Harga Beli Per Barang (Rp)");
                    if (product.buyPrice != null) {
                        etBuyPrice.setText(String.valueOf((int) product.buyPrice.doubleValue()));
                    } else {
                        etBuyPrice.setText("");
                    }
                } else if (checkedId == R.id.rbTotal) {
                    isPerItemMode[0] = false;
                    tilBuyPrice.setHint("Total Harga Beli (Rp)");
                    etBuyPrice.setText("");
                }
                updateTotalPrice.run();
            }
        });

        // Initialize with per item mode
        if (product.buyPrice != null) {
            etBuyPrice.setText(String.valueOf((int) product.buyPrice.doubleValue()));
        }
        updateTotalPrice.run();

        view.findViewById(R.id.btnRestockSave).setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            String buyPriceStr = etBuyPrice.getText().toString().trim();

            if (qtyStr.isEmpty() || buyPriceStr.isEmpty()) {
                android.widget.Toast.makeText(this, "Mohon isi jumlah dan harga", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int quantity = Integer.parseInt(qtyStr);
                double inputPrice = Double.parseDouble(buyPriceStr);

                if (quantity <= 0) {
                    android.widget.Toast.makeText(this, "Jumlah harus lebih dari 0", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputPrice <= 0) {
                    android.widget.Toast.makeText(this, "Harga harus lebih dari 0", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                // Calculate harga per unit berdasarkan mode
                double buyPricePerUnit;
                if (isPerItemMode[0]) {
                    buyPricePerUnit = inputPrice;
                } else {
                    buyPricePerUnit = inputPrice / quantity;
                }

                viewModel.addProductStock(product, quantity, buyPricePerUnit);
                android.widget.Toast.makeText(this, "Stok berhasil ditambahkan", android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(this, "Input tidak valid", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(com.zanjaprogrammer.warungku.data.entity.Product product) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Barang")
                .setMessage("Apakah Anda yakin ingin menghapus " + product.name + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    viewModel.deleteProduct(product);
                    android.widget.Toast.makeText(this, "Barang berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show();
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

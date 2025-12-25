package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.zanjaprogrammer.warungku.adapters.ProductSellAdapter;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.databinding.ActivitySellBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

public class SellActivity extends AppCompatActivity {

    private ActivitySellBinding binding;
    private AppViewModel viewModel;
    private ProductSellAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySellBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        setupRecyclerView();

        viewModel.getAllProducts().observe(this, products -> {
            adapter.setProducts(products);
        });

        binding.bottomNavigation.setSelectedItemId(R.id.nav_sell);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_stock) {
                startActivity(new android.content.Intent(this, StockActivity.class));
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
        binding.bottomNavigation.setSelectedItemId(R.id.nav_sell);
    }

    private void setupRecyclerView() {
        adapter = new ProductSellAdapter(new ProductSellAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                if (product.currentStock > 0) {
                    showQuantityBottomSheet(product);
                } else {
                    Toast.makeText(SellActivity.this, "Stok habis!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onProductLongClick(Product product) {
                // Keep for detail or skip
            }
        });
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvProducts.setAdapter(adapter);

        // Observe Cart
        java.text.NumberFormat formatter = java.text.NumberFormat
                .getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"));
        viewModel.getCartItems().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                binding.cardCartSummary.setVisibility(android.view.View.VISIBLE);
                binding.tvCartCount.setText(items.size() + " Barang");
            } else {
                binding.cardCartSummary.setVisibility(android.view.View.GONE);
            }
        });

        viewModel.getCartTotal().observe(this, total -> {
            binding.tvCartTotal.setText(formatter.format(total));
        });

        binding.btnCheckout.setOnClickListener(v -> {
            viewModel.checkout();
            Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show();
        });
    }

    private int currentSheetQty = 1;

    private void showQuantityBottomSheet(Product product) {
        currentSheetQty = 1;
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_quantity, null);
        dialog.setContentView(view);

        android.widget.TextView tvName = view.findViewById(R.id.tvSheetName);
        android.widget.TextView tvPrice = view.findViewById(R.id.tvSheetPrice);
        android.widget.TextView tvQty = view.findViewById(R.id.tvSheetQuantity);
        android.view.View btnMinus = view.findViewById(R.id.btnSheetMinus);
        android.view.View btnPlus = view.findViewById(R.id.btnSheetPlus);
        android.widget.Button btnAdd = view.findViewById(R.id.btnSheetAdd);

        java.text.NumberFormat formatter = java.text.NumberFormat
                .getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"));
        tvName.setText(product.name);
        tvPrice.setText(formatter.format(product.sellPrice) + " (Stok: " + product.currentStock + ")");
        tvQty.setText(String.valueOf(currentSheetQty));

        btnMinus.setOnClickListener(v -> {
            if (currentSheetQty > 1) {
                currentSheetQty--;
                tvQty.setText(String.valueOf(currentSheetQty));
            }
        });

        btnPlus.setOnClickListener(v -> {
            if (currentSheetQty < product.currentStock) {
                currentSheetQty++;
                tvQty.setText(String.valueOf(currentSheetQty));
            } else {
                Toast.makeText(this, "Mencapai batas stok!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(v -> {
            viewModel.addToCart(product, currentSheetQty);
            Toast.makeText(this, "Berhasil masuk keranjang", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}

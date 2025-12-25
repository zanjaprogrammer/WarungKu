package com.zanjaprogrammer.warungku;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.databinding.ActivityAddProductBinding;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private AppViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AppViewModel.class);

        binding.btnSave.setOnClickListener(v -> saveProduct());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void saveProduct() {
        String name = binding.etName.getText().toString().trim();
        String sSellPrice = binding.etSellPrice.getText().toString().trim();
        String sBuyPrice = binding.etBuyPrice.getText().toString().trim();
        String sStock = binding.etStock.getText().toString().trim();
        String sMinStock = binding.etMinStock.getText().toString().trim();

        if (name.isEmpty() || sSellPrice.isEmpty() || sStock.isEmpty() || sMinStock.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field wajib", Toast.LENGTH_SHORT).show();
            return;
        }

        double sellPrice = Double.parseDouble(sSellPrice);
        Double buyPrice = sBuyPrice.isEmpty() ? null : Double.parseDouble(sBuyPrice);
        int stock = Integer.parseInt(sStock);
        int minStock = Integer.parseInt(sMinStock);

        Product product = new Product(name, sellPrice, buyPrice, stock, minStock);
        viewModel.insertProduct(product);

        Toast.makeText(this, "Barang berhasil disimpan", Toast.LENGTH_SHORT).show();
        finish();
    }
}

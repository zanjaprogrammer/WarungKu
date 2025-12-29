package com.zanjaprogrammer.warungku;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.ScanContract;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.databinding.ActivityAddProductBinding;
import com.zanjaprogrammer.warungku.utils.BarcodeScannerHelper;
import com.zanjaprogrammer.warungku.utils.NetworkUtils;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;
import com.zanjaprogrammer.warungku.api.ProductApiClient;
import com.zanjaprogrammer.warungku.api.ProductApiService;
import com.zanjaprogrammer.warungku.api.ProductApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private AppViewModel viewModel;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    private boolean isEditMode = false;
    private int productId = -1;
    private com.zanjaprogrammer.warungku.data.entity.Product productToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Use singleton instance untuk persist cart across activities
        viewModel = AppViewModel.getInstance(getApplication());
        
        // Check if edit mode
        productId = getIntent().getIntExtra("product_id", -1);
        isEditMode = getIntent().getBooleanExtra("edit_mode", false) && productId > 0;
        
        if (isEditMode) {
            binding.toolbar.setTitle("Edit Produk");
            loadProductForEdit();
        } else {
            binding.toolbar.setTitle("Tambah Produk");
        }

        binding.btnSave.setOnClickListener(v -> saveProduct());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Setup barcode scanner - find TextInputLayout and set end icon click listener
        android.view.ViewParent parent = binding.etBarcode.getParent();
        if (parent != null) {
            android.view.ViewParent grandParent = parent.getParent();
            if (grandParent instanceof com.google.android.material.textfield.TextInputLayout) {
                com.google.android.material.textfield.TextInputLayout barcodeLayout = 
                    (com.google.android.material.textfield.TextInputLayout) grandParent;
                barcodeLayout.setEndIconOnClickListener(v -> scanBarcode());
            }
        }

        // Setup barcode launcher
        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String barcode = result.getContents();
                binding.etBarcode.setText(barcode);
                // Lookup produk dari database lokal atau API
                lookupProductByBarcode(barcode);
            }
        });
        
        // Handle barcode dari intent (jika dibuka dari SellActivity)
        String barcodeFromIntent = getIntent().getStringExtra("barcode");
        if (barcodeFromIntent != null && !barcodeFromIntent.isEmpty()) {
            binding.etBarcode.setText(barcodeFromIntent);
            lookupProductByBarcode(barcodeFromIntent);
        }
    }

    private void saveProduct() {
        String name = binding.etName.getText().toString().trim();
        String sSellPrice = binding.etSellPrice.getText().toString().trim();
        String sBuyPrice = binding.etBuyPrice.getText().toString().trim();
        String sStock = binding.etStock.getText().toString().trim();
        String sMinStock = binding.etMinStock.getText().toString().trim();
        String barcode = binding.etBarcode.getText().toString().trim();

        if (name.isEmpty() || sSellPrice.isEmpty() || sStock.isEmpty() || sMinStock.isEmpty()) {
            Toast.makeText(this, "Mohon isi semua field wajib", Toast.LENGTH_SHORT).show();
            return;
        }

        double sellPrice = Double.parseDouble(sSellPrice);
        Double buyPrice = sBuyPrice.isEmpty() ? null : Double.parseDouble(sBuyPrice);
        int stock = Integer.parseInt(sStock);
        int minStock = Integer.parseInt(sMinStock);

        if (isEditMode && productToEdit != null) {
            // Update existing product
            productToEdit.name = name;
            productToEdit.sellPrice = sellPrice;
            productToEdit.buyPrice = buyPrice;
            productToEdit.currentStock = stock;
            productToEdit.minStock = minStock;
            productToEdit.barcode = barcode.isEmpty() ? null : barcode;
            
            viewModel.updateProduct(productToEdit);
            Toast.makeText(this, "Produk berhasil diupdate", Toast.LENGTH_SHORT).show();
        } else {
            // Insert new product
        Product product = new Product(name, sellPrice, buyPrice, stock, minStock);
            product.barcode = barcode.isEmpty() ? null : barcode;
        viewModel.insertProduct(product);
            Toast.makeText(this, "Barang berhasil disimpan", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
    
    /**
     * Load product data for editing
     */
    private void loadProductForEdit() {
        if (productId <= 0) {
            Toast.makeText(this, "Product ID tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Get product from database
        viewModel.getAllProducts().observe(this, products -> {
            if (products != null) {
                for (Product product : products) {
                    if (product.id == productId) {
                        productToEdit = product;
                        fillFormWithProduct(product);
                        // Remove observer after loading
                        viewModel.getAllProducts().removeObservers(this);
                        break;
                    }
                }
                
                if (productToEdit == null) {
                    Toast.makeText(this, "Produk tidak ditemukan", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void scanBarcode() {
        if (!BarcodeScannerHelper.hasCameraPermission(this)) {
            BarcodeScannerHelper.requestCameraPermission(this);
            return;
        }

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt("Arahkan kamera ke barcode");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        // Lock orientasi ke portrait, tapi tetap bisa detect barcode landscape
        options.setOrientationLocked(true);
        // Set custom capture activity untuk portrait mode
        options.setCaptureActivity(com.zanjaprogrammer.warungku.PortraitCaptureActivity.class);

        barcodeLauncher.launch(options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BarcodeScannerHelper.getCameraPermissionRequestCode()) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                scanBarcode();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk scan barcode", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Lookup produk berdasarkan barcode
     * Hybrid approach: cek database lokal dulu, lalu API eksternal
     */
    private void lookupProductByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return;
        }
        
        // 1. Cek database lokal dulu (di background thread)
        viewModel.getProductByBarcode(barcode, new DataRepository.ProductCallback() {
            @Override
            public void onProductFound(Product localProduct) {
                // Produk sudah ada di database lokal, auto-fill form
                runOnUiThread(() -> {
                    fillFormWithProduct(localProduct);
                    Toast.makeText(AddProductActivity.this, 
                        "Data produk ditemukan di database lokal", 
                        Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onProductNotFound() {
                // 2. Jika tidak ada di lokal, cek API eksternal
                if (!ProductApiClient.isConfigured()) {
                    // API belum dikonfigurasi, biarkan user input manual
                    runOnUiThread(() -> {
                        Toast.makeText(AddProductActivity.this, 
                            "Produk baru, silakan isi data manual", 
                            Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Check network connectivity sebelum call API
                if (!NetworkUtils.isNetworkAvailable(AddProductActivity.this)) {
                    runOnUiThread(() -> {
                        Toast.makeText(AddProductActivity.this, 
                            "Tidak ada koneksi internet. Silakan isi data manual atau coba lagi nanti.", 
                            Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Tampilkan loading indicator
                runOnUiThread(() -> showLoading(true));
                
                // Call API (Open Food Facts - 100% gratis, tidak perlu token)
                ProductApiService apiService = ProductApiClient.getApiService();
                Call<ProductApiResponse> call = apiService.lookupProduct(barcode);
                
                call.enqueue(new Callback<ProductApiResponse>() {
                    @Override
                    public void onResponse(Call<ProductApiResponse> call, Response<ProductApiResponse> response) {
                        runOnUiThread(() -> showLoading(false));
                        
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                ProductApiResponse apiProduct = response.body();
                                
                                if (apiProduct != null && apiProduct.isSuccess()) {
                                    // Data produk ditemukan di API, auto-fill form
                                    runOnUiThread(() -> {
                                        fillFormWithApiProduct(apiProduct);
                                        Toast.makeText(AddProductActivity.this, 
                                            "Data produk ditemukan dari database eksternal", 
                                            Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    // Produk tidak ditemukan di API
                                    runOnUiThread(() -> {
                                        Toast.makeText(AddProductActivity.this, 
                                            "Produk tidak ditemukan, silakan isi data manual", 
                                            Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                // API error
                                runOnUiThread(() -> {
                                    Toast.makeText(AddProductActivity.this, 
                                        "Gagal mengambil data produk, silakan isi manual", 
                                        Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            // Handle any exception
                            runOnUiThread(() -> {
                                Toast.makeText(AddProductActivity.this, 
                                    "Error: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ProductApiResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            // Network error atau API tidak tersedia
                            Toast.makeText(AddProductActivity.this, 
                                "Tidak dapat mengakses database produk, silakan isi manual", 
                                Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Auto-fill form dengan data dari database lokal
     */
    private void fillFormWithProduct(Product product) {
        binding.etName.setText(product.name);
        binding.etSellPrice.setText(String.valueOf((int) product.sellPrice));
        if (product.buyPrice != null) {
            binding.etBuyPrice.setText(String.valueOf((int) product.buyPrice.doubleValue()));
        }
        binding.etStock.setText(String.valueOf(product.currentStock));
        binding.etMinStock.setText(String.valueOf(product.minStock));
    }
    
    /**
     * Auto-fill form dengan data dari API eksternal (Open Food Facts)
     */
    private void fillFormWithApiProduct(ProductApiResponse apiProduct) {
        if (apiProduct == null || binding == null) {
            return;
        }
        
        try {
            // Nama produk
            String productName = apiProduct.getName();
            String brand = apiProduct.getBrand();
            
            if (productName != null && !productName.isEmpty()) {
                // Gabungkan brand dan nama jika ada
                if (brand != null && !brand.isEmpty() && !productName.contains(brand)) {
                    binding.etName.setText(brand + " " + productName);
                } else {
                    binding.etName.setText(productName);
                }
            }
            
            // Quantity bisa ditambahkan ke nama jika ada (misal: "Teh Botol Sosro 330ml")
            String quantity = apiProduct.getQuantity();
            if (quantity != null && !quantity.isEmpty()) {
                String currentName = binding.etName.getText() != null ? 
                    binding.etName.getText().toString() : "";
                if (!currentName.contains(quantity)) {
                    binding.etName.setText(currentName + " " + quantity);
                }
            }
            
            // Note: Open Food Facts tidak menyediakan data harga
            // User harus input harga manual
            // Stok default 0 (user harus input manual)
            binding.etStock.setText("0");
            binding.etMinStock.setText("5"); // Default min stock
            
            // Tampilkan info tambahan jika ada
            String category = apiProduct.getCategory();
            if (category != null && !category.isEmpty()) {
                Toast.makeText(this, 
                    "Kategori: " + category + 
                    (quantity != null && !quantity.isEmpty() ? " | Ukuran: " + quantity : ""), 
                    Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // Handle any exception during form filling
            Toast.makeText(this, 
                "Error mengisi form: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Tampilkan/sembunyikan loading indicator
     */
    private void showLoading(boolean show) {
        // Bisa ditambahkan ProgressBar di layout jika diperlukan
        // Untuk sekarang, hanya toast message
        if (show) {
            Toast.makeText(this, "Mencari data produk...", Toast.LENGTH_SHORT).show();
        }
    }
}

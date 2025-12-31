package com.zanjaprogrammer.warungku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.journeyapps.barcodescanner.ScanOptions;
import com.journeyapps.barcodescanner.ScanContract;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zanjaprogrammer.warungku.data.entity.Product;
import com.zanjaprogrammer.warungku.data.DataRepository;
import com.zanjaprogrammer.warungku.databinding.ActivityAddProductBinding;
import com.zanjaprogrammer.warungku.utils.BarcodeScannerHelper;
import com.zanjaprogrammer.warungku.utils.NetworkUtils;
import com.zanjaprogrammer.warungku.utils.CustomToast;
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
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Uri tempImageUri;
    private boolean isEditMode = false;
    private int productId = -1;
    private com.zanjaprogrammer.warungku.data.entity.Product productToEdit;
    private String currentImageUrl = null;
    private String apiImageUrl = null; // Store API image URL separately
    private boolean pendingCameraAction = false; // Flag to track if camera action is pending

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

        setupImageHandling();
        setupBarcodeScanner();
        
        binding.btnSave.setOnClickListener(v -> saveProduct());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Handle barcode dari intent (jika dibuka dari SellActivity)
        String barcodeFromIntent = getIntent().getStringExtra("barcode");
        if (barcodeFromIntent != null && !barcodeFromIntent.isEmpty()) {
            binding.etBarcode.setText(barcodeFromIntent);
            lookupProductByBarcode(barcodeFromIntent);
        }
    }

    private void setupImageHandling() {
        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    if (pendingCameraAction) {
                        pendingCameraAction = false;
                        launchCamera();
                    }
                    CustomToast.showSuccess(this, "Izin kamera diberikan");
                } else {
                    pendingCameraAction = false;
                    CustomToast.showError(this, "Izin kamera diperlukan untuk mengambil foto");
                }
            }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    currentImageUrl = uri.toString();
                    loadImageIntoView(currentImageUrl);
                    binding.btnRemoveImage.setVisibility(android.view.View.VISIBLE);
                    binding.btnUseApiImage.setVisibility(android.view.View.GONE);
                }
            }
        );

        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && tempImageUri != null) {
                    currentImageUrl = tempImageUri.toString();
                    loadImageIntoView(currentImageUrl);
                    binding.btnRemoveImage.setVisibility(android.view.View.VISIBLE);
                    binding.btnUseApiImage.setVisibility(android.view.View.GONE);
                    CustomToast.showSuccess(this, "Foto berhasil diambil");
                }
            }
        );

        // Button listeners
        binding.btnSelectFromGallery.setOnClickListener(v -> {
            showImageSourceDialog();
        });

        binding.btnUseApiImage.setOnClickListener(v -> {
            if (apiImageUrl != null && !apiImageUrl.isEmpty()) {
                currentImageUrl = apiImageUrl;
                loadImageIntoView(currentImageUrl);
                binding.btnRemoveImage.setVisibility(android.view.View.VISIBLE);
                binding.btnUseApiImage.setVisibility(android.view.View.GONE);
                CustomToast.showSuccess(this, "Gambar dari API digunakan");
            }
        });

        binding.btnRemoveImage.setOnClickListener(v -> {
            currentImageUrl = null;
            binding.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
            binding.btnRemoveImage.setVisibility(android.view.View.GONE);
            if (apiImageUrl != null && !apiImageUrl.isEmpty()) {
                binding.btnUseApiImage.setVisibility(android.view.View.VISIBLE);
            }
        });
    }

    private void loadImageIntoView(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivProductImage);
        } else {
            binding.ivProductImage.setImageResource(R.drawable.ic_image_placeholder);
        }
    }

    private void setupBarcodeScanner() {
        // Setup barcode scanner button
        binding.btnScanBarcode.setOnClickListener(v -> scanBarcode());

        // Setup barcode launcher
        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String barcode = result.getContents();
                binding.etBarcode.setText(barcode);
                // Lookup produk dari database lokal atau API
                lookupProductByBarcode(barcode);
            }
        });
    }

    private void saveProduct() {
        String name = binding.etName.getText().toString().trim();
        String sSellPrice = binding.etSellPrice.getText().toString().trim();
        String sBuyPrice = binding.etBuyPrice.getText().toString().trim();
        String sStock = binding.etStock.getText().toString().trim();
        String sMinStock = binding.etMinStock.getText().toString().trim();
        String barcode = binding.etBarcode.getText().toString().trim();

        if (name.isEmpty() || sSellPrice.isEmpty() || sStock.isEmpty() || sMinStock.isEmpty()) {
            CustomToast.showError(this, "Mohon isi semua field wajib");
            return;
        }

        double sellPrice = Double.parseDouble(sSellPrice);
        Double buyPrice = sBuyPrice.isEmpty() ? null : Double.parseDouble(sBuyPrice);
        int stock = Integer.parseInt(sStock);
        int minStock = Integer.parseInt(sMinStock);

        if (isEditMode && productToEdit != null) {
            // Update existing product - skip duplicate check
            productToEdit.name = name;
            productToEdit.sellPrice = sellPrice;
            productToEdit.buyPrice = buyPrice;
            productToEdit.currentStock = stock;
            productToEdit.minStock = minStock;
            productToEdit.barcode = barcode.isEmpty() ? null : barcode;
            productToEdit.imageUrl = currentImageUrl; // Save image URL
            
            viewModel.updateProduct(productToEdit);
            CustomToast.showSuccess(this, "Produk berhasil diupdate");
            finish();
        } else {
            // Create new product - check for duplicates first
            viewModel.checkProductExists(name, barcode.isEmpty() ? null : barcode, new com.zanjaprogrammer.warungku.data.DataRepository.ProductExistsCallback() {
                @Override
                public void onProductExists(Product existingProduct) {
                    runOnUiThread(() -> {
                        showDuplicateProductDialog(existingProduct, name, sellPrice, buyPrice, stock, minStock, barcode);
                    });
                }
                
                @Override
                public void onProductNotExists() {
                    runOnUiThread(() -> {
                        // No duplicate found, proceed with creating new product
                        Product product = new Product(name, sellPrice, buyPrice, stock, minStock);
                        product.barcode = barcode.isEmpty() ? null : barcode;
                        product.imageUrl = currentImageUrl; // Save image URL
                        
                        viewModel.addProduct(product);
                        CustomToast.showSuccess(AddProductActivity.this, "Produk berhasil ditambahkan");
                        finish();
                    });
                }
            });
        }
    }
    
    /**
     * Load product data for editing
     */
    private void loadProductForEdit() {
        if (productId <= 0) {
            CustomToast.showError(this, "Product ID tidak valid");
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
                    CustomToast.showError(this, "Produk tidak ditemukan");
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
                // Permission granted - check what action was requested
                // This could be for barcode scanning or taking photo
                CustomToast.showSuccess(this, "Izin kamera diberikan");
            } else {
                CustomToast.showError(this, "Izin kamera diperlukan untuk menggunakan fitur kamera");
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
                    CustomToast.showInfo(AddProductActivity.this, 
                        "Data produk ditemukan di database lokal");
                });
            }
            
            @Override
            public void onProductNotFound() {
                // 2. Jika tidak ada di lokal, cek API eksternal
                if (!ProductApiClient.isConfigured()) {
                    // API belum dikonfigurasi, biarkan user input manual
                    runOnUiThread(() -> {
                        CustomToast.showInfo(AddProductActivity.this, 
                            "Produk baru, silakan isi data manual");
                    });
                    return;
                }
                
                // Check network connectivity sebelum call API
                if (!NetworkUtils.isNetworkAvailable(AddProductActivity.this)) {
                    runOnUiThread(() -> {
                        CustomToast.showWarning(AddProductActivity.this, 
                            "Tidak ada koneksi internet. Silakan isi data manual atau coba lagi nanti.");
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
                                        CustomToast.showSuccess(AddProductActivity.this, 
                                            "Data produk ditemukan dari database eksternal");
                                    });
                                } else {
                                    // Produk tidak ditemukan di API
                                    runOnUiThread(() -> {
                                        CustomToast.showInfo(AddProductActivity.this, 
                                            "Produk tidak ditemukan, silakan isi data manual");
                                    });
                                }
                            } else {
                                // API error
                                runOnUiThread(() -> {
                                    CustomToast.showError(AddProductActivity.this, 
                                        "Gagal mengambil data produk, silakan isi manual");
                                });
                            }
                        } catch (Exception e) {
                            // Handle any exception
                            runOnUiThread(() -> {
                                CustomToast.showError(AddProductActivity.this, 
                                    "Error: " + e.getMessage());
                            });
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ProductApiResponse> call, Throwable t) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            // Network error atau API tidak tersedia
                            CustomToast.showWarning(AddProductActivity.this, 
                                "Tidak dapat mengakses database produk, silakan isi manual");
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
        
        // Load existing image if available
        if (product.imageUrl != null && !product.imageUrl.isEmpty()) {
            currentImageUrl = product.imageUrl;
            loadImageIntoView(currentImageUrl);
            binding.btnRemoveImage.setVisibility(android.view.View.VISIBLE);
        }
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
            
            // Handle product image from API
            String imageUrl = apiProduct.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                apiImageUrl = imageUrl;
                binding.btnUseApiImage.setVisibility(android.view.View.VISIBLE);
                // Auto-load API image
                currentImageUrl = apiImageUrl;
                loadImageIntoView(currentImageUrl);
                binding.btnRemoveImage.setVisibility(android.view.View.VISIBLE);
                binding.btnUseApiImage.setVisibility(android.view.View.GONE);
            }
            
            // Note: Open Food Facts tidak menyediakan data harga
            // User harus input harga manual
            // Stok default 0 (user harus input manual)
            binding.etStock.setText("0");
            binding.etMinStock.setText("5"); // Default min stock
            
            // Tampilkan info tambahan jika ada
            String category = apiProduct.getCategory();
            if (category != null && !category.isEmpty()) {
                CustomToast.showInfo(this, 
                    "Kategori: " + category + 
                    (quantity != null && !quantity.isEmpty() ? " | Ukuran: " + quantity : ""));
            }
        } catch (Exception e) {
            // Handle any exception during form filling
            CustomToast.showError(this, 
                "Error mengisi form: " + e.getMessage());
        }
    }
    
    /**
     * Tampilkan/sembunyikan loading indicator
     */
    private void showLoading(boolean show) {
        // Bisa ditambahkan ProgressBar di layout jika diperlukan
        // Untuk sekarang, hanya toast message
        if (show) {
            CustomToast.showInfo(this, "Mencari data produk...");
        }
    }

    /**
     * Show dialog to choose image source (camera or gallery)
     */
    private void showImageSourceDialog() {
        String[] options = {"Ambil Foto", "Pilih dari Galeri"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Camera
                    takePictureFromCamera();
                } else {
                    // Gallery
                    galleryLauncher.launch("image/*");
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    /**
     * Take picture from camera
     */
    private void takePictureFromCamera() {
        // Check camera permission first
        if (!BarcodeScannerHelper.hasCameraPermission(this)) {
            pendingCameraAction = true;
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            return;
        }
        
        launchCamera();
    }
    
    /**
     * Launch camera after permission is granted
     */
    private void launchCamera() {
        try {
            // Create Pictures directory if it doesn't exist
            java.io.File picturesDir = new java.io.File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), "");
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }
            
            // Create temporary file for the image
            java.io.File imageFile = new java.io.File(picturesDir, 
                "product_" + System.currentTimeMillis() + ".jpg");
            
            tempImageUri = androidx.core.content.FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", imageFile);
            
            cameraLauncher.launch(tempImageUri);
        } catch (Exception e) {
            CustomToast.showError(this, "Error mengambil foto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show dialog when duplicate product is found
     */
    private void showDuplicateProductDialog(Product existingProduct, String newName, double newSellPrice, 
                                          Double newBuyPrice, int newStock, int newMinStock, String newBarcode) {
        String duplicateReason = "";
        if (existingProduct.name.equalsIgnoreCase(newName)) {
            duplicateReason = "nama yang sama";
        } else if (existingProduct.barcode != null && existingProduct.barcode.equals(newBarcode)) {
            duplicateReason = "barcode yang sama";
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Produk Sudah Ada")
            .setMessage("Produk dengan " + duplicateReason + " sudah ada:\n\n" +
                       "Nama: " + existingProduct.name + "\n" +
                       "Harga: Rp " + (int) existingProduct.sellPrice + "\n" +
                       "Stok: " + existingProduct.currentStock + "\n\n" +
                       "Apa yang ingin Anda lakukan?")
            .setPositiveButton("Tambah Stok", (dialog, which) -> {
                // Add stock to existing product
                int additionalStock = newStock;
                double buyPrice = newBuyPrice != null ? newBuyPrice : existingProduct.buyPrice != null ? existingProduct.buyPrice : newSellPrice * 0.7;
                viewModel.addProductStock(existingProduct, additionalStock, buyPrice);
                CustomToast.showSuccess(this, "Stok berhasil ditambahkan ke produk yang sudah ada");
                finish();
            })
            .setNeutralButton("Edit Produk", (dialog, which) -> {
                // Open edit mode for existing product
                Intent intent = new Intent(this, AddProductActivity.class);
                intent.putExtra("product_id", existingProduct.id);
                intent.putExtra("edit_mode", true);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Batal", null)
            .show();
    }
}

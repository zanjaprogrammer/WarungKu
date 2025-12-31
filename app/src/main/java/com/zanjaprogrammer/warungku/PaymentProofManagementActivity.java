package com.zanjaprogrammer.warungku;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.zanjaprogrammer.warungku.adapters.PaymentProofManagementAdapter;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.databinding.ActivityPaymentProofManagementBinding;
import com.zanjaprogrammer.warungku.service.PaymentProofManager;
import com.zanjaprogrammer.warungku.utils.CurrencyFormatter;
import com.zanjaprogrammer.warungku.viewmodel.AppViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for managing payment proof photos
 * Provides grid gallery view, search/filter functionality, and bulk operations
 */
public class PaymentProofManagementActivity extends AppCompatActivity {
    
    private ActivityPaymentProofManagementBinding binding;
    private AppViewModel viewModel;
    private PaymentProofManager paymentProofManager;
    private PaymentProofManagementAdapter adapter;
    
    private List<PaymentProof> allPaymentProofs = new ArrayList<>();
    private List<PaymentProof> filteredPaymentProofs = new ArrayList<>();
    private List<Long> selectedProofIds = new ArrayList<>();
    
    // Filter state
    private long filterStartDate = 0;
    private long filterEndDate = 0;
    private String searchQuery = "";
    private boolean isSelectionMode = false;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityPaymentProofManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize components
        viewModel = AppViewModel.getInstance(getApplication());
        paymentProofManager = new PaymentProofManager(this);
        
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupFab();
        loadPaymentProofs();
        updateStatistics();
    }
    
    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Bukti Pembayaran");
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isSelectionMode) {
                exitSelectionMode();
            } else {
                finish();
            }
        });
    }
    
    private void setupRecyclerView() {
        adapter = new PaymentProofManagementAdapter();
        
        // Use GridLayoutManager with 2 columns for gallery view
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.rvPaymentProofs.setLayoutManager(layoutManager);
        binding.rvPaymentProofs.setAdapter(adapter);
        
        // Set adapter callbacks
        adapter.setOnItemClickListener(this::onPaymentProofClick);
        adapter.setOnItemLongClickListener(this::onPaymentProofLongClick);
        adapter.setOnSelectionChangedListener(this::onSelectionChanged);
    }
    
    private void setupSearchAndFilters() {
        // Search functionality
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                applyFilters();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Date range filter
        binding.btnDateRange.setOnClickListener(v -> showDateRangeDialog());
        
        // Clear filters
        binding.btnClearFilters.setOnClickListener(v -> clearAllFilters());
    }
    
    private void setupFab() {
        binding.fabBulkActions.setOnClickListener(v -> {
            if (isSelectionMode) {
                showBulkActionsDialog();
            } else {
                enterSelectionMode();
            }
        });
        
        updateFabIcon();
    }
    
    private void loadPaymentProofs() {
        paymentProofManager.getAllPaymentProofs().observe(this, paymentProofs -> {
            if (paymentProofs != null) {
                allPaymentProofs.clear();
                allPaymentProofs.addAll(paymentProofs);
                applyFilters();
                updateEmptyState();
                updateStatistics();
            }
        });
    }
    
    private void applyFilters() {
        filteredPaymentProofs.clear();
        
        for (PaymentProof proof : allPaymentProofs) {
            // Apply search filter
            if (!searchQuery.isEmpty()) {
                boolean matchesSearch = false;
                
                // Search in payment method
                if (proof.getPaymentMethod() != null && 
                    proof.getPaymentMethod().toLowerCase().contains(searchQuery.toLowerCase())) {
                    matchesSearch = true;
                }
                
                // Search in file name
                if (proof.getFileName() != null && 
                    proof.getFileName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    matchesSearch = true;
                }
                
                if (!matchesSearch) {
                    continue;
                }
            }
            
            // Apply date range filter
            if (filterStartDate > 0 && proof.getCaptureTimestamp() < filterStartDate) {
                continue;
            }
            if (filterEndDate > 0 && proof.getCaptureTimestamp() > filterEndDate) {
                continue;
            }
            
            filteredPaymentProofs.add(proof);
        }
        
        adapter.setPaymentProofs(filteredPaymentProofs);
        updateFilterChips();
        updateEmptyState();
    }
    
    private void updateFilterChips() {
        // Update date range button
        if (filterStartDate > 0 && filterEndDate > 0) {
            String dateText = dateFormat.format(new Date(filterStartDate)) + " - " + 
                            dateFormat.format(new Date(filterEndDate));
            binding.btnDateRange.setText(dateText);
            binding.btnDateRange.setIcon(getDrawable(R.drawable.ic_close));
            binding.btnDateRange.setOnClickListener(v -> {
                filterStartDate = 0;
                filterEndDate = 0;
                applyFilters();
            });
        } else {
            binding.btnDateRange.setText("Rentang Tanggal");
            binding.btnDateRange.setIcon(getDrawable(R.drawable.ic_calendar));
            binding.btnDateRange.setOnClickListener(v -> showDateRangeDialog());
        }
        
        // Show/hide clear filters button
        boolean hasActiveFilters = filterStartDate > 0 || filterEndDate > 0 || 
                                  !searchQuery.isEmpty();
        binding.btnClearFilters.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }
    
    private void updateEmptyState() {
        if (filteredPaymentProofs.isEmpty()) {
            binding.rvPaymentProofs.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            
            if (allPaymentProofs.isEmpty()) {
                // No payment proofs at all
                binding.tvEmptyTitle.setText("Belum Ada Bukti Pembayaran");
                binding.tvEmptyMessage.setText("Mulai transaksi QRIS dan ambil foto bukti pembayaran untuk melihat galeri");
                binding.btnEmptyAction.setText("Mulai Transaksi");
                binding.btnEmptyAction.setOnClickListener(v -> {
                    startActivity(new Intent(this, SellActivity.class));
                    finish();
                });
            } else {
                // Has payment proofs but filtered out
                binding.tvEmptyTitle.setText("Tidak Ada Hasil");
                binding.tvEmptyMessage.setText("Tidak ada bukti pembayaran yang sesuai dengan filter yang dipilih");
                binding.btnEmptyAction.setText("Hapus Filter");
                binding.btnEmptyAction.setOnClickListener(v -> clearAllFilters());
            }
        } else {
            binding.rvPaymentProofs.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        }
    }
    
    private void updateStatistics() {
        paymentProofManager.getPaymentProofStatistics().thenAccept(stats -> {
            runOnUiThread(() -> {
                if (stats != null && stats.length >= 4) {
                    long totalCount = stats[0];
                    long qrisCount = stats[1];
                    long totalSize = stats[2];
                    long corruptedCount = stats[3];
                    
                    String statsText = String.format(Locale.getDefault(),
                        "%d foto • %s • %d rusak",
                        totalCount,
                        formatFileSize(totalSize),
                        corruptedCount
                    );
                    
                    binding.tvStatistics.setText(statsText);
                    binding.tvStatistics.setVisibility(View.VISIBLE);
                }
            });
        });
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    // ========== SELECTION MODE ==========
    
    private void enterSelectionMode() {
        isSelectionMode = true;
        selectedProofIds.clear();
        adapter.setSelectionMode(true);
        updateSelectionUI();
        updateFabIcon();
    }
    
    private void exitSelectionMode() {
        isSelectionMode = false;
        selectedProofIds.clear();
        adapter.setSelectionMode(false);
        adapter.clearSelection();
        updateSelectionUI();
        updateFabIcon();
    }
    
    private void updateSelectionUI() {
        if (isSelectionMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(selectedProofIds.size() + " dipilih");
            }
            binding.layoutFilters.setVisibility(View.GONE);
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Kelola Bukti Pembayaran");
            }
            binding.layoutFilters.setVisibility(View.VISIBLE);
        }
        
        invalidateOptionsMenu();
    }
    
    private void updateFabIcon() {
        if (isSelectionMode) {
            binding.fabBulkActions.setText("Konfirmasi");
            binding.fabBulkActions.setIcon(getDrawable(R.drawable.ic_check_circle));
        } else {
            binding.fabBulkActions.setText("Pilih");
            binding.fabBulkActions.setIcon(getDrawable(R.drawable.ic_check_circle));
        }
    }
    
    // ========== EVENT HANDLERS ==========
    
    private void onPaymentProofClick(PaymentProof paymentProof) {
        if (isSelectionMode) {
            toggleSelection(paymentProof.getId());
        } else {
            // Open payment proof view
            Intent intent = new Intent(this, PaymentProofViewActivity.class);
            intent.putExtra(PaymentProofViewActivity.EXTRA_TRANSACTION_ID, paymentProof.getTransactionId());
            startActivity(intent);
        }
    }
    
    private boolean onPaymentProofLongClick(PaymentProof paymentProof) {
        if (!isSelectionMode) {
            enterSelectionMode();
        }
        toggleSelection(paymentProof.getId());
        return true;
    }
    
    private void onSelectionChanged(List<Long> selectedIds) {
        selectedProofIds.clear();
        selectedProofIds.addAll(selectedIds);
        updateSelectionUI();
    }
    
    private void toggleSelection(long paymentProofId) {
        if (selectedProofIds.contains(paymentProofId)) {
            selectedProofIds.remove(paymentProofId);
        } else {
            selectedProofIds.add(paymentProofId);
        }
        
        adapter.setSelectedIds(selectedProofIds);
        updateSelectionUI();
    }
    
    // ========== DIALOGS ==========
    
    private void showDateRangeDialog() {
        Calendar calendar = Calendar.getInstance();
        
        // Start date picker
        DatePickerDialog startDatePicker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                Calendar startCal = Calendar.getInstance();
                startCal.set(year, month, dayOfMonth, 0, 0, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                filterStartDate = startCal.getTimeInMillis();
                
                // End date picker
                DatePickerDialog endDatePicker = new DatePickerDialog(this,
                    (view2, year2, month2, dayOfMonth2) -> {
                        Calendar endCal = Calendar.getInstance();
                        endCal.set(year2, month2, dayOfMonth2, 23, 59, 59);
                        endCal.set(Calendar.MILLISECOND, 999);
                        filterEndDate = endCal.getTimeInMillis();
                        
                        applyFilters();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                );
                
                endDatePicker.setTitle("Pilih Tanggal Akhir");
                endDatePicker.show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        startDatePicker.setTitle("Pilih Tanggal Mulai");
        startDatePicker.show();
    }
    
    private void showBulkActionsDialog() {
        if (selectedProofIds.isEmpty()) {
            Toast.makeText(this, "Pilih bukti pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] actions = {"Hapus", "Download", "Bagikan"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Aksi untuk " + selectedProofIds.size() + " item")
            .setItems(actions, (dialog, which) -> {
                switch (which) {
                    case 0: // Delete
                        confirmBulkDelete();
                        break;
                    case 1: // Export
                        performBulkExport();
                        break;
                    case 2: // Share
                        performBulkShare();
                        break;
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void confirmBulkDelete() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Bukti Pembayaran")
            .setMessage("Hapus " + selectedProofIds.size() + " bukti pembayaran? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus", (dialog, which) -> performBulkDelete())
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void performBulkDelete() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        paymentProofManager.bulkDeletePaymentProofs(selectedProofIds)
            .thenAccept(deletedCount -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    String message = deletedCount + " bukti pembayaran berhasil dihapus";
                    if (deletedCount < selectedProofIds.size()) {
                        message += " (" + (selectedProofIds.size() - deletedCount) + " gagal)";
                    }
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    exitSelectionMode();
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal menghapus bukti pembayaran", Toast.LENGTH_SHORT).show();
                });
                return null;
            });
    }
    
    private void performBulkExport() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Create export directory
        File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), 
                                 "PaymentProofs_" + System.currentTimeMillis());
        
        paymentProofManager.bulkExportPaymentProofs(selectedProofIds, exportDir.getAbsolutePath())
            .thenAccept(result -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (result != null && result.length >= 3) {
                        int successCount = result[0];
                        int failureCount = result[1];
                        int totalSize = result[2];
                        
                        String message = successCount + " file berhasil didownload";
                        if (failureCount > 0) {
                            message += " (" + failureCount + " gagal)";
                        }
                        message += "\nLokasi: " + exportDir.getAbsolutePath();
                        
                        new MaterialAlertDialogBuilder(this)
                            .setTitle("Download Selesai")
                            .setMessage(message)
                            .setPositiveButton("Buka Folder", (dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri uri = FileProvider.getUriForFile(this, 
                                    getPackageName() + ".fileprovider", exportDir);
                                intent.setDataAndType(uri, "resource/folder");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                
                                try {
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Tidak dapat membuka folder", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("OK", null)
                            .show();
                        
                        exitSelectionMode();
                    } else {
                        Toast.makeText(this, "Gagal mendownload file", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal mendownload file", Toast.LENGTH_SHORT).show();
                });
                return null;
            });
    }
    
    private void performBulkShare() {
        if (selectedProofIds.isEmpty()) {
            Toast.makeText(this, "Pilih bukti pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Get selected payment proofs
        List<PaymentProof> selectedProofs = new ArrayList<>();
        for (PaymentProof proof : filteredPaymentProofs) {
            if (selectedProofIds.contains(proof.getId())) {
                selectedProofs.add(proof);
            }
        }
        
        // Create temporary files for sharing
        paymentProofManager.bulkLoadPaymentProofPhotos(selectedProofs)
            .thenAccept(bitmaps -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (bitmaps.isEmpty()) {
                        Toast.makeText(this, "Tidak ada foto yang dapat dibagikan", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    try {
                        // Create cache directory for sharing
                        File cachePath = new File(getCacheDir(), "shared_images");
                        cachePath.mkdirs();
                        
                        ArrayList<Uri> imageUris = new ArrayList<>();
                        StringBuilder shareText = new StringBuilder("Bukti Pembayaran QRIS\n\n");
                        
                        for (int i = 0; i < bitmaps.size() && i < selectedProofs.size(); i++) {
                            PaymentProof proof = selectedProofs.get(i);
                            android.graphics.Bitmap bitmap = bitmaps.get(i);
                            
                            if (bitmap != null) {
                                // Create temporary file
                                String fileName = "bukti_pembayaran_" + proof.getTransactionId() + ".jpg";
                                File imageFile = new File(cachePath, fileName);
                                
                                java.io.FileOutputStream stream = new java.io.FileOutputStream(imageFile);
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream);
                                stream.close();
                                
                                // Get content URI
                                Uri contentUri = FileProvider.getUriForFile(this, 
                                    getPackageName() + ".fileprovider", imageFile);
                                imageUris.add(contentUri);
                                
                                // Add to share text
                                shareText.append("Transaksi #").append(proof.getTransactionId())
                                    .append(" - ").append(dateFormat.format(new Date(proof.getCaptureTimestamp())))
                                    .append("\n");
                            }
                        }
                        
                        if (imageUris.isEmpty()) {
                            Toast.makeText(this, "Tidak ada foto yang dapat dibagikan", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Create share intent for multiple files
                        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        shareIntent.setType("image/jpeg");
                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        startActivity(Intent.createChooser(shareIntent, 
                            "Bagikan " + imageUris.size() + " Bukti Pembayaran"));
                        
                        // Exit selection mode after sharing
                        exitSelectionMode();
                        
                    } catch (Exception e) {
                        android.util.Log.e("PaymentProofManagement", "Error sharing payment proofs", e);
                        Toast.makeText(this, "Gagal membagikan foto", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat foto untuk dibagikan", Toast.LENGTH_SHORT).show();
                });
                return null;
            });
    }
    
    private void clearAllFilters() {
        filterStartDate = 0;
        filterEndDate = 0;
        searchQuery = "";
        binding.etSearch.setText("");
        applyFilters();
    }
    
    private void selectAllItems() {
        selectedProofIds.clear();
        for (PaymentProof proof : filteredPaymentProofs) {
            selectedProofIds.add(proof.getId());
        }
        adapter.setSelectedIds(selectedProofIds);
        updateSelectionUI();
    }
    
    @Override
    public void onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentProofManager != null) {
            paymentProofManager.shutdown();
        }
    }
}
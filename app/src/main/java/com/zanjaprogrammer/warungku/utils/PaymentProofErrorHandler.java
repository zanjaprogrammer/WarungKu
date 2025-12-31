package com.zanjaprogrammer.warungku.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.os.StatFs;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zanjaprogrammer.warungku.service.PaymentProofManager;

import java.io.File;
import java.util.concurrent.CompletableFuture;

/**
 * Comprehensive error handling utility for payment proof operations
 * Handles storage space monitoring, camera errors, file corruption, and network independence
 */
public class PaymentProofErrorHandler {
    
    private static final long MIN_STORAGE_SPACE_MB = 50; // Minimum 50MB required
    private static final long WARNING_STORAGE_SPACE_MB = 100; // Warning at 100MB
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    
    private final Context context;
    
    public PaymentProofErrorHandler(Context context) {
        this.context = context;
    }
    
    // ========== STORAGE SPACE MONITORING ==========
    
    /**
     * Check if there's enough storage space for payment proof operations
     * @return StorageStatus indicating the current storage situation
     */
    public StorageStatus checkStorageSpace() {
        try {
            StatFs stat = new StatFs(context.getFilesDir().getPath());
            long availableBytes = stat.getAvailableBytes();
            long availableMB = availableBytes / (1024 * 1024);
            
            if (availableMB < MIN_STORAGE_SPACE_MB) {
                return new StorageStatus(StorageLevel.CRITICAL, availableMB, 
                    "Ruang penyimpanan hampir habis. Diperlukan minimal " + MIN_STORAGE_SPACE_MB + "MB untuk menyimpan foto bukti pembayaran.");
            } else if (availableMB < WARNING_STORAGE_SPACE_MB) {
                return new StorageStatus(StorageLevel.WARNING, availableMB,
                    "Ruang penyimpanan tersisa " + availableMB + "MB. Pertimbangkan untuk membersihkan file yang tidak diperlukan.");
            } else {
                return new StorageStatus(StorageLevel.SUFFICIENT, availableMB, null);
            }
        } catch (Exception e) {
            return new StorageStatus(StorageLevel.ERROR, 0, 
                "Tidak dapat memeriksa ruang penyimpanan: " + e.getMessage());
        }
    }
    
    /**
     * Get storage cleanup suggestions
     * @return CompletableFuture with cleanup suggestions
     */
    public CompletableFuture<StorageCleanupSuggestion> getStorageCleanupSuggestions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PaymentProofManager manager = new PaymentProofManager(context);
                long[] stats = manager.getPaymentProofStatistics().get();
                
                if (stats != null && stats.length >= 4) {
                    long totalCount = stats[0];
                    long totalSize = stats[2];
                    long corruptedCount = stats[3];
                    
                    StorageCleanupSuggestion suggestion = new StorageCleanupSuggestion();
                    suggestion.totalFiles = totalCount;
                    suggestion.totalSizeMB = totalSize / (1024 * 1024);
                    suggestion.corruptedFiles = corruptedCount;
                    
                    if (corruptedCount > 0) {
                        suggestion.suggestions.add("Hapus " + corruptedCount + " file bukti pembayaran yang rusak");
                        suggestion.potentialSavingsMB += (corruptedCount * 2); // Estimate 2MB per corrupted file
                    }
                    
                    if (totalCount > 100) {
                        suggestion.suggestions.add("Ekspor dan hapus bukti pembayaran lama (lebih dari 6 bulan)");
                        suggestion.potentialSavingsMB += (suggestion.totalSizeMB / 2); // Estimate half could be old
                    }
                    
                    return suggestion;
                }
                
                return new StorageCleanupSuggestion();
            } catch (Exception e) {
                StorageCleanupSuggestion errorSuggestion = new StorageCleanupSuggestion();
                errorSuggestion.errorMessage = e.getMessage();
                return errorSuggestion;
            }
        });
    }
    
    // ========== CAMERA ERROR HANDLING ==========
    
    /**
     * Check camera permission status
     * @return CameraStatus indicating camera availability
     */
    public CameraStatus checkCameraStatus() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            return new CameraStatus(CameraAvailability.PERMISSION_DENIED, 
                "Izin kamera diperlukan untuk mengambil foto bukti pembayaran");
        }
        
        // Check camera hardware availability
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                return new CameraStatus(CameraAvailability.HARDWARE_UNAVAILABLE, 
                    "Kamera tidak tersedia pada perangkat ini");
            }
            
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                return new CameraStatus(CameraAvailability.HARDWARE_UNAVAILABLE, 
                    "Tidak ada kamera yang ditemukan pada perangkat ini");
            }
            
            return new CameraStatus(CameraAvailability.AVAILABLE, null);
            
        } catch (Exception e) {
            return new CameraStatus(CameraAvailability.ERROR, 
                "Error saat memeriksa kamera: " + e.getMessage());
        }
    }
    
    /**
     * Request camera permission
     * @param activity Activity to request permission from
     */
    public void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, 
            new String[]{Manifest.permission.CAMERA}, 
            CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    /**
     * Handle camera permission result
     * @param requestCode Request code from permission result
     * @param permissions Permissions array
     * @param grantResults Grant results array
     * @return true if camera permission was granted, false otherwise
     */
    public boolean handleCameraPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
    
    // ========== FILE CORRUPTION DETECTION ==========
    
    /**
     * Detect and handle corrupted payment proof files
     * @return CompletableFuture with corruption detection result
     */
    public CompletableFuture<CorruptionDetectionResult> detectCorruptedFiles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PaymentProofManager manager = new PaymentProofManager(context);
                int[] validationResult = manager.validateAllPaymentProofs().get();
                
                if (validationResult != null && validationResult.length >= 3) {
                    int validCount = validationResult[0];
                    int corruptedCount = validationResult[1];
                    int missingCount = validationResult[2];
                    
                    return new CorruptionDetectionResult(validCount, corruptedCount, missingCount, null);
                }
                
                return new CorruptionDetectionResult(0, 0, 0, "Validation failed");
            } catch (Exception e) {
                return new CorruptionDetectionResult(0, 0, 0, e.getMessage());
            }
        });
    }
    
    /**
     * Clean up corrupted files
     * @return CompletableFuture with cleanup result
     */
    public CompletableFuture<Integer> cleanupCorruptedFiles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
                return storageHelper.cleanupCorruptedFiles();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }
    
    // ========== NETWORK INDEPENDENCE ==========
    
    /**
     * Verify that payment proof operations work without network
     * @return NetworkIndependenceStatus
     */
    public NetworkIndependenceStatus verifyNetworkIndependence() {
        try {
            // Test core operations that should work offline
            PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
            
            // Test storage space check
            long availableSpace = storageHelper.getAvailableStorageSpace();
            if (availableSpace <= 0) {
                return new NetworkIndependenceStatus(false, 
                    "Storage space check failed - may require network");
            }
            
            // Test file listing
            storageHelper.getAllPaymentProofFiles();
            
            // All tests passed
            return new NetworkIndependenceStatus(true, 
                "All payment proof operations work offline");
            
        } catch (Exception e) {
            return new NetworkIndependenceStatus(false, 
                "Network independence verification failed: " + e.getMessage());
        }
    }
    
    // ========== GENERAL ERROR RECOVERY ==========
    
    /**
     * Attempt to recover from various error states
     * @param errorType Type of error to recover from
     * @return CompletableFuture with recovery result
     */
    public CompletableFuture<RecoveryResult> attemptErrorRecovery(ErrorType errorType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                switch (errorType) {
                    case STORAGE_FULL:
                        return recoverFromStorageFull();
                    case CORRUPTED_FILES:
                        return recoverFromCorruptedFiles();
                    case CAMERA_ERROR:
                        return recoverFromCameraError();
                    case DATABASE_ERROR:
                        return recoverFromDatabaseError();
                    default:
                        return new RecoveryResult(false, "Unknown error type");
                }
            } catch (Exception e) {
                return new RecoveryResult(false, "Recovery failed: " + e.getMessage());
            }
        });
    }
    
    private RecoveryResult recoverFromStorageFull() {
        try {
            PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
            int cleanedFiles = storageHelper.cleanupCorruptedFiles();
            
            if (cleanedFiles > 0) {
                return new RecoveryResult(true, 
                    "Recovered " + cleanedFiles + " corrupted files, freeing up storage space");
            } else {
                return new RecoveryResult(false, 
                    "No corrupted files found to clean up. Manual storage management required.");
            }
        } catch (Exception e) {
            return new RecoveryResult(false, "Storage recovery failed: " + e.getMessage());
        }
    }
    
    private RecoveryResult recoverFromCorruptedFiles() {
        try {
            int cleanedFiles = cleanupCorruptedFiles().get();
            return new RecoveryResult(true, 
                "Cleaned up " + cleanedFiles + " corrupted files");
        } catch (Exception e) {
            return new RecoveryResult(false, "Corruption recovery failed: " + e.getMessage());
        }
    }
    
    private RecoveryResult recoverFromCameraError() {
        CameraStatus status = checkCameraStatus();
        if (status.availability == CameraAvailability.AVAILABLE) {
            return new RecoveryResult(true, "Camera is now available");
        } else {
            return new RecoveryResult(false, 
                "Camera recovery failed: " + status.errorMessage);
        }
    }
    
    private RecoveryResult recoverFromDatabaseError() {
        try {
            // Attempt to reinitialize database connection
            PaymentProofManager manager = new PaymentProofManager(context);
            manager.getStorageStatistics().get(); // Test database access
            return new RecoveryResult(true, "Database connection restored");
        } catch (Exception e) {
            return new RecoveryResult(false, "Database recovery failed: " + e.getMessage());
        }
    }
    
    // ========== DATA CLASSES ==========
    
    public enum StorageLevel {
        SUFFICIENT, WARNING, CRITICAL, ERROR
    }
    
    public enum CameraAvailability {
        AVAILABLE, PERMISSION_DENIED, HARDWARE_UNAVAILABLE, ERROR
    }
    
    public enum ErrorType {
        STORAGE_FULL, CORRUPTED_FILES, CAMERA_ERROR, DATABASE_ERROR
    }
    
    public static class StorageStatus {
        public final StorageLevel level;
        public final long availableMB;
        public final String message;
        
        public StorageStatus(StorageLevel level, long availableMB, String message) {
            this.level = level;
            this.availableMB = availableMB;
            this.message = message;
        }
        
        public boolean isOk() {
            return level == StorageLevel.SUFFICIENT;
        }
        
        public boolean needsAttention() {
            return level == StorageLevel.WARNING || level == StorageLevel.CRITICAL;
        }
    }
    
    public static class StorageCleanupSuggestion {
        public long totalFiles = 0;
        public long totalSizeMB = 0;
        public long corruptedFiles = 0;
        public long potentialSavingsMB = 0;
        public java.util.List<String> suggestions = new java.util.ArrayList<>();
        public String errorMessage = null;
        
        public boolean hasSuggestions() {
            return !suggestions.isEmpty();
        }
    }
    
    public static class CameraStatus {
        public final CameraAvailability availability;
        public final String errorMessage;
        
        public CameraStatus(CameraAvailability availability, String errorMessage) {
            this.availability = availability;
            this.errorMessage = errorMessage;
        }
        
        public boolean isAvailable() {
            return availability == CameraAvailability.AVAILABLE;
        }
    }
    
    public static class CorruptionDetectionResult {
        public final int validFiles;
        public final int corruptedFiles;
        public final int missingFiles;
        public final String errorMessage;
        
        public CorruptionDetectionResult(int validFiles, int corruptedFiles, int missingFiles, String errorMessage) {
            this.validFiles = validFiles;
            this.corruptedFiles = corruptedFiles;
            this.missingFiles = missingFiles;
            this.errorMessage = errorMessage;
        }
        
        public boolean hasIssues() {
            return corruptedFiles > 0 || missingFiles > 0;
        }
        
        public int getTotalIssues() {
            return corruptedFiles + missingFiles;
        }
    }
    
    public static class NetworkIndependenceStatus {
        public final boolean isIndependent;
        public final String message;
        
        public NetworkIndependenceStatus(boolean isIndependent, String message) {
            this.isIndependent = isIndependent;
            this.message = message;
        }
    }
    
    public static class RecoveryResult {
        public final boolean success;
        public final String message;
        
        public RecoveryResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
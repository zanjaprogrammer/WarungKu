package com.zanjaprogrammer.warungku.service;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;

import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.dao.PaymentProofDao;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.utils.ImageCompressionUtil;
import com.zanjaprogrammer.warungku.utils.PaymentProofStorageHelper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service class for managing payment proof operations
 * Handles capture, storage, retrieval, and management of payment proof photos
 */
public class PaymentProofManager {
    
    private final Context context;
    private final PaymentProofDao paymentProofDao;
    private final CashFlowDao cashFlowDao;
    private final PaymentProofStorageHelper storageHelper;
    private final ExecutorService executorService;
    
    public PaymentProofManager(Context context) {
        this.context = context;
        AppDatabase database = AppDatabase.getDatabase(context);
        this.paymentProofDao = database.paymentProofDao();
        this.cashFlowDao = database.cashFlowDao();
        this.storageHelper = new PaymentProofStorageHelper(context);
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Capture payment proof for a transaction
     * This method initiates the payment proof capture process
     * @param transactionId The ID of the transaction
     * @param paymentMethod The payment method used (e.g., "QRIS")
     * @return CompletableFuture with the created PaymentProof or null if failed
     */
    public CompletableFuture<PaymentProof> capturePaymentProof(long transactionId, String paymentMethod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate transaction exists
                CashFlow transaction = cashFlowDao.getCashFlowById(transactionId);
                if (transaction == null) {
                    return null;
                }
                
                // Check if payment proof already exists for this transaction
                PaymentProof existingProof = paymentProofDao.getPaymentProofByTransactionIdSync(transactionId);
                if (existingProof != null) {
                    return existingProof; // Return existing proof
                }
                
                // Create placeholder PaymentProof entry (will be updated when photo is saved)
                PaymentProof paymentProof = new PaymentProof();
                paymentProof.setTransactionId(transactionId);
                paymentProof.setPaymentMethod(paymentMethod != null ? paymentMethod : "QRIS");
                paymentProof.setCaptureTimestamp(System.currentTimeMillis());
                paymentProof.setCorrupted(false);
                
                return paymentProof;
                
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }
    
    /**
     * Save payment proof photo with compression and validation
     * @param transactionId The ID of the transaction
     * @param photo The bitmap photo to save
     * @return CompletableFuture with the saved PaymentProof or null if failed
     */
    public CompletableFuture<PaymentProof> savePaymentProof(long transactionId, Bitmap photo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate inputs
                if (photo == null || transactionId <= 0) {
                    return null;
                }
                
                // Validate image is readable and meets constraints
                if (!ImageCompressionUtil.isImageReadable(photo)) {
                    return null;
                }
                
                if (!ImageCompressionUtil.validateImageConstraints(photo)) {
                    return null;
                }
                
                // Compress image for storage
                Bitmap compressedPhoto = ImageCompressionUtil.compressImage(photo);
                if (compressedPhoto == null) {
                    return null;
                }
                
                // Generate unique filename
                String filename = ImageCompressionUtil.generateUniqueFilename(transactionId);
                
                // Save photo to storage
                String filePath = storageHelper.savePhotoToInternalStorage(compressedPhoto, filename);
                if (filePath == null) {
                    // Clean up compressed bitmap if save failed
                    if (compressedPhoto != photo) {
                        compressedPhoto.recycle();
                    }
                    return null;
                }
                
                // Get file size
                long fileSize = new java.io.File(filePath).length();
                
                // Create PaymentProof entity
                PaymentProof paymentProof = new PaymentProof();
                paymentProof.setTransactionId(transactionId);
                paymentProof.setFilePath(filePath);
                paymentProof.setFileName(filename);
                paymentProof.setCaptureTimestamp(System.currentTimeMillis());
                paymentProof.setFileSize(fileSize);
                paymentProof.setCorrupted(false);
                paymentProof.setPaymentMethod("QRIS");
                
                // Save to database
                long paymentProofId = paymentProofDao.insertPaymentProof(paymentProof);
                paymentProof.setId(paymentProofId);
                
                // Update transaction to indicate it has payment proof
                CashFlow transaction = cashFlowDao.getCashFlowById(transactionId);
                if (transaction != null) {
                    transaction.setHasPaymentProof(true);
                    transaction.setPaymentMethod("QRIS");
                    cashFlowDao.update(transaction);
                }
                
                // Clean up compressed bitmap if it's different from original
                if (compressedPhoto != photo) {
                    compressedPhoto.recycle();
                }
                
                return paymentProof;
                
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }
    
    /**
     * Get payment proof by transaction ID
     * @param transactionId The transaction ID to search for
     * @return CompletableFuture with PaymentProof or null if not found
     */
    public CompletableFuture<PaymentProof> getPaymentProofByTransactionId(long transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return paymentProofDao.getPaymentProofByTransactionIdSync(transactionId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }
    
    /**
     * Delete payment proof with file and database cleanup
     * @param paymentProofId The ID of the payment proof to delete
     * @return CompletableFuture with true if successful, false otherwise
     */
    public CompletableFuture<Boolean> deletePaymentProof(long paymentProofId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get payment proof details
                PaymentProof paymentProof = paymentProofDao.getPaymentProofByIdSync(paymentProofId);
                if (paymentProof == null) {
                    return false;
                }
                
                // Delete physical file
                boolean fileDeleted = true;
                if (paymentProof.getFilePath() != null) {
                    fileDeleted = storageHelper.deletePhotoFile(paymentProof.getFilePath());
                }
                
                // Delete from database
                paymentProofDao.deletePaymentProofById(paymentProofId);
                
                // Update transaction to indicate no payment proof
                CashFlow transaction = cashFlowDao.getCashFlowById(paymentProof.getTransactionId());
                if (transaction != null) {
                    transaction.setHasPaymentProof(false);
                    // Keep payment method as is, just remove proof indicator
                    cashFlowDao.update(transaction);
                }
                
                return fileDeleted;
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }, executorService);
    }
    
    /**
     * Load payment proof photo as bitmap
     * @param paymentProof The PaymentProof entity
     * @return CompletableFuture with Bitmap or null if failed
     */
    public CompletableFuture<Bitmap> loadPaymentProofPhoto(PaymentProof paymentProof) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (paymentProof == null || paymentProof.getFilePath() == null) {
                    return null;
                }
                
                Bitmap bitmap = storageHelper.loadPhotoFromInternalStorage(paymentProof.getFilePath());
                
                // If bitmap is null, mark as corrupted
                if (bitmap == null && !paymentProof.isCorrupted()) {
                    paymentProofDao.markAsCorrupted(paymentProof.getId());
                }
                
                return bitmap;
                
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executorService);
    }
    
    /**
     * Validate payment proof file integrity
     * @param paymentProof The PaymentProof to validate
     * @return CompletableFuture with true if valid, false if corrupted
     */
    public CompletableFuture<Boolean> validatePaymentProofIntegrity(PaymentProof paymentProof) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (paymentProof == null || paymentProof.getFilePath() == null) {
                    return false;
                }
                
                // Check if file exists
                java.io.File file = new java.io.File(paymentProof.getFilePath());
                if (!file.exists()) {
                    paymentProofDao.markAsCorrupted(paymentProof.getId());
                    return false;
                }
                
                // Try to load the image
                Bitmap bitmap = storageHelper.loadPhotoFromInternalStorage(paymentProof.getFilePath());
                if (bitmap == null) {
                    paymentProofDao.markAsCorrupted(paymentProof.getId());
                    return false;
                }
                
                // Validate image is readable
                boolean isValid = ImageCompressionUtil.isImageReadable(bitmap);
                bitmap.recycle(); // Clean up memory
                
                if (!isValid) {
                    paymentProofDao.markAsCorrupted(paymentProof.getId());
                }
                
                return isValid;
                
            } catch (Exception e) {
                e.printStackTrace();
                if (paymentProof != null) {
                    paymentProofDao.markAsCorrupted(paymentProof.getId());
                }
                return false;
            }
        }, executorService);
    }
    
    /**
     * Get storage statistics
     * @return CompletableFuture with storage info as array [totalFiles, totalSizeBytes, availableSpaceBytes]
     */
    public CompletableFuture<long[]> getStorageStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> allFiles = storageHelper.getAllPaymentProofFiles();
                long totalFiles = allFiles.size();
                long totalSize = storageHelper.getTotalPaymentProofSize();
                long availableSpace = storageHelper.getAvailableStorageSpace();
                
                return new long[]{totalFiles, totalSize, availableSpace};
                
            } catch (Exception e) {
                e.printStackTrace();
                return new long[]{0, 0, 0};
            }
        }, executorService);
    }
    
    /**
     * Cleanup corrupted files and update database
     * @return CompletableFuture with number of files cleaned up
     */
    public CompletableFuture<Integer> cleanupCorruptedFiles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return storageHelper.cleanupCorruptedFiles();
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }, executorService);
    }
    
    /**
     * Shutdown the executor service
     * Call this when the manager is no longer needed
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // ========== SEARCH AND MANAGEMENT OPERATIONS ==========
    
    /**
     * Get payment proofs by date range
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @return LiveData list of PaymentProof objects in the date range
     */
    public LiveData<List<PaymentProof>> getPaymentProofsByDateRange(long startDate, long endDate) {
        return paymentProofDao.getPaymentProofsByDateRange(startDate, endDate);
    }
    
    /**
     * Search payment proofs with filters
     * @param query Search query (can be transaction description, payment method, etc.)
     * @return CompletableFuture with filtered list of PaymentProof objects
     */
    public CompletableFuture<List<PaymentProof>> searchPaymentProofs(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (query == null || query.trim().isEmpty()) {
                    // Return all payment proofs if no query
                    return paymentProofDao.getAllPaymentProofsSync();
                }
                
                String searchQuery = "%" + query.trim().toLowerCase() + "%";
                
                // Search by payment method, transaction description, or file name
                List<PaymentProof> results = paymentProofDao.searchPaymentProofs(searchQuery);
                
                return results;
                
            } catch (Exception e) {
                e.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }, executorService);
    }
    
    /**
     * Get payment proofs by payment method
     * @param paymentMethod The payment method to filter by (e.g., "QRIS")
     * @return LiveData list of PaymentProof objects for the specified payment method
     */
    public LiveData<List<PaymentProof>> getPaymentProofsByMethod(String paymentMethod) {
        return paymentProofDao.getPaymentProofsByMethod(paymentMethod);
    }
    
    /**
     * Get all payment proofs
     * @return LiveData list of all PaymentProof objects
     */
    public LiveData<List<PaymentProof>> getAllPaymentProofs() {
        return paymentProofDao.getAllPaymentProofs();
    }
    
    /**
     * Bulk delete payment proofs
     * @param paymentProofIds List of payment proof IDs to delete
     * @return CompletableFuture with number of successfully deleted proofs
     */
    public CompletableFuture<Integer> bulkDeletePaymentProofs(List<Long> paymentProofIds) {
        return CompletableFuture.supplyAsync(() -> {
            int deletedCount = 0;
            
            try {
                for (Long paymentProofId : paymentProofIds) {
                    if (paymentProofId != null) {
                        Boolean deleted = deletePaymentProof(paymentProofId).get();
                        if (deleted != null && deleted) {
                            deletedCount++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return deletedCount;
        }, executorService);
    }
    
    /**
     * Bulk export payment proofs
     * @param paymentProofIds List of payment proof IDs to export
     * @param exportDirectory Directory to export files to
     * @return CompletableFuture with export result info [successCount, failureCount, totalSize]
     */
    public CompletableFuture<int[]> bulkExportPaymentProofs(List<Long> paymentProofIds, String exportDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            int successCount = 0;
            int failureCount = 0;
            long totalSize = 0;
            
            try {
                java.io.File exportDir = new java.io.File(exportDirectory);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                for (Long paymentProofId : paymentProofIds) {
                    try {
                        PaymentProof proof = paymentProofDao.getPaymentProofByIdSync(paymentProofId);
                        if (proof != null && proof.getFilePath() != null) {
                            java.io.File sourceFile = new java.io.File(proof.getFilePath());
                            java.io.File destFile = new java.io.File(exportDir, proof.getFileName());
                            
                            if (copyFile(sourceFile, destFile)) {
                                successCount++;
                                totalSize += sourceFile.length();
                            } else {
                                failureCount++;
                            }
                        } else {
                            failureCount++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        failureCount++;
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                failureCount = paymentProofIds.size();
            }
            
            return new int[]{successCount, failureCount, (int) totalSize};
        }, executorService);
    }
    
    /**
     * Get payment proof statistics
     * @return CompletableFuture with statistics [totalCount, qrisCount, totalSizeBytes, corruptedCount]
     */
    public CompletableFuture<long[]> getPaymentProofStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> allProofs = paymentProofDao.getAllPaymentProofsSync();
                
                long totalCount = allProofs.size();
                long qrisCount = 0;
                long totalSize = 0;
                long corruptedCount = 0;
                
                for (PaymentProof proof : allProofs) {
                    if ("QRIS".equals(proof.getPaymentMethod())) {
                        qrisCount++;
                    }
                    totalSize += proof.getFileSize();
                    if (proof.isCorrupted()) {
                        corruptedCount++;
                    }
                }
                
                return new long[]{totalCount, qrisCount, totalSize, corruptedCount};
                
            } catch (Exception e) {
                e.printStackTrace();
                return new long[]{0, 0, 0, 0};
            }
        }, executorService);
    }
    
    /**
     * Filter payment proofs by multiple criteria
     * @param startDate Start date filter (0 to ignore)
     * @param endDate End date filter (0 to ignore)
     * @param paymentMethod Payment method filter (null to ignore)
     * @param minFileSize Minimum file size filter (0 to ignore)
     * @param maxFileSize Maximum file size filter (0 to ignore)
     * @param excludeCorrupted Whether to exclude corrupted files
     * @return CompletableFuture with filtered list of PaymentProof objects
     */
    public CompletableFuture<List<PaymentProof>> filterPaymentProofs(
            long startDate, long endDate, String paymentMethod, 
            long minFileSize, long maxFileSize, boolean excludeCorrupted) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> allProofs = paymentProofDao.getAllPaymentProofsSync();
                List<PaymentProof> filteredProofs = new java.util.ArrayList<>();
                
                for (PaymentProof proof : allProofs) {
                    // Date range filter
                    if (startDate > 0 && proof.getCaptureTimestamp() < startDate) {
                        continue;
                    }
                    if (endDate > 0 && proof.getCaptureTimestamp() > endDate) {
                        continue;
                    }
                    
                    // Payment method filter
                    if (paymentMethod != null && !paymentMethod.equals(proof.getPaymentMethod())) {
                        continue;
                    }
                    
                    // File size filters
                    if (minFileSize > 0 && proof.getFileSize() < minFileSize) {
                        continue;
                    }
                    if (maxFileSize > 0 && proof.getFileSize() > maxFileSize) {
                        continue;
                    }
                    
                    // Corrupted filter
                    if (excludeCorrupted && proof.isCorrupted()) {
                        continue;
                    }
                    
                    filteredProofs.add(proof);
                }
                
                return filteredProofs;
                
            } catch (Exception e) {
                e.printStackTrace();
                return new java.util.ArrayList<>();
            }
        }, executorService);
    }
    
    /**
     * Validate all payment proof files and mark corrupted ones
     * @return CompletableFuture with validation result [validCount, corruptedCount, missingCount]
     */
    public CompletableFuture<int[]> validateAllPaymentProofs() {
        return CompletableFuture.supplyAsync(() -> {
            int validCount = 0;
            int corruptedCount = 0;
            int missingCount = 0;
            
            try {
                List<PaymentProof> allProofs = paymentProofDao.getAllPaymentProofsSync();
                
                for (PaymentProof proof : allProofs) {
                    if (proof.getFilePath() == null) {
                        missingCount++;
                        paymentProofDao.markAsCorrupted(proof.getId());
                        continue;
                    }
                    
                    java.io.File file = new java.io.File(proof.getFilePath());
                    if (!file.exists()) {
                        missingCount++;
                        paymentProofDao.markAsCorrupted(proof.getId());
                        continue;
                    }
                    
                    Bitmap bitmap = storageHelper.loadPhotoFromInternalStorage(proof.getFilePath());
                    if (bitmap == null || !ImageCompressionUtil.isImageReadable(bitmap)) {
                        corruptedCount++;
                        paymentProofDao.markAsCorrupted(proof.getId());
                    } else {
                        validCount++;
                        bitmap.recycle();
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return new int[]{validCount, corruptedCount, missingCount};
        }, executorService);
    }
    
    /**
     * Bulk load payment proof photos for sharing
     * @param paymentProofs List of PaymentProof objects to load photos for
     * @return CompletableFuture with list of Bitmap objects (null entries for failed loads)
     */
    public CompletableFuture<List<Bitmap>> bulkLoadPaymentProofPhotos(List<PaymentProof> paymentProofs) {
        return CompletableFuture.supplyAsync(() -> {
            List<Bitmap> bitmaps = new java.util.ArrayList<>();
            
            try {
                for (PaymentProof proof : paymentProofs) {
                    try {
                        if (proof.isCorrupted() || proof.getFilePath() == null) {
                            bitmaps.add(null);
                            continue;
                        }
                        
                        java.io.File file = new java.io.File(proof.getFilePath());
                        if (!file.exists()) {
                            bitmaps.add(null);
                            continue;
                        }
                        
                        Bitmap bitmap = storageHelper.loadPhotoFromInternalStorage(proof.getFilePath());
                        if (bitmap != null && ImageCompressionUtil.isImageReadable(bitmap)) {
                            bitmaps.add(bitmap);
                        } else {
                            bitmaps.add(null);
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        bitmaps.add(null);
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return bitmaps;
        }, executorService);
    }
    
    /**
     * Helper method to copy files
     * @param source Source file
     * @param destination Destination file
     * @return true if successful, false otherwise
     */
    private boolean copyFile(java.io.File source, java.io.File destination) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(destination)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
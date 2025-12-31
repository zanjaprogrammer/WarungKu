package com.zanjaprogrammer.warungku.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing payment proof photo storage operations
 * Handles saving, loading, and deleting payment proof photos in internal storage
 */
public class PaymentProofStorageHelper {
    
    private static final String PAYMENT_PROOF_FOLDER = "payment_proofs";
    private static final long MIN_STORAGE_SPACE_MB = 50; // Minimum 50MB required
    private static final int JPEG_QUALITY = 85; // Quality for JPEG compression
    
    private final Context context;
    private final File paymentProofDir;
    
    public PaymentProofStorageHelper(Context context) {
        this.context = context;
        this.paymentProofDir = new File(context.getFilesDir(), PAYMENT_PROOF_FOLDER);
        
        // Create payment proof directory if it doesn't exist
        if (!paymentProofDir.exists()) {
            paymentProofDir.mkdirs();
        }
    }
    
    /**
     * Save a photo bitmap to internal storage with compression
     * @param photo The bitmap to save
     * @param filename The filename to use (should include .jpg extension)
     * @return The full file path if successful, null if failed
     */
    public String savePhotoToInternalStorage(Bitmap photo, String filename) {
        if (photo == null || filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        // Check available storage space
        if (!hasEnoughStorageSpace()) {
            return null;
        }
        
        File photoFile = new File(paymentProofDir, filename);
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(photoFile);
            
            // Compress and save as JPEG
            boolean success = photo.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            
            if (success) {
                return photoFile.getAbsolutePath();
            } else {
                // Delete file if compression failed
                if (photoFile.exists()) {
                    photoFile.delete();
                }
                return null;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            // Clean up file if it was created
            if (photoFile.exists()) {
                photoFile.delete();
            }
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Load a photo bitmap from internal storage
     * @param filepath The full file path to load
     * @return The bitmap if successful, null if failed or file doesn't exist
     */
    public Bitmap loadPhotoFromInternalStorage(String filepath) {
        if (filepath == null || filepath.trim().isEmpty()) {
            return null;
        }
        
        File photoFile = new File(filepath);
        if (!photoFile.exists() || !photoFile.canRead()) {
            return null;
        }
        
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(photoFile);
            return BitmapFactory.decodeStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Delete a photo file from storage
     * @param filepath The full file path to delete
     * @return true if successfully deleted, false otherwise
     */
    public boolean deletePhotoFile(String filepath) {
        if (filepath == null || filepath.trim().isEmpty()) {
            return false;
        }
        
        File photoFile = new File(filepath);
        if (!photoFile.exists()) {
            return true; // File doesn't exist, consider it deleted
        }
        
        // Ensure the file is within our payment proof directory for security
        if (!isFileInPaymentProofDirectory(photoFile)) {
            return false;
        }
        
        return photoFile.delete();
    }
    
    /**
     * Get available storage space in bytes
     * @return Available storage space in bytes
     */
    public long getAvailableStorageSpace() {
        try {
            StatFs stat = new StatFs(context.getFilesDir().getPath());
            return stat.getAvailableBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Check if there's enough storage space for saving photos
     * @return true if there's enough space, false otherwise
     */
    public boolean hasEnoughStorageSpace() {
        long availableBytes = getAvailableStorageSpace();
        long requiredBytes = MIN_STORAGE_SPACE_MB * 1024 * 1024; // Convert MB to bytes
        return availableBytes > requiredBytes;
    }
    
    /**
     * Get total size of all payment proof files in bytes
     * @return Total size in bytes
     */
    public long getTotalPaymentProofSize() {
        if (!paymentProofDir.exists()) {
            return 0;
        }
        
        long totalSize = 0;
        File[] files = paymentProofDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    totalSize += file.length();
                }
            }
        }
        return totalSize;
    }
    
    /**
     * Get list of all payment proof file paths
     * @return List of file paths
     */
    public List<String> getAllPaymentProofFiles() {
        List<String> filePaths = new ArrayList<>();
        
        if (!paymentProofDir.exists()) {
            return filePaths;
        }
        
        File[] files = paymentProofDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file)) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        
        return filePaths;
    }
    
    /**
     * Clean up corrupted or invalid image files
     * @return Number of files cleaned up
     */
    public int cleanupCorruptedFiles() {
        int cleanedCount = 0;
        
        if (!paymentProofDir.exists()) {
            return cleanedCount;
        }
        
        File[] files = paymentProofDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && isImageFile(file)) {
                    // Try to load the image to check if it's corrupted
                    Bitmap bitmap = loadPhotoFromInternalStorage(file.getAbsolutePath());
                    if (bitmap == null) {
                        // File is corrupted, delete it
                        if (file.delete()) {
                            cleanedCount++;
                        }
                    } else {
                        // Recycle bitmap to free memory
                        bitmap.recycle();
                    }
                }
            }
        }
        
        return cleanedCount;
    }
    
    /**
     * Check if a file is within the payment proof directory (security check)
     * @param file The file to check
     * @return true if file is in payment proof directory, false otherwise
     */
    private boolean isFileInPaymentProofDirectory(File file) {
        try {
            String paymentProofPath = paymentProofDir.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            return filePath.startsWith(paymentProofPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a file is an image file based on extension
     * @param file The file to check
     * @return true if it's an image file, false otherwise
     */
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }
    
    /**
     * Restore a photo from backup to the payment proof directory
     * @param backupPhotoFile The backup photo file (temporary)
     * @param originalFileName The original filename to restore
     * @return The restored file path if successful, null if failed
     */
    public String restorePhotoFromBackup(File backupPhotoFile, String originalFileName) {
        if (backupPhotoFile == null || !backupPhotoFile.exists() || 
            originalFileName == null || originalFileName.trim().isEmpty()) {
            return null;
        }
        
        // Check available storage space
        if (!hasEnoughStorageSpace()) {
            return null;
        }
        
        File targetFile = new File(paymentProofDir, originalFileName);
        
        try {
            // Copy backup file to payment proof directory
            FileInputStream fis = new FileInputStream(backupPhotoFile);
            FileOutputStream fos = new FileOutputStream(targetFile);
            
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            fis.close();
            fos.close();
            
            // Verify the restored file is valid
            Bitmap testBitmap = loadPhotoFromInternalStorage(targetFile.getAbsolutePath());
            if (testBitmap != null) {
                testBitmap.recycle();
                return targetFile.getAbsolutePath();
            } else {
                // Delete invalid file
                targetFile.delete();
                return null;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            // Clean up target file if it was created
            if (targetFile.exists()) {
                targetFile.delete();
            }
            return null;
        }
    }
    
    /**
     * Get the expected file path for a payment proof photo
     * @param filename The filename
     * @return The full file path
     */
    public String getPaymentProofPath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        return new File(paymentProofDir, filename).getAbsolutePath();
    }
}
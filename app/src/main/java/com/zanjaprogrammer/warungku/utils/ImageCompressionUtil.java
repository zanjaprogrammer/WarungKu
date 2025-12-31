package com.zanjaprogrammer.warungku.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for image compression and validation operations
 * Handles image compression, filename generation, and image validation
 */
public class ImageCompressionUtil {
    
    // Default compression settings
    private static final int DEFAULT_MAX_WIDTH = 1024;
    private static final int DEFAULT_MAX_HEIGHT = 1024;
    private static final int DEFAULT_QUALITY = 85;
    
    // Minimum image dimensions for validation
    private static final int MIN_WIDTH = 100;
    private static final int MIN_HEIGHT = 100;
    
    // Maximum file size in bytes (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * Compress an image bitmap with specified dimensions and quality
     * @param original The original bitmap to compress
     * @param maxWidth Maximum width for the compressed image
     * @param maxHeight Maximum height for the compressed image
     * @param quality JPEG quality (0-100)
     * @return Compressed bitmap, or null if compression failed
     */
    public static Bitmap compressImage(Bitmap original, int maxWidth, int maxHeight, int quality) {
        if (original == null || original.isRecycled()) {
            return null;
        }
        
        // Validate parameters
        if (maxWidth <= 0 || maxHeight <= 0 || quality < 0 || quality > 100) {
            return null;
        }
        
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // If image is already smaller than max dimensions, return copy
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original.copy(original.getConfig(), false);
        }
        
        // Calculate scaling factor
        float scaleWidth = (float) maxWidth / originalWidth;
        float scaleHeight = (float) maxHeight / originalHeight;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);
        
        // Calculate new dimensions
        int newWidth = Math.round(originalWidth * scaleFactor);
        int newHeight = Math.round(originalHeight * scaleFactor);
        
        // Create transformation matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor);
        
        try {
            // Create scaled bitmap
            return Bitmap.createBitmap(original, 0, 0, originalWidth, originalHeight, matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Compress an image bitmap with default settings
     * @param original The original bitmap to compress
     * @return Compressed bitmap, or null if compression failed
     */
    public static Bitmap compressImage(Bitmap original) {
        return compressImage(original, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }
    
    /**
     * Check if an image bitmap is readable and valid
     * @param image The bitmap to validate
     * @return true if image is valid and readable, false otherwise
     */
    public static boolean isImageReadable(Bitmap image) {
        if (image == null || image.isRecycled()) {
            return false;
        }
        
        // Check minimum dimensions
        if (image.getWidth() < MIN_WIDTH || image.getHeight() < MIN_HEIGHT) {
            return false;
        }
        
        // Check if bitmap has valid config
        if (image.getConfig() == null) {
            return false;
        }
        
        // Try to access pixel data to ensure bitmap is not corrupted
        try {
            int pixel = image.getPixel(0, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Generate a unique filename for payment proof photo
     * @param transactionId The transaction ID to include in filename
     * @param timestamp The timestamp for the photo
     * @return Unique filename with .jpg extension
     */
    public static String generateUniqueFilename(long transactionId, Date timestamp) {
        if (timestamp == null) {
            timestamp = new Date();
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String formattedDate = dateFormat.format(timestamp);
        
        return String.format(Locale.getDefault(), "payment_proof_%d_%s.jpg", transactionId, formattedDate);
    }
    
    /**
     * Generate a unique filename for payment proof photo with current timestamp
     * @param transactionId The transaction ID to include in filename
     * @return Unique filename with .jpg extension
     */
    public static String generateUniqueFilename(long transactionId) {
        return generateUniqueFilename(transactionId, new Date());
    }
    
    /**
     * Validate image dimensions and size constraints
     * @param image The bitmap to validate
     * @return true if image meets all constraints, false otherwise
     */
    public static boolean validateImageConstraints(Bitmap image) {
        if (!isImageReadable(image)) {
            return false;
        }
        
        // Check maximum dimensions (reasonable limits)
        int maxDimension = 4096; // 4K resolution limit
        if (image.getWidth() > maxDimension || image.getHeight() > maxDimension) {
            return false;
        }
        
        // Estimate file size (rough calculation)
        long estimatedSize = (long) image.getWidth() * image.getHeight() * 4; // 4 bytes per pixel (ARGB)
        if (estimatedSize > MAX_FILE_SIZE) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate compression ratio needed to fit within size constraints
     * @param originalWidth Original image width
     * @param originalHeight Original image height
     * @param maxWidth Maximum allowed width
     * @param maxHeight Maximum allowed height
     * @return Compression ratio (0.0 to 1.0)
     */
    public static float calculateCompressionRatio(int originalWidth, int originalHeight, 
                                                 int maxWidth, int maxHeight) {
        if (originalWidth <= 0 || originalHeight <= 0 || maxWidth <= 0 || maxHeight <= 0) {
            return 1.0f;
        }
        
        float scaleWidth = (float) maxWidth / originalWidth;
        float scaleHeight = (float) maxHeight / originalHeight;
        
        return Math.min(Math.min(scaleWidth, scaleHeight), 1.0f);
    }
    
    /**
     * Get recommended JPEG quality based on image size
     * Larger images get lower quality to reduce file size
     * @param width Image width
     * @param height Image height
     * @return Recommended JPEG quality (0-100)
     */
    public static int getRecommendedQuality(int width, int height) {
        long pixels = (long) width * height;
        
        if (pixels > 2000000) { // > 2MP
            return 75;
        } else if (pixels > 1000000) { // > 1MP
            return 80;
        } else {
            return 85;
        }
    }
    
    /**
     * Create a thumbnail version of the image
     * @param original The original bitmap
     * @param thumbnailSize The size for the thumbnail (width and height)
     * @return Thumbnail bitmap, or null if failed
     */
    public static Bitmap createThumbnail(Bitmap original, int thumbnailSize) {
        if (original == null || thumbnailSize <= 0) {
            return null;
        }
        
        return compressImage(original, thumbnailSize, thumbnailSize, 80);
    }
    
    /**
     * Validate filename format for payment proof photos
     * @param filename The filename to validate
     * @return true if filename follows expected format, false otherwise
     */
    public static boolean isValidPaymentProofFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // Check if filename follows pattern: payment_proof_[transactionId]_[timestamp].jpg
        String pattern = "^payment_proof_\\d+_\\d{8}_\\d{6}\\.jpg$";
        return filename.matches(pattern);
    }
    
    /**
     * Extract transaction ID from payment proof filename
     * @param filename The filename to parse
     * @return Transaction ID, or -1 if parsing failed
     */
    public static long extractTransactionIdFromFilename(String filename) {
        if (!isValidPaymentProofFilename(filename)) {
            return -1;
        }
        
        try {
            // Extract transaction ID from filename pattern
            String[] parts = filename.split("_");
            if (parts.length >= 3) {
                return Long.parseLong(parts[2]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
}
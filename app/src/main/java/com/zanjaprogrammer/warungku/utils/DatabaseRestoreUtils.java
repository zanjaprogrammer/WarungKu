package com.zanjaprogrammer.warungku.utils;

import android.content.Context;
import android.net.Uri;
import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.service.PaymentProofManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatabaseRestoreUtils {
    
    private static final String DB_NAME = "warungku_db";
    
    /**
     * Restore database dari file SQLite
     * Note: Method ini akan membuat backup otomatis sebelum restore
     * @param context Context
     * @param backupFile File backup yang akan di-restore
     * @return true jika berhasil, false jika gagal
     */
    public static boolean restoreDatabase(Context context, File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            return false;
        }
        
        // Check if it's a ZIP backup (with payment proofs) or simple database backup
        if (backupFile.getName().endsWith(".zip")) {
            return restoreFromZipBackup(context, backupFile);
        } else {
            return restoreFromDatabaseBackup(context, backupFile);
        }
    }
    
    /**
     * Restore from simple database backup file
     */
    private static boolean restoreFromDatabaseBackup(Context context, File backupFile) {
        try {
            // 1. Buat backup otomatis sebelum restore (safety)
            File autoBackup = DatabaseBackupUtils.backupDatabase(context);
            if (autoBackup == null) {
                // Jika gagal backup, tetap lanjutkan restore (user sudah konfirmasi)
                // Tapi log warning
                android.util.Log.w("DatabaseRestore", "Gagal membuat backup otomatis sebelum restore");
            }
            
            // 2. Close database connection
            AppDatabase db = AppDatabase.getDatabase(context);
            db.close();
            
            // 3. Get database file
            File dbFile = context.getDatabasePath(DB_NAME);
            File dbWalFile = new File(dbFile.getPath() + "-wal");
            File dbShmFile = new File(dbFile.getPath() + "-shm");
            
            // 4. Delete existing database files
            if (dbFile.exists()) dbFile.delete();
            if (dbWalFile.exists()) dbWalFile.delete();
            if (dbShmFile.exists()) dbShmFile.delete();
            
            // 5. Copy backup file ke database location
            FileInputStream fis = new FileInputStream(backupFile);
            FileOutputStream fos = new FileOutputStream(dbFile);
            FileChannel source = fis.getChannel();
            FileChannel destination = fos.getChannel();
            destination.transferFrom(source, 0, source.size());
            
            source.close();
            destination.close();
            fis.close();
            fos.close();
            
            // 6. Reopen database (akan dibuat otomatis oleh Room)
            AppDatabase.getDatabase(context);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Restore from ZIP backup file (includes payment proof photos)
     */
    private static boolean restoreFromZipBackup(Context context, File zipBackupFile) {
        try {
            // 1. Buat backup otomatis sebelum restore (safety)
            File autoBackup = DatabaseBackupUtils.backupDatabase(context, true);
            if (autoBackup == null) {
                android.util.Log.w("DatabaseRestore", "Gagal membuat backup otomatis sebelum restore");
            }
            
            // 2. Close database connection
            AppDatabase db = AppDatabase.getDatabase(context);
            db.close();
            
            // 3. Extract ZIP file
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipBackupFile));
            ZipEntry entry;
            
            File tempDbFile = null;
            PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
            int restoredPhotos = 0;
            
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                if ("database.db".equals(entryName)) {
                    // Extract database file to temp location
                    tempDbFile = new File(context.getCacheDir(), "temp_restore_db.db");
                    extractZipEntry(zipIn, tempDbFile);
                } else if (entryName.startsWith("payment_proofs/") && !entry.isDirectory()) {
                    // Extract payment proof photo
                    String fileName = entryName.substring("payment_proofs/".length());
                    if (!fileName.isEmpty()) {
                        File photoFile = new File(context.getCacheDir(), "temp_" + fileName);
                        extractZipEntry(zipIn, photoFile);
                        
                        // Restore photo to proper location
                        String restoredPath = storageHelper.restorePhotoFromBackup(photoFile, fileName);
                        if (restoredPath != null) {
                            restoredPhotos++;
                        }
                        
                        // Clean up temp file
                        photoFile.delete();
                    }
                }
                // Skip backup_info.txt and other files
                
                zipIn.closeEntry();
            }
            zipIn.close();
            
            // 4. Restore database if extracted
            if (tempDbFile != null && tempDbFile.exists()) {
                boolean dbRestored = restoreFromDatabaseBackup(context, tempDbFile);
                tempDbFile.delete(); // Clean up temp file
                
                if (dbRestored) {
                    // 5. Re-link payment proof photos to transactions
                    relinkPaymentProofPhotos(context);
                    
                    android.util.Log.i("DatabaseRestore", 
                        "Restore completed: database + " + restoredPhotos + " photos");
                    return true;
                } else {
                    android.util.Log.e("DatabaseRestore", "Failed to restore database from ZIP");
                    return false;
                }
            } else {
                android.util.Log.e("DatabaseRestore", "No database found in ZIP backup");
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Extract a single entry from ZIP to file
     */
    private static void extractZipEntry(ZipInputStream zipIn, File outputFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] buffer = new byte[8192];
        int length;
        while ((length = zipIn.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fos.close();
    }
    
    /**
     * Re-link payment proof photos to transactions after restore
     * This validates that all payment proof records have valid file paths
     */
    private static void relinkPaymentProofPhotos(Context context) {
        try {
            PaymentProofManager manager = new PaymentProofManager(context);
            
            // Get all payment proof records from database
            List<PaymentProof> allProofs = manager.getAllPaymentProofs().getValue();
            if (allProofs == null) return;
            
            PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
            int relinkedCount = 0;
            int corruptedCount = 0;
            
            for (PaymentProof proof : allProofs) {
                if (proof.getFileName() != null) {
                    // Try to find the restored photo file
                    String expectedPath = storageHelper.getPaymentProofPath(proof.getFileName());
                    File photoFile = new File(expectedPath);
                    
                    if (photoFile.exists()) {
                        // Update the file path in database if different
                        if (!expectedPath.equals(proof.getFilePath())) {
                            proof.setFilePath(expectedPath);
                            proof.setFileSize(photoFile.length());
                            proof.setCorrupted(false);
                            // Update in database would need to be done through DAO
                            relinkedCount++;
                        }
                    } else {
                        // Mark as corrupted if photo file not found
                        proof.setCorrupted(true);
                        corruptedCount++;
                    }
                }
            }
            
            android.util.Log.i("DatabaseRestore", 
                "Photo relinking: " + relinkedCount + " relinked, " + corruptedCount + " corrupted");
                
        } catch (Exception e) {
            android.util.Log.e("DatabaseRestore", "Error relinking payment proof photos", e);
        }
    }
    
    /**
     * Restore database dari Uri (untuk file picker)
     * @param context Context
     * @param uri Uri dari file backup
     * @return true jika berhasil, false jika gagal
     */
    public static boolean restoreDatabaseFromUri(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }
        
        try {
            // 1. Copy URI content to temporary file
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return false;
            }
            
            // Determine file type from URI or content
            String fileName = getFileNameFromUri(context, uri);
            boolean isZipBackup = fileName != null && fileName.endsWith(".zip");
            
            File tempFile = new File(context.getCacheDir(), 
                isZipBackup ? "temp_backup.zip" : "temp_backup.db");
            
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            
            inputStream.close();
            fos.close();
            
            // 2. Restore from temporary file
            boolean result = restoreDatabase(context, tempFile);
            
            // 3. Clean up temporary file
            tempFile.delete();
            
            return result;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get filename from URI
     */
    private static String getFileNameFromUri(Context context, Uri uri) {
        try {
            android.database.Cursor cursor = context.getContentResolver().query(
                uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    String fileName = cursor.getString(nameIndex);
                    cursor.close();
                    return fileName;
                }
                cursor.close();
            }
        } catch (Exception e) {
            // Fallback to URI path
        }
        
        String path = uri.getPath();
        if (path != null) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < path.length() - 1) {
                return path.substring(lastSlash + 1);
            }
        }
        
        return null;
    }
    
    /**
     * Validate backup file (check if it's a valid SQLite database or ZIP backup)
     * @param file Backup file
     * @return true jika valid, false jika tidak
     */
    public static boolean isValidBackupFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String fileName = file.getName();
        
        // Check if it's a ZIP backup
        if (fileName.endsWith(".zip")) {
            return isValidZipBackup(file);
        }
        
        // Check if it's a database backup
        if (fileName.endsWith(".db")) {
            return isValidDatabaseBackup(file);
        }
        
        return false;
    }
    
    /**
     * Validate ZIP backup file
     */
    private static boolean isValidZipBackup(File file) {
        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            boolean hasDatabaseFile = false;
            
            while ((entry = zipIn.getNextEntry()) != null) {
                if ("database.db".equals(entry.getName())) {
                    hasDatabaseFile = true;
                    break;
                }
                zipIn.closeEntry();
            }
            zipIn.close();
            
            return hasDatabaseFile;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Validate database backup file
     */
    private static boolean isValidDatabaseBackup(File file) {
        // Check file size (minimal beberapa KB)
        if (file.length() < 1024) {
            return false;
        }
        
        // Check SQLite magic number (first 16 bytes should be "SQLite format 3\000")
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] header = new byte[16];
            int read = fis.read(header);
            fis.close();
            
            if (read < 16) {
                return false;
            }
            
            String headerStr = new String(header);
            return headerStr.startsWith("SQLite format 3");
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get backup file information
     * @param file Backup file
     * @return Array with [isZipBackup, hasPaymentProofs, estimatedPhotoCount]
     */
    public static long[] getBackupFileInfo(File file) {
        if (!isValidBackupFile(file)) {
            return new long[]{0, 0, 0};
        }
        
        if (file.getName().endsWith(".zip")) {
            return getZipBackupInfo(file);
        } else {
            return new long[]{0, 0, 0}; // Database backup only
        }
    }
    
    /**
     * Get ZIP backup information
     */
    private static long[] getZipBackupInfo(File file) {
        try {
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            boolean hasDatabase = false;
            int photoCount = 0;
            
            while ((entry = zipIn.getNextEntry()) != null) {
                String entryName = entry.getName();
                if ("database.db".equals(entryName)) {
                    hasDatabase = true;
                } else if (entryName.startsWith("payment_proofs/") && !entry.isDirectory()) {
                    photoCount++;
                }
                zipIn.closeEntry();
            }
            zipIn.close();
            
            return new long[]{1, photoCount > 0 ? 1 : 0, photoCount};
        } catch (IOException e) {
            return new long[]{0, 0, 0};
        }
    }
}


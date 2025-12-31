package com.zanjaprogrammer.warungku.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.zanjaprogrammer.warungku.service.PaymentProofManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DatabaseBackupUtils {
    
    private static final String DB_NAME = "warungku_db";
    private static final String BACKUP_FOLDER = "WarungKu_Backup";
    
    /**
     * Backup database SQLite file ke app's external files directory
     * Tidak perlu permission untuk Android 10+
     * @return File path jika berhasil, null jika gagal
     */
    public static File backupDatabase(Context context) {
        return backupDatabase(context, false);
    }
    
    /**
     * Backup database SQLite file dengan opsi untuk include payment proof photos
     * @param context Context
     * @param includePaymentProofs Whether to include payment proof photos in backup
     * @return File path jika berhasil, null jika gagal
     */
    public static File backupDatabase(Context context, boolean includePaymentProofs) {
        try {
            // Get database file
            File dbFile = context.getDatabasePath(DB_NAME);
            if (!dbFile.exists()) {
                return null;
            }
            
            // Create backup folder di app's external files directory
            File backupFolder = new File(context.getExternalFilesDir(null), BACKUP_FOLDER);
            if (!backupFolder.exists()) {
                backupFolder.mkdirs();
            }
            
            // Create backup file dengan timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            
            if (includePaymentProofs) {
                // Create ZIP backup with database and payment proof photos
                String backupFileName = "warungku_backup_with_photos_" + timestamp + ".zip";
                File backupFile = new File(backupFolder, backupFileName);
                return createZipBackup(context, dbFile, backupFile);
            } else {
                // Create simple database backup
                String backupFileName = "warungku_backup_" + timestamp + ".db";
                File backupFile = new File(backupFolder, backupFileName);
                return createDatabaseBackup(dbFile, backupFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Create simple database backup (database file only)
     */
    private static File createDatabaseBackup(File dbFile, File backupFile) throws IOException {
        // Copy database file
        FileInputStream fis = new FileInputStream(dbFile);
        FileOutputStream fos = new FileOutputStream(backupFile);
        FileChannel source = fis.getChannel();
        FileChannel destination = fos.getChannel();
        destination.transferFrom(source, 0, source.size());
        
        source.close();
        destination.close();
        fis.close();
        fos.close();
        
        return backupFile;
    }
    
    /**
     * Create ZIP backup with database and payment proof photos
     */
    private static File createZipBackup(Context context, File dbFile, File backupFile) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(backupFile));
        
        try {
            // Add database file to ZIP
            addFileToZip(zipOut, dbFile, "database.db");
            
            // Add payment proof photos to ZIP
            PaymentProofStorageHelper storageHelper = new PaymentProofStorageHelper(context);
            List<String> paymentProofFiles = storageHelper.getAllPaymentProofFiles();
            
            for (String filePath : paymentProofFiles) {
                File photoFile = new File(filePath);
                if (photoFile.exists()) {
                    // Use filename only for ZIP entry (maintain flat structure)
                    String entryName = "payment_proofs/" + photoFile.getName();
                    addFileToZip(zipOut, photoFile, entryName);
                }
            }
            
            // Add backup metadata
            String metadata = createBackupMetadata(context, paymentProofFiles.size());
            ZipEntry metadataEntry = new ZipEntry("backup_info.txt");
            zipOut.putNextEntry(metadataEntry);
            zipOut.write(metadata.getBytes());
            zipOut.closeEntry();
            
        } finally {
            zipOut.close();
        }
        
        return backupFile;
    }
    
    /**
     * Add a file to ZIP archive
     */
    private static void addFileToZip(ZipOutputStream zipOut, File file, String entryName) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);
        
        byte[] buffer = new byte[8192];
        int length;
        while ((length = fis.read(buffer)) >= 0) {
            zipOut.write(buffer, 0, length);
        }
        
        fis.close();
        zipOut.closeEntry();
    }
    
    /**
     * Create backup metadata information
     */
    private static String createBackupMetadata(Context context, int photoCount) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("WarungKu Backup Information\n");
        metadata.append("==========================\n");
        metadata.append("Backup Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
        metadata.append("App Version: ").append(getAppVersion(context)).append("\n");
        metadata.append("Database: warungku_db\n");
        metadata.append("Payment Proof Photos: ").append(photoCount).append(" files\n");
        metadata.append("\nContents:\n");
        metadata.append("- database.db (SQLite database)\n");
        if (photoCount > 0) {
            metadata.append("- payment_proofs/ (").append(photoCount).append(" photo files)\n");
        }
        metadata.append("- backup_info.txt (this file)\n");
        return metadata.toString();
    }
    
    /**
     * Get app version for metadata
     */
    private static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Validate payment proof photo files during backup
     * @param context Context
     * @return Array with [validCount, corruptedCount, totalSize]
     */
    public static long[] validatePaymentProofFiles(Context context) {
        try {
            PaymentProofManager manager = new PaymentProofManager(context);
            int[] result = manager.validateAllPaymentProofs().get();
            if (result != null && result.length >= 3) {
                return new long[]{result[0], result[1], result[2]};
            }
            return new long[]{0, 0, 0};
        } catch (Exception e) {
            e.printStackTrace();
            return new long[]{0, 0, 0};
        }
    }
    
    /**
     * Share backup file via Intent (Google Drive, Email, dll)
     */
    public static Intent getShareIntent(Context context, File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            return null;
        }
        
        Uri fileUri = FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".fileprovider",
            backupFile
        );
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        
        // Set appropriate MIME type based on file extension
        if (backupFile.getName().endsWith(".zip")) {
            shareIntent.setType("application/zip");
        } else {
            shareIntent.setType("application/octet-stream");
        }
        
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WarungKu Backup - " + backupFile.getName());
        
        // Add descriptive text based on backup type
        String description = "Backup data WarungKu";
        if (backupFile.getName().contains("with_photos")) {
            description += " (termasuk foto bukti pembayaran)";
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, description);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        return shareIntent;
    }
}


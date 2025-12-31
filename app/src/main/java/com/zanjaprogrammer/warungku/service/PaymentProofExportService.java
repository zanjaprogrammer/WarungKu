package com.zanjaprogrammer.warungku.service;

import android.content.Context;
import android.os.Environment;

import com.zanjaprogrammer.warungku.data.AppDatabase;
import com.zanjaprogrammer.warungku.data.dao.CashFlowDao;
import com.zanjaprogrammer.warungku.data.dao.PaymentProofDao;
import com.zanjaprogrammer.warungku.data.entity.CashFlow;
import com.zanjaprogrammer.warungku.data.entity.PaymentProof;
import com.zanjaprogrammer.warungku.utils.CurrencyFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service class for exporting payment proof data and photos
 * Handles structured export with photos and transaction data in various formats
 */
public class PaymentProofExportService {
    
    private final Context context;
    private final PaymentProofDao paymentProofDao;
    private final CashFlowDao cashFlowDao;
    private final ExecutorService executorService;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    
    public PaymentProofExportService(Context context) {
        this.context = context;
        AppDatabase database = AppDatabase.getDatabase(context);
        this.paymentProofDao = database.paymentProofDao();
        this.cashFlowDao = database.cashFlowDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Export all payment proofs with photos and transaction data
     * @param exportDirectory Directory to export files to
     * @return CompletableFuture with export result [successCount, failureCount, totalSizeBytes, exportPath]
     */
    public CompletableFuture<ExportResult> exportAllPaymentProofs(String exportDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> allProofs = paymentProofDao.getAllPaymentProofsSync();
                return exportPaymentProofs(allProofs, exportDirectory, "all_payment_proofs");
            } catch (Exception e) {
                e.printStackTrace();
                return new ExportResult(0, 0, 0, null, e.getMessage());
            }
        }, executorService);
    }
    
    /**
     * Export payment proofs by date range
     * @param startDate Start date timestamp
     * @param endDate End date timestamp
     * @param exportDirectory Directory to export files to
     * @return CompletableFuture with export result
     */
    public CompletableFuture<ExportResult> exportPaymentProofsByDateRange(
            long startDate, long endDate, String exportDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> proofs = paymentProofDao.getPaymentProofsByDateRange(startDate, endDate).getValue();
                if (proofs == null) {
                    return new ExportResult(0, 0, 0, null, "No data found for date range");
                }
                
                String dateRangeStr = fileNameDateFormat.format(new Date(startDate)) + "_to_" + 
                                     fileNameDateFormat.format(new Date(endDate));
                return exportPaymentProofs(proofs, exportDirectory, "payment_proofs_" + dateRangeStr);
            } catch (Exception e) {
                e.printStackTrace();
                return new ExportResult(0, 0, 0, null, e.getMessage());
            }
        }, executorService);
    }
    
    /**
     * Export specific payment proofs by IDs
     * @param paymentProofIds List of payment proof IDs to export
     * @param exportDirectory Directory to export files to
     * @return CompletableFuture with export result
     */
    public CompletableFuture<ExportResult> exportSelectedPaymentProofs(
            List<Long> paymentProofIds, String exportDirectory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> selectedProofs = new java.util.ArrayList<>();
                for (Long id : paymentProofIds) {
                    PaymentProof proof = paymentProofDao.getPaymentProofByIdSync(id);
                    if (proof != null) {
                        selectedProofs.add(proof);
                    }
                }
                
                return exportPaymentProofs(selectedProofs, exportDirectory, "selected_payment_proofs");
            } catch (Exception e) {
                e.printStackTrace();
                return new ExportResult(0, 0, 0, null, e.getMessage());
            }
        }, executorService);
    }
    
    /**
     * Core export method that handles the actual export process
     */
    private ExportResult exportPaymentProofs(List<PaymentProof> proofs, String exportDirectory, String baseName) {
        if (proofs == null || proofs.isEmpty()) {
            return new ExportResult(0, 0, 0, null, "No payment proofs to export");
        }
        
        try {
            // Create export directory
            File exportDir = new File(exportDirectory);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            // Create timestamped export folder
            String timestamp = fileNameDateFormat.format(new Date());
            String exportFolderName = baseName + "_" + timestamp;
            File exportFolder = new File(exportDir, exportFolderName);
            exportFolder.mkdirs();
            
            // Export in multiple formats
            ExportResult csvResult = exportAsCSV(proofs, exportFolder);
            ExportResult zipResult = exportAsZIP(proofs, exportFolder);
            
            // Combine results
            int totalSuccess = csvResult.successCount + zipResult.successCount;
            int totalFailure = csvResult.failureCount + zipResult.failureCount;
            long totalSize = csvResult.totalSizeBytes + zipResult.totalSizeBytes;
            
            return new ExportResult(totalSuccess, totalFailure, totalSize, 
                                  exportFolder.getAbsolutePath(), null);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ExportResult(0, proofs.size(), 0, null, e.getMessage());
        }
    }
    
    /**
     * Export payment proofs as CSV with transaction data
     */
    private ExportResult exportAsCSV(List<PaymentProof> proofs, File exportFolder) {
        try {
            File csvFile = new File(exportFolder, "payment_proofs_data.csv");
            FileWriter writer = new FileWriter(csvFile);
            
            // Write CSV header
            writer.append("Transaction ID,Date,Amount,Description,Payment Method,")
                  .append("Photo Filename,Photo Size (bytes),Capture Date,File Path,Is Corrupted\n");
            
            int successCount = 0;
            long totalSize = 0;
            
            for (PaymentProof proof : proofs) {
                try {
                    // Get transaction data
                    CashFlow transaction = cashFlowDao.getCashFlowById(proof.getTransactionId());
                    
                    // Write CSV row
                    writer.append(String.valueOf(proof.getTransactionId())).append(",");
                    
                    if (transaction != null) {
                        writer.append(dateFormat.format(new Date(transaction.timestamp))).append(",");
                        writer.append(CurrencyFormatter.formatPlain(transaction.amount)).append(",");
                        writer.append("\"").append(escapeCSV(transaction.description)).append("\"").append(",");
                    } else {
                        writer.append(",,Unknown Transaction,");
                    }
                    
                    writer.append(proof.getPaymentMethod() != null ? proof.getPaymentMethod() : "").append(",");
                    writer.append(proof.getFileName() != null ? proof.getFileName() : "").append(",");
                    writer.append(String.valueOf(proof.getFileSize())).append(",");
                    writer.append(dateFormat.format(new Date(proof.getCaptureTimestamp()))).append(",");
                    writer.append(proof.getFilePath() != null ? proof.getFilePath() : "").append(",");
                    writer.append(proof.isCorrupted() ? "Yes" : "No").append("\n");
                    
                    successCount++;
                    totalSize += proof.getFileSize();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    // Continue with next record
                }
            }
            
            writer.close();
            totalSize += csvFile.length();
            
            return new ExportResult(successCount, proofs.size() - successCount, totalSize, 
                                  csvFile.getAbsolutePath(), null);
            
        } catch (IOException e) {
            e.printStackTrace();
            return new ExportResult(0, proofs.size(), 0, null, e.getMessage());
        }
    }
    
    /**
     * Export payment proofs as ZIP with photos and CSV data
     */
    private ExportResult exportAsZIP(List<PaymentProof> proofs, File exportFolder) {
        try {
            File zipFile = new File(exportFolder, "payment_proofs_with_photos.zip");
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            
            int successCount = 0;
            int failureCount = 0;
            long totalSize = 0;
            
            // Add CSV data to ZIP
            File csvFile = new File(exportFolder, "payment_proofs_data.csv");
            if (csvFile.exists()) {
                addFileToZip(zipOut, csvFile, "data/payment_proofs_data.csv");
                totalSize += csvFile.length();
            }
            
            // Add photos to ZIP
            for (PaymentProof proof : proofs) {
                try {
                    if (proof.getFilePath() != null && !proof.isCorrupted()) {
                        File photoFile = new File(proof.getFilePath());
                        if (photoFile.exists()) {
                            String zipEntryName = "photos/" + (proof.getFileName() != null ? 
                                                 proof.getFileName() : "photo_" + proof.getId() + ".jpg");
                            addFileToZip(zipOut, photoFile, zipEntryName);
                            successCount++;
                            totalSize += photoFile.length();
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
            
            // Add export metadata
            String metadata = createExportMetadata(proofs, successCount, failureCount);
            ZipEntry metadataEntry = new ZipEntry("export_info.txt");
            zipOut.putNextEntry(metadataEntry);
            zipOut.write(metadata.getBytes());
            zipOut.closeEntry();
            
            zipOut.close();
            totalSize += zipFile.length();
            
            return new ExportResult(successCount, failureCount, totalSize, 
                                  zipFile.getAbsolutePath(), null);
            
        } catch (IOException e) {
            e.printStackTrace();
            return new ExportResult(0, proofs.size(), 0, null, e.getMessage());
        }
    }
    
    /**
     * Add a file to ZIP archive
     */
    private void addFileToZip(ZipOutputStream zipOut, File file, String entryName) throws IOException {
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
     * Create export metadata information
     */
    private String createExportMetadata(List<PaymentProof> proofs, int successCount, int failureCount) {
        StringBuilder metadata = new StringBuilder();
        metadata.append("WarungKu Payment Proof Export\n");
        metadata.append("============================\n");
        metadata.append("Export Date: ").append(dateFormat.format(new Date())).append("\n");
        metadata.append("Total Records: ").append(proofs.size()).append("\n");
        metadata.append("Photos Exported: ").append(successCount).append("\n");
        metadata.append("Photos Failed: ").append(failureCount).append("\n");
        metadata.append("\nExport Contents:\n");
        metadata.append("- data/payment_proofs_data.csv (Transaction data in CSV format)\n");
        metadata.append("- photos/ (Payment proof photo files)\n");
        metadata.append("- export_info.txt (This file)\n");
        metadata.append("\nCSV Columns:\n");
        metadata.append("Transaction ID, Date, Amount, Description, Payment Method, ");
        metadata.append("Photo Filename, Photo Size, Capture Date, File Path, Is Corrupted\n");
        return metadata.toString();
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Get export statistics for progress tracking
     * @return CompletableFuture with statistics [totalRecords, validPhotos, corruptedPhotos, totalSizeBytes]
     */
    public CompletableFuture<long[]> getExportStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<PaymentProof> allProofs = paymentProofDao.getAllPaymentProofsSync();
                
                long totalRecords = allProofs.size();
                long validPhotos = 0;
                long corruptedPhotos = 0;
                long totalSize = 0;
                
                for (PaymentProof proof : allProofs) {
                    if (proof.isCorrupted()) {
                        corruptedPhotos++;
                    } else if (proof.getFilePath() != null) {
                        File photoFile = new File(proof.getFilePath());
                        if (photoFile.exists()) {
                            validPhotos++;
                            totalSize += photoFile.length();
                        } else {
                            corruptedPhotos++;
                        }
                    }
                }
                
                return new long[]{totalRecords, validPhotos, corruptedPhotos, totalSize};
                
            } catch (Exception e) {
                e.printStackTrace();
                return new long[]{0, 0, 0, 0};
            }
        }, executorService);
    }
    
    /**
     * Create export directory in app's external files directory
     * @param folderName Name of the export folder
     * @return File object for the export directory
     */
    public File createExportDirectory(String folderName) {
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        return exportDir;
    }
    
    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * Export result data class
     */
    public static class ExportResult {
        public final int successCount;
        public final int failureCount;
        public final long totalSizeBytes;
        public final String exportPath;
        public final String errorMessage;
        
        public ExportResult(int successCount, int failureCount, long totalSizeBytes, 
                          String exportPath, String errorMessage) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.totalSizeBytes = totalSizeBytes;
            this.exportPath = exportPath;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() {
            return errorMessage == null && successCount > 0;
        }
        
        public String getFormattedSize() {
            if (totalSizeBytes < 1024) return totalSizeBytes + " B";
            if (totalSizeBytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", totalSizeBytes / 1024.0);
            return String.format(Locale.getDefault(), "%.1f MB", totalSizeBytes / (1024.0 * 1024.0));
        }
    }
}
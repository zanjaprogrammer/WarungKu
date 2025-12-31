package com.zanjaprogrammer.warungku.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "payment_proofs")
public class PaymentProof {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @ColumnInfo(name = "transaction_id")
    private long transactionId;
    
    @ColumnInfo(name = "file_path")
    private String filePath;
    
    @ColumnInfo(name = "file_name")
    private String fileName;
    
    @ColumnInfo(name = "capture_timestamp")
    private long captureTimestamp;
    
    @ColumnInfo(name = "file_size")
    private long fileSize;
    
    @ColumnInfo(name = "is_corrupted")
    private boolean isCorrupted;
    
    @ColumnInfo(name = "payment_method")
    private String paymentMethod; // "QRIS"

    // Default constructor
    public PaymentProof() {
    }

    // Constructor
    @Ignore
    public PaymentProof(long transactionId, String filePath, String fileName, 
                       long captureTimestamp, long fileSize, boolean isCorrupted, 
                       String paymentMethod) {
        this.transactionId = transactionId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.captureTimestamp = captureTimestamp;
        this.fileSize = fileSize;
        this.isCorrupted = isCorrupted;
        this.paymentMethod = paymentMethod;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getCaptureTimestamp() {
        return captureTimestamp;
    }

    public void setCaptureTimestamp(long captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }

    public void setCorrupted(boolean corrupted) {
        isCorrupted = corrupted;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
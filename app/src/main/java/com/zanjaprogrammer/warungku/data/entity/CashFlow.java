package com.zanjaprogrammer.warungku.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "cash_flow")
public class CashFlow {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String type; // "IN" (SALE) or "OUT" (EXPENSE)
    public double amount;
    public String description;
    public long timestamp;
    public Integer productId; // Optional, linked to Product if it's a sale
    public Double profit; // Calculated at time of sale
    
    @ColumnInfo(name = "has_payment_proof")
    public boolean hasPaymentProof;
    
    @ColumnInfo(name = "payment_method")
    public String paymentMethod; // "CASH", "QRIS"

    // Default constructor
    public CashFlow() {
    }

    // Original constructor for backward compatibility
    @Ignore
    public CashFlow(String type, double amount, String description, long timestamp, Integer productId, Double profit) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.productId = productId;
        this.profit = profit;
        this.hasPaymentProof = false;
        this.paymentMethod = "CASH"; // Default to cash
    }

    // Extended constructor with payment proof fields
    @Ignore
    public CashFlow(String type, double amount, String description, long timestamp, 
                   Integer productId, Double profit, boolean hasPaymentProof, String paymentMethod) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.productId = productId;
        this.profit = profit;
        this.hasPaymentProof = hasPaymentProof;
        this.paymentMethod = paymentMethod;
    }
    
    // Getters and setters for payment proof fields
    public boolean isHasPaymentProof() {
        return hasPaymentProof;
    }
    
    public void setHasPaymentProof(boolean hasPaymentProof) {
        this.hasPaymentProof = hasPaymentProof;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

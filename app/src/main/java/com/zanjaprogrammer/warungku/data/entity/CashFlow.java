package com.zanjaprogrammer.warungku.data.entity;

import androidx.room.Entity;
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
    
    // Sync fields
    public boolean synced; // Whether this cash flow has been synced to Firestore
    public long lastSyncedAt; // Last sync timestamp
    public String cloudId; // Firestore document ID (nullable)

    public CashFlow(String type, double amount, String description, long timestamp, Integer productId, Double profit) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.productId = productId;
        this.profit = profit;
        this.synced = false;
        this.lastSyncedAt = 0;
        this.cloudId = null;
    }
}

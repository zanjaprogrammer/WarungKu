package com.zanjaprogrammer.warungku.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double sellPrice;
    public Double buyPrice; // Nullable
    public int currentStock;
    public int minStock;
    public int salesCount; // To track frequency
    public boolean isFavorite; // Favorite flag
    public long lastSoldTimestamp; // Last sold timestamp
    public String barcode; // Barcode for scanning (nullable)
    
    // Sync fields
    public boolean synced; // Whether this product has been synced to Firestore
    public long lastSyncedAt; // Last sync timestamp
    public String cloudId; // Firestore document ID (nullable)

    public Product(String name, double sellPrice, Double buyPrice, int currentStock, int minStock) {
        this.name = name;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.salesCount = 0;
        this.isFavorite = false;
        this.lastSoldTimestamp = 0;
        this.barcode = null;
        this.synced = false;
        this.lastSyncedAt = 0;
        this.cloudId = null;
    }
}

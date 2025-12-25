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

    public Product(String name, double sellPrice, Double buyPrice, int currentStock, int minStock) {
        this.name = name;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.salesCount = 0;
    }
}

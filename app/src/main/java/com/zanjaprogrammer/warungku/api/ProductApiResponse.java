package com.zanjaprogrammer.warungku.api;

import com.google.gson.annotations.SerializedName;

/**
 * Response model dari Open Food Facts API
 * Format response sesuai dengan dokumentasi: https://world.openfoodfacts.org/data
 * 
 * ✅ 100% gratis, tidak ada batasan request
 * ✅ Database sangat besar, termasuk produk Indonesia
 */
public class ProductApiResponse {
    
    @SerializedName("status")
    public Integer status; // 1 = found, 0 = not found
    
    @SerializedName("status_verbose")
    public String statusVerbose; // Status message
    
    @SerializedName("product")
    public Product product;
    
    /**
     * Nested class untuk product data dari Open Food Facts
     */
    public static class Product {
        @SerializedName("product_name")
        public String productName; // Nama produk
        
        @SerializedName("product_name_en")
        public String productNameEn; // Nama produk (English)
        
        @SerializedName("brands")
        public String brands; // Merek (bisa multiple, dipisah koma)
        
        @SerializedName("brands_tags")
        public String[] brandsTags; // Array brand tags
        
        @SerializedName("categories")
        public String categories; // Kategori
        
        @SerializedName("categories_tags")
        public String[] categoriesTags; // Array kategori tags
        
        @SerializedName("quantity")
        public String quantity; // Kuantitas (misal: "330ml", "500g")
        
        @SerializedName("image_url")
        public String imageUrl; // URL gambar produk
        
        @SerializedName("image_front_url")
        public String imageFrontUrl; // URL gambar depan
        
        @SerializedName("image_small_url")
        public String imageSmallUrl; // URL gambar kecil
        
        @SerializedName("code")
        public String code; // Barcode
        
        @SerializedName("countries")
        public String countries; // Negara (bisa multiple)
        
        @SerializedName("countries_tags")
        public String[] countriesTags; // Array negara tags
        
        @SerializedName("ingredients_text")
        public String ingredientsText; // Daftar bahan
        
        @SerializedName("nutriments")
        public Nutriments nutriments; // Informasi nutrisi
    }
    
    /**
     * Nested class untuk nutriments (opsional, untuk info nutrisi)
     */
    public static class Nutriments {
        @SerializedName("energy-kcal_100g")
        public Double energyKcal; // Kalori per 100g
    }
    
    public ProductApiResponse() {
    }
    
    /**
     * Check if response is successful
     */
    public boolean isSuccess() {
        try {
            return status != null && status == 1 && product != null && 
                   ((product.productName != null && !product.productName.isEmpty()) ||
                    (product.productNameEn != null && !product.productNameEn.isEmpty()));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get product name (prioritize Indonesian name, fallback to English)
     */
    public String getName() {
        if (product != null) {
            // Prioritize product_name (bisa bahasa lokal)
            if (product.productName != null && !product.productName.isEmpty()) {
                return product.productName;
            }
            // Fallback to English
            if (product.productNameEn != null && !product.productNameEn.isEmpty()) {
                return product.productNameEn;
            }
        }
        return null;
    }
    
    /**
     * Get product brand (ambil brand pertama jika multiple)
     */
    public String getBrand() {
        if (product != null) {
            if (product.brands != null && !product.brands.isEmpty()) {
                // Jika multiple brands (dipisah koma), ambil yang pertama
                String[] brandArray = product.brands.split(",");
                if (brandArray.length > 0) {
                    return brandArray[0].trim();
                }
                return product.brands.trim();
            }
        }
        return null;
    }
    
    /**
     * Get product price as double
     * Note: Open Food Facts biasanya tidak menyediakan harga
     * Return null karena harga tidak tersedia di API ini
     */
    public Double getPrice() {
        // Open Food Facts tidak menyediakan data harga
        // User harus input manual
        return null;
    }
    
    /**
     * Get product category (ambil kategori pertama jika multiple)
     */
    public String getCategory() {
        if (product != null) {
            if (product.categories != null && !product.categories.isEmpty()) {
                // Jika multiple categories (dipisah koma), ambil yang pertama
                String[] categoryArray = product.categories.split(",");
                if (categoryArray.length > 0) {
                    return categoryArray[0].trim();
                }
                return product.categories.trim();
            }
        }
        return null;
    }
    
    /**
     * Get product quantity (misal: "330ml", "500g")
     */
    public String getQuantity() {
        if (product != null && product.quantity != null) {
            return product.quantity;
        }
        return null;
    }
}


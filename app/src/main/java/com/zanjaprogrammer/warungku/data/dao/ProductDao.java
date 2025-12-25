package com.zanjaprogrammer.warungku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.zanjaprogrammer.warungku.data.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE currentStock <= minStock ORDER BY salesCount DESC")
    LiveData<List<Product>> getShoppingList();

    @Query("SELECT * FROM products WHERE id = :id")
    Product getProductById(int id);
}

package com.zanjaprogrammer.warungku.data.model;

import com.zanjaprogrammer.warungku.data.entity.Product;

public class CartItem {
    public Product product;
    public int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return product.sellPrice * quantity;
    }
}

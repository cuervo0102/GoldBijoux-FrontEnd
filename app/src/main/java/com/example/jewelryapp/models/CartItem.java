package com.example.jewelryapp.models;

public class CartItem {
    private int id;
    private int user_id;
    private int product_id;
    private int quantity;
    private Product product;
    private String created_at;
    private String updated_at;

    public CartItem() {
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return user_id; }
    public int getProductId() { return product_id; }
    public int getQuantity() { return quantity; }
    public Product getProduct() { return product; }
    public String getCreatedAt() { return created_at; }
    public String getUpdatedAt() { return updated_at; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
    public void setProductId(int product_id) { this.product_id = product_id; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setProduct(Product product) { this.product = product; }
    public void setCreatedAt(String created_at) { this.created_at = created_at; }
    public void setUpdatedAt(String updated_at) { this.updated_at = updated_at; }

    // Helper method to calculate subtotal
    public double getSubtotal() {
        if (product != null) {
            try {
                double price = Double.parseDouble(product.getPrice());
                return price * quantity;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}

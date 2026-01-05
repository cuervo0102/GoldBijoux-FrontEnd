package com.example.jewelryapp.models;

import com.google.gson.annotations.SerializedName;

public class Product {

    private int id;
    private String name;
    private String description;
    private String price;
    private int stock;
    private String image;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("requires_ring_size")
    private boolean requiresRingSize;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Category object (for nested response)
    private Category category;

    // Constructors
    public Product() {
        this.isActive = true;
        this.requiresRingSize = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isRequiresRingSize() {
        return requiresRingSize;
    }

    public void setRequiresRingSize(boolean requiresRingSize) {
        this.requiresRingSize = requiresRingSize;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
package com.example.jewelryapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jewelryapp.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final String PREF_NAME = "CartPrefs";
    private static final String KEY_CART = "cart_items";
    private static final String KEY_FAVORITES = "favorite_items";

    private SharedPreferences prefs;
    private Gson gson;

    public CartManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Cart methods
    public void addToCart(Product product) {
        List<Product> cart = getCartItems();
        cart.add(product);
        saveCart(cart);
    }

    public void removeFromCart(int productId) {
        List<Product> cart = getCartItems();
        cart.removeIf(p -> p.getId() == productId);
        saveCart(cart);
    }

    public List<Product> getCartItems() {
        String json = prefs.getString(KEY_CART, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Product>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public int getCartCount() {
        return getCartItems().size();
    }

    public void clearCart() {
        prefs.edit().remove(KEY_CART).apply();
    }

    private void saveCart(List<Product> cart) {
        String json = gson.toJson(cart);
        prefs.edit().putString(KEY_CART, json).apply();
    }

    // Favorites methods
    public void addToFavorites(Product product) {
        List<Product> favorites = getFavorites();
        if (!isFavorite(product.getId())) {
            favorites.add(product);
            saveFavorites(favorites);
        }
    }

    public void removeFromFavorites(int productId) {
        List<Product> favorites = getFavorites();
        favorites.removeIf(p -> p.getId() == productId);
        saveFavorites(favorites);
    }

    public List<Product> getFavorites() {
        String json = prefs.getString(KEY_FAVORITES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Product>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isFavorite(int productId) {
        List<Product> favorites = getFavorites();
        for (Product p : favorites) {
            if (p.getId() == productId) {
                return true;
            }
        }
        return false;
    }

    public int getFavoritesCount() {
        return getFavorites().size();
    }

    private void saveFavorites(List<Product> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }


    public boolean isProductInFavorites(int productId) {
        List<Product> favorites = getFavorites();
        for (Product product : favorites) {
            if (product.getId() == productId) {
                return true;
            }
        }
        return false;
    }

    // And in CartHelper.java, update the isProductInCart method:
    private static boolean isProductInCart(CartManager cartManager, int productId) {
        // Check both cart and favorites if you want to merge them
        for (Product p : cartManager.getCartItems()) {
            if (p.getId() == productId) {
                return true;
            }
        }
        for (Product p : cartManager.getFavorites()) {
            if (p.getId() == productId) {
                return true;
            }
        }
        return false;
    }
}
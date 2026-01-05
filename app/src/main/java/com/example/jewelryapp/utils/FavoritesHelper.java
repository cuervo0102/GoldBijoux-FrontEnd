package com.example.jewelryapp.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.jewelryapp.models.Product;

public class FavoritesHelper {

    public static void addToFavorites(Context context, Product product) {
        CartManager cartManager = new CartManager(context);

        if (cartManager.isFavorite(product.getId())) {
            Toast.makeText(context, "Produit déjà dans les favoris", Toast.LENGTH_SHORT).show();
            return;
        }

        cartManager.addToFavorites(product);
        Toast.makeText(context, "Ajouté aux favoris!", Toast.LENGTH_SHORT).show();
    }

    public static void removeFromFavorites(Context context, int productId) {
        CartManager cartManager = new CartManager(context);
        cartManager.removeFromFavorites(productId);
        Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
    }

    public static boolean isFavorite(Context context, int productId) {
        CartManager cartManager = new CartManager(context);
        return cartManager.isFavorite(productId);
    }
}
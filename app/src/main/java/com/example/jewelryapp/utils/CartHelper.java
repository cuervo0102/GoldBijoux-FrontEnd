package com.example.jewelryapp.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartHelper {

    private static final String TAG = "CART_HELPER";

    /**
     * Add product to cart
     */
    public static void addToCart(Context context, Product product, SessionManager sessionManager, Runnable onSuccess) {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "Veuillez vous connecter pour ajouter au panier", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService();

        // FIXED: Create request with ONLY 2 parameters (no ring_size_id)
        ApiService.AddToCartRequest request = new ApiService.AddToCartRequest(
                product.getId(),
                1  // quantity
        );

        Call<ApiService.CartItemResponse> call = apiService.addToCart(token, request);
        call.enqueue(new Callback<ApiService.CartItemResponse>() {
            @Override
            public void onResponse(Call<ApiService.CartItemResponse> call, Response<ApiService.CartItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CartItemResponse cartResponse = response.body();
                    if (cartResponse.success) {
                        Toast.makeText(context, "Produit ajouté au panier!", Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        Toast.makeText(context, cartResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Erreur d'ajout au panier", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CartItemResponse> call, Throwable t) {
                Log.e(TAG, "Network error adding to cart: " + t.getMessage());
                Toast.makeText(context, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Remove product from cart
     */
    public static void removeFromCart(Context context, int cartId, SessionManager sessionManager, Runnable onSuccess) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService();

        Call<ApiService.DeleteResponse> call = apiService.removeCartItem(token, cartId);
        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Produit retiré du panier", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e(TAG, "Error removing from cart: " + t.getMessage());
            }
        });
    }

    /**
     * Clear cart
     */
    public static void clearCart(Context context, SessionManager sessionManager, Runnable onSuccess) {
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        ApiService apiService = RetrofitClient.getApiService();

        Call<ApiService.DeleteResponse> call = apiService.clearCart(token);
        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "Panier vidé", Toast.LENGTH_SHORT).show();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e(TAG, "Error clearing cart: " + t.getMessage());
            }
        });
    }
}

package com.example.jewelryapp.utils;

import android.content.Context;
import android.util.Log;

import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesManager {
    private static final String TAG = "FavoritesManager";

    private Context context;
    private SessionManager sessionManager;
    private ApiService apiService;

    // Cache for favorites (to reduce API calls)
    private Set<Integer> favoritesCache = new HashSet<>();
    private boolean cacheInitialized = false;

    public FavoritesManager(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public interface FavoritesCallback {
        void onSuccess(List<Product> favorites);
        void onError(String message);
    }

    public interface FavoriteActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface CheckFavoriteCallback {
        void onResult(boolean isFavorite);
    }

    // Get all favorites
    public void getFavorites(FavoritesCallback callback) {
        if (!sessionManager.isLoggedIn()) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.FavoritesResponse> call = apiService.getFavorites(token);

        call.enqueue(new Callback<ApiService.FavoritesResponse>() {
            @Override
            public void onResponse(Call<ApiService.FavoritesResponse> call, Response<ApiService.FavoritesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.FavoritesResponse favResponse = response.body();

                    if (favResponse.success) {
                        List<Product> favorites = favResponse.data != null ? favResponse.data : new ArrayList<>();

                        // Update cache
                        favoritesCache.clear();
                        for (Product product : favorites) {
                            favoritesCache.add(product.getId());
                        }
                        cacheInitialized = true;

                        callback.onSuccess(favorites);
                    } else {
                        callback.onError(favResponse.message);
                    }
                } else {
                    callback.onError("Erreur de chargement des favoris");
                }
            }

            @Override
            public void onFailure(Call<ApiService.FavoritesResponse> call, Throwable t) {
                Log.e(TAG, "Error getting favorites: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // Add to favorites
    public void addToFavorites(int productId, FavoriteActionCallback callback) {
        if (!sessionManager.isLoggedIn()) {
            callback.onError("Veuillez vous connecter");
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        ApiService.AddToFavoriteRequest request = new ApiService.AddToFavoriteRequest(productId);
        Call<ApiService.FavoriteResponse> call = apiService.addToFavorites(token, request);

        call.enqueue(new Callback<ApiService.FavoriteResponse>() {
            @Override
            public void onResponse(Call<ApiService.FavoriteResponse> call, Response<ApiService.FavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.FavoriteResponse favResponse = response.body();

                    if (favResponse.success) {
                        favoritesCache.add(productId);
                        callback.onSuccess();
                    } else {
                        callback.onError(favResponse.message);
                    }
                } else {
                    callback.onError("Erreur d'ajout aux favoris");
                }
            }

            @Override
            public void onFailure(Call<ApiService.FavoriteResponse> call, Throwable t) {
                Log.e(TAG, "Error adding to favorites: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // Remove from favorites
    public void removeFromFavorites(int productId, FavoriteActionCallback callback) {
        if (!sessionManager.isLoggedIn()) {
            callback.onError("Veuillez vous connecter");
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.DeleteResponse> call = apiService.removeFromFavorites(token, productId);

        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DeleteResponse deleteResponse = response.body();

                    if (deleteResponse.success) {
                        favoritesCache.remove(productId);
                        callback.onSuccess();
                    } else {
                        callback.onError(deleteResponse.message);
                    }
                } else {
                    callback.onError("Erreur de suppression");
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e(TAG, "Error removing from favorites: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // Check if product is favorite (uses cache if available)
    public boolean isFavoriteCached(int productId) {
        return cacheInitialized && favoritesCache.contains(productId);
    }

    // Check favorite status from server
    public void checkFavorite(int productId, CheckFavoriteCallback callback) {
        if (!sessionManager.isLoggedIn()) {
            callback.onResult(false);
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.CheckFavoriteResponse> call = apiService.checkFavorite(token, productId);

        call.enqueue(new Callback<ApiService.CheckFavoriteResponse>() {
            @Override
            public void onResponse(Call<ApiService.CheckFavoriteResponse> call, Response<ApiService.CheckFavoriteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isFavorite = response.body().is_favorite;

                    // Update cache
                    if (isFavorite) {
                        favoritesCache.add(productId);
                    } else {
                        favoritesCache.remove(productId);
                    }

                    callback.onResult(isFavorite);
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<ApiService.CheckFavoriteResponse> call, Throwable t) {
                Log.e(TAG, "Error checking favorite: " + t.getMessage());
                callback.onResult(false);
            }
        });
    }

    // Clear cache (call when user logs out)
    public void clearCache() {
        favoritesCache.clear();
        cacheInitialized = false;
    }
}
package com.example.jewelryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jewelryapp.R;
import com.example.jewelryapp.adapters.ProductAdapter;
import com.example.jewelryapp.models.Category;
import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.CartManager;
import com.example.jewelryapp.utils.FavoritesManager;
import com.example.jewelryapp.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "HOME_ACTIVITY";

    private TextView tvUserRole, tvCartCount, tvFavoritesCount;
    private EditText etSearch;
    private RecyclerView rvProducts;
    private ChipGroup chipGroupCategories;
    private CardView ivAddProduct, ivManageCategories, ivLogout, cartBadge, favoriteBadge;
    private LinearLayout layoutAdminButtons, btnHome, btnFavorites, btnCart;
    private CardView btnRingSize;

    private SessionManager sessionManager;
    private CartManager cartManager;
    private FavoritesManager favoritesManager;
    private ApiService apiService;
    private ProductAdapter productAdapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private String selectedCategory = "Tous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            sessionManager = new SessionManager(this);

            Log.d(TAG, "=== HOME ACTIVITY STARTING ===");
            Log.d(TAG, "isLoggedIn: " + sessionManager.isLoggedIn());

            if (!sessionManager.isLoggedIn()) {
                Log.d(TAG, "NOT LOGGED IN - Redirecting to Landing");
                redirectToLanding();
                return;
            }

            Log.d(TAG, "User is logged in: " + sessionManager.getUserName());

            setContentView(R.layout.activity_home);

            initViews();
            initManagers();
            setupUI();
            loadFavoritesCache();
            loadCategories();
            loadProducts();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            redirectToLanding();
        }
    }

    private void redirectToLanding() {
        Intent intent = new Intent(HomeActivity.this, LandingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        try {
            btnRingSize = findViewById(R.id.btnRingSize);
            tvUserRole = findViewById(R.id.tvUserRole);
            tvCartCount = findViewById(R.id.tvCartCount);
            tvFavoritesCount = findViewById(R.id.tvFavoritesCount);
            etSearch = findViewById(R.id.etSearch);
            rvProducts = findViewById(R.id.rvProducts);
            chipGroupCategories = findViewById(R.id.chipGroupCategories);
            ivAddProduct = findViewById(R.id.ivAddProduct);
            ivManageCategories = findViewById(R.id.ivManageCategories);
            ivLogout = findViewById(R.id.ivLogout);
            layoutAdminButtons = findViewById(R.id.layoutAdminButtons);
            btnHome = findViewById(R.id.btnHome);
            btnFavorites = findViewById(R.id.btnFavorites);
            btnCart = findViewById(R.id.btnCart);
            cartBadge = findViewById(R.id.cartBadge);
            favoriteBadge = findViewById(R.id.favoriteBadge);

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void initManagers() {
        try {
            cartManager = new CartManager(this);
            favoritesManager = new FavoritesManager(this);
            apiService = RetrofitClient.getApiService();
            Log.d(TAG, "Managers initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing managers: " + e.getMessage(), e);
            throw e;
        }
    }

    private void loadFavoritesCache() {
        try {
            favoritesManager.getFavorites(new FavoritesManager.FavoritesCallback() {
                @Override
                public void onSuccess(List<Product> favorites) {
                    Log.d(TAG, "Loaded " + favorites.size() + " favorites");
                    updateFavoritesBadge();
                    if (productAdapter != null) {
                        productAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error loading favorites: " + message);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadFavoritesCache: " + e.getMessage(), e);
        }
    }

    private void setupUI() {
        try {
            if (sessionManager.isAdmin()) {
                tvUserRole.setText("MODE ADMINISTRATEUR");
                layoutAdminButtons.setVisibility(View.VISIBLE);
            } else {
                tvUserRole.setText("CLIENT PRIVILÉGIÉ");
                layoutAdminButtons.setVisibility(View.GONE);
            }

            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            rvProducts.setLayoutManager(layoutManager);
            productAdapter = new ProductAdapter(this, filteredProducts, sessionManager.isAdmin());
            productAdapter.setOnProductClickListener(this);
            rvProducts.setAdapter(productAdapter);

            updateCartBadge();
            updateFavoritesBadge();

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterProducts(s.toString(), selectedCategory);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            ivAddProduct.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, AddProductActivity.class));
            });

            ivManageCategories.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, ManageCategoriesActivity.class));
            });

            ivLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Déconnexion")
                        .setMessage("Voulez-vous vraiment vous déconnecter?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            sessionManager.logout();
                            redirectToLanding();
                        })
                        .setNegativeButton("Non", null)
                        .show();
            });

            btnHome.setOnClickListener(v -> {});

            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            });

            btnFavorites.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, FavoritesActivity.class));
            });

            btnRingSize.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, RingSizeActivity.class));
            });

            Log.d(TAG, "UI setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error in setupUI: " + e.getMessage(), e);
        }
    }

    private void loadCategories() {
        Log.d(TAG, "=== LOADING CATEGORIES ===");

        Call<ApiService.CategoriesResponse> call = apiService.getCategories();

        call.enqueue(new Callback<ApiService.CategoriesResponse>() {
            @Override
            public void onResponse(Call<ApiService.CategoriesResponse> call, Response<ApiService.CategoriesResponse> response) {
                Log.d(TAG, "Categories Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategoriesResponse categoriesResponse = response.body();

                    if (categoriesResponse.success && categoriesResponse.data != null) {
                        categories = categoriesResponse.data;
                        Log.d(TAG, "Loaded " + categories.size() + " categories");
                        updateCategoryChips();
                    } else {
                        Log.e(TAG, "Categories response not successful");
                    }
                } else {
                    Log.e(TAG, "Failed to load categories: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiService.CategoriesResponse> call, Throwable t) {
                Log.e(TAG, "Categories network error: " + t.getMessage(), t);
            }
        });
    }

    private void updateCategoryChips() {
        chipGroupCategories.removeAllViews();

        Chip chipAll = new Chip(this);
        chipAll.setText("Tous");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setOnClickListener(v -> {
            selectedCategory = "Tous";
            filterProducts(etSearch.getText().toString(), selectedCategory);
        });
        chipGroupCategories.addView(chipAll);

        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                selectedCategory = category.getName();
                filterProducts(etSearch.getText().toString(), selectedCategory);
            });
            chipGroupCategories.addView(chip);
        }
    }

    private void loadProducts() {
        Log.d(TAG, "=== LOADING PRODUCTS ===");
        Log.d(TAG, "API URL: " + RetrofitClient.getClient().baseUrl());

        Call<ApiService.ProductsResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ApiService.ProductsResponse>() {
            @Override
            public void onResponse(Call<ApiService.ProductsResponse> call, Response<ApiService.ProductsResponse> response) {
                Log.d(TAG, "=== PRODUCTS RESPONSE ===");
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ProductsResponse productsResponse = response.body();

                    Log.d(TAG, "Success: " + productsResponse.success);
                    Log.d(TAG, "Message: " + productsResponse.message);
                    Log.d(TAG, "Data is null: " + (productsResponse.data == null));

                    if (productsResponse.data != null) {
                        Log.d(TAG, "Data.data is null: " + (productsResponse.data.data == null));

                        if (productsResponse.data.data != null) {
                            Log.d(TAG, "Products count: " + productsResponse.data.data.size());

                            if (!productsResponse.data.data.isEmpty()) {
                                Product firstProduct = productsResponse.data.data.get(0);
                                Log.d(TAG, "First product:");
                                Log.d(TAG, "  ID: " + firstProduct.getId());
                                Log.d(TAG, "  Name: " + firstProduct.getName());
                                Log.d(TAG, "  Price: " + firstProduct.getPrice());
                                Log.d(TAG, "  Image: " + firstProduct.getImage());
                                Log.d(TAG, "  Category ID: " + firstProduct.getCategoryId());
                            }

                            allProducts.clear();
                            allProducts.addAll(productsResponse.data.data);
                            filterProducts(etSearch.getText().toString(), selectedCategory);

                            Toast.makeText(HomeActivity.this,
                                    productsResponse.data.data.size() + " produits chargés",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "data.data is null");
                            Toast.makeText(HomeActivity.this,
                                    "Aucun produit disponible",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response data is null");
                        Toast.makeText(HomeActivity.this,
                                "Erreur de format de données",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());

                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "No error body";
                        Log.e(TAG, "Error body: " + errorBody);

                        // Show first 200 chars of error
                        String shortError = errorBody.length() > 200 ?
                                errorBody.substring(0, 200) + "..." : errorBody;
                        Toast.makeText(HomeActivity.this,
                                "Erreur " + response.code() + ": " + shortError,
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body: " + e.getMessage());
                        Toast.makeText(HomeActivity.this,
                                "Erreur " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.ProductsResponse> call, Throwable t) {
                Log.e(TAG, "=== API CALL FAILED ===");
                Log.e(TAG, "Error: " + t.getMessage());
                Log.e(TAG, "Error class: " + t.getClass().getName());
                t.printStackTrace();

                Toast.makeText(HomeActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterProducts(String searchText, String category) {
        filteredProducts.clear();

        for (Product product : allProducts) {
            boolean matchesSearch = searchText.isEmpty() ||
                    product.getName().toLowerCase().contains(searchText.toLowerCase());

            boolean matchesCategory = category.equals("Tous") ||
                    (product.getCategory() != null && product.getCategory().getName().equals(category));

            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }

        Log.d(TAG, "Filtered products: " + filteredProducts.size() + " out of " + allProducts.size());
        productAdapter.notifyDataSetChanged();
    }

    private void updateCartBadge() {
        try {
            int count = cartManager.getCartCount();
            if (count > 0) {
                cartBadge.setVisibility(View.VISIBLE);
                tvCartCount.setText(String.valueOf(count));
            } else {
                cartBadge.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating cart badge: " + e.getMessage());
        }
    }

    private void updateFavoritesBadge() {
        try {
            favoritesManager.getFavorites(new FavoritesManager.FavoritesCallback() {
                @Override
                public void onSuccess(List<Product> favorites) {
                    int count = favorites != null ? favorites.size() : 0;
                    if (count > 0) {
                        favoriteBadge.setVisibility(View.VISIBLE);
                        tvFavoritesCount.setText(String.valueOf(count));
                    } else {
                        favoriteBadge.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String message) {
                    favoriteBadge.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error updating favorites badge: " + e.getMessage());
        }
    }

    @Override
    public void onDeleteClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le produit")
                .setMessage("Êtes-vous sûr de vouloir supprimer " + product.getName() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deleteProduct(product.getId());
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public void onEditClick(Product product) {
        Intent intent = new Intent(HomeActivity.this, AddProductActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void deleteProduct(int productId) {
        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.DeleteResponse> call = apiService.deleteProduct(token, productId);

        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DeleteResponse deleteResponse = response.body();
                    if (deleteResponse.success) {
                        Toast.makeText(HomeActivity.this, "Produit supprimé", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(HomeActivity.this, "Erreur: " + deleteResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e(TAG, "Delete error: " + t.getMessage(), t);
                Toast.makeText(HomeActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateCartBadge();
            updateFavoritesBadge();
            loadFavoritesCache();
            loadProducts();
            loadCategories();
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }
}

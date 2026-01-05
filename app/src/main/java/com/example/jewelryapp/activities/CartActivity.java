package com.example.jewelryapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jewelryapp.R;
import com.example.jewelryapp.adapters.CartAdapter;
import com.example.jewelryapp.models.CartItem;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemListener {

    private ImageView ivBack;
    private RecyclerView rvCart;
    private LinearLayout layoutEmptyCart;
    private TextView tvSubtotal, tvTotal;
    private Button btnCheckout;
    private ImageView btnClearCart;  // FIXED: Changed from Button to ImageView

    private SessionManager sessionManager;
    private ApiService apiService;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        initViews();
        setupRecyclerView();
        loadCart();

        ivBack.setOnClickListener(v -> finish());

        btnClearCart.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Vider le panier")
                    .setMessage("Voulez-vous vraiment vider tout le panier ?")
                    .setPositiveButton("Oui", (dialog, which) -> clearCart())
                    .setNegativeButton("Non", null)
                    .show();
        });

        btnCheckout.setOnClickListener(v -> {
            Toast.makeText(this, "Fonction de paiement à venir", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvCart = findViewById(R.id.rvCart);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnClearCart = findViewById(R.id.btnClearCart);
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems);
        cartAdapter.setOnCartItemListener(this);
        rvCart.setAdapter(cartAdapter);
    }

    private void loadCart() {
        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.CartResponse> call = apiService.getCart(token);

        call.enqueue(new Callback<ApiService.CartResponse>() {
            @Override
            public void onResponse(Call<ApiService.CartResponse> call, Response<ApiService.CartResponse> response) {
                Log.d("CART", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CartResponse cartResponse = response.body();

                    if (cartResponse.success && cartResponse.data != null) {
                        cartItems.clear();
                        if (cartResponse.data.items != null) {
                            cartItems.addAll(cartResponse.data.items);
                        }

                        updateUI();
                        Log.d("CART", "Cart loaded: " + cartItems.size() + " items");
                    } else {
                        Log.e("CART", "Cart response not successful");
                        showEmptyCart();
                    }
                } else {
                    Log.e("CART", "Failed to load cart: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("CART", "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e("CART", "Error reading error body");
                    }
                    Toast.makeText(CartActivity.this, "Erreur de chargement du panier", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CartResponse> call, Throwable t) {
                Log.e("CART", "Error loading cart: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(CartActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyCart();
        } else {
            showCart();
            calculateTotal();
        }
    }

    private void showEmptyCart() {
        rvCart.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(false);
        btnClearCart.setEnabled(false);
        tvSubtotal.setText("0 MAD");
        tvTotal.setText("0 MAD");
    }

    private void showCart() {
        rvCart.setVisibility(View.VISIBLE);
        layoutEmptyCart.setVisibility(View.GONE);
        btnCheckout.setEnabled(true);
        btnClearCart.setEnabled(true);
        cartAdapter.notifyDataSetChanged();
    }

    private void calculateTotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct() != null) {
                try {
                    double price = Double.parseDouble(item.getProduct().getPrice());
                    subtotal += price * item.getQuantity();
                } catch (NumberFormatException e) {
                    Log.e("CART", "Error parsing price: " + e.getMessage());
                }
            }
        }

        double total = subtotal;

        tvSubtotal.setText(String.format("%.2f MAD", subtotal));
        tvTotal.setText(String.format("%.2f MAD", total));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        updateCartItem(item.getId(), newQuantity);
    }

    @Override
    public void onRemoveItem(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'article")
                .setMessage("Voulez-vous retirer cet article du panier ?")
                .setPositiveButton("Oui", (dialog, which) -> removeCartItem(item.getId()))
                .setNegativeButton("Non", null)
                .show();
    }

    private void updateCartItem(int cartId, int newQuantity) {
        String token = "Bearer " + sessionManager.getToken();

        ApiService.UpdateCartRequest request = new ApiService.UpdateCartRequest(newQuantity);
        Call<ApiService.CartItemResponse> call = apiService.updateCartItem(token, cartId, request);

        call.enqueue(new Callback<ApiService.CartItemResponse>() {
            @Override
            public void onResponse(Call<ApiService.CartItemResponse> call, Response<ApiService.CartItemResponse> response) {
                Log.d("CART_UPDATE", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CartItemResponse cartItemResponse = response.body();

                    if (cartItemResponse.success) {
                        Toast.makeText(CartActivity.this, "Quantité mise à jour", Toast.LENGTH_SHORT).show();
                        loadCart();
                    } else {
                        Toast.makeText(CartActivity.this, "Erreur: " + cartItemResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CART_UPDATE", "Update failed: " + response.code());
                    Toast.makeText(CartActivity.this, "Erreur de mise à jour", Toast.LENGTH_SHORT).show();
                    loadCart();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CartItemResponse> call, Throwable t) {
                Log.e("CART_UPDATE", "Error: " + t.getMessage());
                Toast.makeText(CartActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadCart();
            }
        });
    }

    private void removeCartItem(int cartId) {
        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.DeleteResponse> call = apiService.removeCartItem(token, cartId);

        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                Log.d("CART_REMOVE", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DeleteResponse deleteResponse = response.body();

                    if (deleteResponse.success) {
                        Toast.makeText(CartActivity.this, "Article retiré du panier", Toast.LENGTH_SHORT).show();
                        loadCart();
                    } else {
                        Toast.makeText(CartActivity.this, "Erreur: " + deleteResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CART_REMOVE", "Remove failed: " + response.code());
                    Toast.makeText(CartActivity.this, "Erreur de suppression", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e("CART_REMOVE", "Error: " + t.getMessage());
                Toast.makeText(CartActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearCart() {
        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.DeleteResponse> call = apiService.clearCart(token);

        call.enqueue(new Callback<ApiService.DeleteResponse>() {
            @Override
            public void onResponse(Call<ApiService.DeleteResponse> call, Response<ApiService.DeleteResponse> response) {
                Log.d("CART_CLEAR", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DeleteResponse deleteResponse = response.body();

                    if (deleteResponse.success) {
                        Toast.makeText(CartActivity.this, "Panier vidé", Toast.LENGTH_SHORT).show();
                        loadCart();
                    } else {
                        Toast.makeText(CartActivity.this, "Erreur: " + deleteResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("CART_CLEAR", "Clear failed: " + response.code());
                    Toast.makeText(CartActivity.this, "Erreur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.DeleteResponse> call, Throwable t) {
                Log.e("CART_CLEAR", "Error: " + t.getMessage());
                Toast.makeText(CartActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }
}

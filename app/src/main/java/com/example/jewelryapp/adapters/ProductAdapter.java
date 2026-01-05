package com.example.jewelryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jewelryapp.R;
import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.utils.FavoritesManager;
import com.example.jewelryapp.utils.SessionManager;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";
    private Context context;
    private List<Product> products;
    private FavoritesManager favoritesManager;
    private SessionManager sessionManager;
    private ApiService apiService;
    private boolean isAdmin;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onDeleteClick(Product product);
        void onEditClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, boolean isAdmin) {
        this.context = context;
        this.products = products;
        this.isAdmin = isAdmin;
        this.favoritesManager = new FavoritesManager(context);
        this.sessionManager = new SessionManager(context);
        this.apiService = RetrofitClient.getApiService();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getPrice() + " MAD");

        String image = product.getImage();
        Log.d(TAG, "Product: " + product.getName() + ", Image: " + image);

        if (image == null || image.isEmpty()) {
            // No image - show default emoji
            holder.tvProductEmoji.setVisibility(View.VISIBLE);
            holder.ivProductImage.setVisibility(View.GONE);
            holder.tvProductEmoji.setText("💍");
        } else if (image.startsWith("http://") || image.startsWith("https://")) {
            holder.tvProductEmoji.setVisibility(View.GONE);
            holder.ivProductImage.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(image)
                    .placeholder(R.drawable.ic_cart) // Use any default drawable
                    .error(R.drawable.ic_cart)
                    .into(holder.ivProductImage);

            Log.d(TAG, "Loading image from URL: " + image);
        } else {
            // It's an emoji or text - display as text
            holder.tvProductEmoji.setVisibility(View.VISIBLE);
            holder.ivProductImage.setVisibility(View.GONE);
            holder.tvProductEmoji.setText(image);
            holder.tvProductEmoji.setTextSize(48); // Make emoji bigger

            Log.d(TAG, "Displaying emoji: " + image);
        }

        // Favorite status
        boolean isFavorite = favoritesManager.isFavoriteCached(product.getId());
        holder.ivFavorite.setImageResource(isFavorite ?
                R.drawable.ic_heart_filled : R.drawable.ic_heart);

        // Admin controls
        if (isAdmin) {
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        } else {
            holder.ivDelete.setVisibility(View.GONE);
        }

        // Add to Cart
        holder.btnAddToCart.setOnClickListener(v -> {
            addToCartViaAPI(product);
        });

        // Favorite Toggle
        holder.ivFavorite.setOnClickListener(v -> {
            if (favoritesManager.isFavoriteCached(product.getId())) {
                favoritesManager.removeFromFavorites(product.getId(),
                        new FavoritesManager.FavoriteActionCallback() {
                            @Override
                            public void onSuccess() {
                                holder.ivFavorite.setImageResource(R.drawable.ic_heart);
                                Toast.makeText(context, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(context, "Erreur: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                favoritesManager.addToFavorites(product.getId(),
                        new FavoritesManager.FavoriteActionCallback() {
                            @Override
                            public void onSuccess() {
                                holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
                                Toast.makeText(context, "Ajouté aux favoris ❤", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(context, "Erreur: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // WhatsApp
        holder.ivWhatsApp.setOnClickListener(v -> {
            String message = "Bonjour, je suis intéressé par " +
                    product.getName() + " - " + product.getPrice() + " MAD";
            String url = "https://wa.me/?text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        });
    }

    private void addToCartViaAPI(Product product) {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sessionManager.getToken();
        ApiService.AddToCartRequest request = new ApiService.AddToCartRequest(
                product.getId(),
                1
        );

        Call<ApiService.CartItemResponse> call = apiService.addToCart(token, request);
        call.enqueue(new Callback<ApiService.CartItemResponse>() {
            @Override
            public void onResponse(Call<ApiService.CartItemResponse> call,
                                   Response<ApiService.CartItemResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CartItemResponse cartResponse = response.body();
                    if (cartResponse.success) {
                        Toast.makeText(context, "Ajouté au panier ✓", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, cartResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Erreur d'ajout au panier", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CartItemResponse> call, Throwable t) {
                Log.e(TAG, "Add to cart failed: " + t.getMessage());
                Toast.makeText(context, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivFavorite, ivDelete, ivWhatsApp;
        TextView tvProductName, tvProductPrice, tvProductEmoji;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductEmoji = itemView.findViewById(R.id.tvProductEmoji);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            ivWhatsApp = itemView.findViewById(R.id.ivWhatsApp);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}

package com.example.jewelryapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jewelryapp.R;
import com.example.jewelryapp.adapters.ProductAdapter;
import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.utils.FavoritesManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView rvFavorites;
    private LinearLayout layoutEmptyFavorites;

    private FavoritesManager favoritesManager;
    private ProductAdapter productAdapter;
    private List<Product> favoriteProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        initViews();
        setupRecyclerView();
        loadFavorites();

        ivBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvFavorites = findViewById(R.id.rvFavorites);
        layoutEmptyFavorites = findViewById(R.id.layoutEmptyFavorites);

        favoritesManager = new FavoritesManager(this);
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, favoriteProducts, false);
        rvFavorites.setAdapter(productAdapter);
    }

    private void loadFavorites() {
        favoritesManager.getFavorites(new FavoritesManager.FavoritesCallback() {
            @Override
            public void onSuccess(List<Product> favorites) {
                Log.d("FAVORITES", "Loaded " + favorites.size() + " favorites");
                favoriteProducts.clear();
                favoriteProducts.addAll(favorites);

                updateUI();
            }

            @Override
            public void onError(String message) {
                Log.e("FAVORITES", "Error loading favorites: " + message);
                showEmptyView();
            }
        });
    }

    private void updateUI() {
        if (favoriteProducts.isEmpty()) {
            showEmptyView();
        } else {
            showFavorites();
        }
    }

    private void showEmptyView() {
        rvFavorites.setVisibility(View.GONE);
        layoutEmptyFavorites.setVisibility(View.VISIBLE);
    }

    private void showFavorites() {
        rvFavorites.setVisibility(View.VISIBLE);
        layoutEmptyFavorites.setVisibility(View.GONE);
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}
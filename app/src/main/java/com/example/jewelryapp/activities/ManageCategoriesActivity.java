package com.example.jewelryapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jewelryapp.R;
import com.example.jewelryapp.models.Category;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCategoriesActivity extends AppCompatActivity {

    private ImageView ivBack;
    private Button btnAddCategory;
    private RecyclerView rvCategories;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        ivBack = findViewById(R.id.ivBack);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        rvCategories = findViewById(R.id.rvCategories);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        ivBack.setOnClickListener(v -> finish());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();
    }

    private void loadCategories() {
        Call<ApiService.CategoriesResponse> call = apiService.getCategories();
        call.enqueue(new Callback<ApiService.CategoriesResponse>() {
            @Override
            public void onResponse(Call<ApiService.CategoriesResponse> call, Response<ApiService.CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategoriesResponse categoriesResponse = response.body();

                    if (categoriesResponse.success && categoriesResponse.data != null) {
                        categories = categoriesResponse.data;
                        Log.d("MANAGE_CATEGORIES", "Categories loaded: " + categories.size());
                        // TODO: Setup adapter with categories list
                    } else {
                        Log.e("MANAGE_CATEGORIES", "Response not successful or data is null");
                        Toast.makeText(ManageCategoriesActivity.this, "Aucune catégorie disponible", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("MANAGE_CATEGORIES", "Failed to load categories: " + response.code());
                    Toast.makeText(ManageCategoriesActivity.this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CategoriesResponse> call, Throwable t) {
                Log.e("MANAGE_CATEGORIES", "Network error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(ManageCategoriesActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nouvelle Catégorie");

        final EditText input = new EditText(this);
        input.setHint("Nom de la catégorie");
        builder.setView(input);

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String categoryName = input.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                addCategory(categoryName);
            }
        });
        builder.setNegativeButton("Annuler", null);
        builder.show();
    }

    private void addCategory(String name) {
        Category category = new Category();
        category.setName(name);

        String token = "Bearer " + sessionManager.getToken();
        Call<ApiService.CategoryResponse> call = apiService.createCategory(token, category);

        call.enqueue(new Callback<ApiService.CategoryResponse>() {
            @Override
            public void onResponse(Call<ApiService.CategoryResponse> call, Response<ApiService.CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategoryResponse categoryResponse = response.body();

                    if (categoryResponse.success && categoryResponse.data != null) {
                        Toast.makeText(ManageCategoriesActivity.this, "Catégorie ajoutée!", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(ManageCategoriesActivity.this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("MANAGE_CATEGORIES", "Failed to add category: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("MANAGE_CATEGORIES", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("MANAGE_CATEGORIES", "Error reading error body");
                    }
                    Toast.makeText(ManageCategoriesActivity.this, "Erreur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.CategoryResponse> call, Throwable t) {
                Log.e("MANAGE_CATEGORIES", "Network error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(ManageCategoriesActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
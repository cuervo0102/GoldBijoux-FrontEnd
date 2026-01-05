package com.example.jewelryapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.jewelryapp.R;
import com.example.jewelryapp.models.Category;
import com.example.jewelryapp.models.Product;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "ADD_PRODUCT";
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int TAKE_PHOTO_REQUEST = 101;

    private ImageView ivBack;
    private TextView tvTitle;
    private EditText etProductName, etProductPrice, etProductDescription, etProductEmoji;
    private Spinner spinnerCategory;
    private Button btnSelectImage, btnSaveProduct;

    private SessionManager sessionManager;
    private ApiService apiService;
    private List<Category> categories = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    private Uri selectedImageUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Log.d(TAG, "=== AddProductActivity onCreate ===");

        initViews();
        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "User not logged in!");
            Toast.makeText(this, "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCategories();

        ivBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> {
            // Just select image, no permission needed for now
            chooseFromGallery();
        });
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductDescription = findViewById(R.id.etProductDescription);




        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
    }

    private void loadCategories() {
        Log.d(TAG, "=== Loading Categories ===");
        Call<ApiService.CategoriesResponse> call = apiService.getCategories();

        call.enqueue(new Callback<ApiService.CategoriesResponse>() {
            @Override
            public void onResponse(Call<ApiService.CategoriesResponse> call, Response<ApiService.CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.CategoriesResponse categoriesResponse = response.body();

                    if (categoriesResponse.success && categoriesResponse.data != null) {
                        categories = categoriesResponse.data;

                        categoryNames.clear();
                        for (Category category : categories) {
                            categoryNames.add(category.getName());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                AddProductActivity.this,
                                android.R.layout.simple_spinner_item,
                                categoryNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategory.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.CategoriesResponse> call, Throwable t) {
                Log.e(TAG, "Error loading categories: " + t.getMessage(), t);
                Toast.makeText(AddProductActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                selectedImageUri = data.getData();
                Toast.makeText(this, "Image sélectionnée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir le nom et le prix", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerCategory.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = spinnerCategory.getSelectedItemPosition();
        Category selectedCategory = categories.get(selectedPosition);
        String cleanPrice = priceStr.replaceAll("[^0-9.]", "");


        uploadProductWithoutImage(name, cleanPrice, description, selectedCategory.getId());
    }


    private void uploadProductWithoutImage(String name, String price, String description, int categoryId) {
        String token = "Bearer " + sessionManager.getToken();

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        product.setCategoryId(categoryId);
        product.setImage("💍");  // Default emoji
        product.setStock(0);
        product.setRequiresRingSize(false);
        product.setActive(true);

        Log.d(TAG, "Creating product: " + name);

        Call<ApiService.ProductResponse> call = apiService.createProduct(token, product);

        call.enqueue(new Callback<ApiService.ProductResponse>() {
            @Override
            public void onResponse(Call<ApiService.ProductResponse> call, Response<ApiService.ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.ProductResponse productResponse = response.body();

                    if (productResponse.success) {
                        Toast.makeText(AddProductActivity.this, "Produit ajouté!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Erreur: " + productResponse.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddProductActivity.this, "Erreur: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.ProductResponse> call, Throwable t) {
                Toast.makeText(AddProductActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}

package com.example.jewelryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jewelryapp.R;
import com.example.jewelryapp.models.User;
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("REGISTER_DEBUG", "Attempting registration for: " + email);

        ApiService.RegisterRequest request = new ApiService.RegisterRequest(name, email, phone, password);
        Call<ApiService.RegisterResponse> call = apiService.register(request);

        call.enqueue(new Callback<ApiService.RegisterResponse>() {
            @Override
            public void onResponse(Call<ApiService.RegisterResponse> call, Response<ApiService.RegisterResponse> response) {
                Log.d("REGISTER_DEBUG", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.RegisterResponse registerResponse = response.body();


                    User user = registerResponse.getUser();
                    String token = registerResponse.getToken();

                    Log.d("REGISTER_DEBUG", "=== REGISTER SUCCESS ===");
                    Log.d("REGISTER_DEBUG", "Success: " + registerResponse.success);
                    Log.d("REGISTER_DEBUG", "Message: " + registerResponse.message);
                    Log.d("REGISTER_DEBUG", "User: " + (user != null ? user.getName() : "null"));
                    Log.d("REGISTER_DEBUG", "Email: " + (user != null ? user.getEmail() : "null"));
                    Log.d("REGISTER_DEBUG", "Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

                    if (user != null && token != null) {
                        sessionManager.createLoginSession(user, token);


                        Log.d("REGISTER_DEBUG", "Session saved - isLoggedIn: " + sessionManager.isLoggedIn());

                        Toast.makeText(RegisterActivity.this, "Inscription réussie!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e("REGISTER_DEBUG", "User or token is null");
                        Toast.makeText(RegisterActivity.this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("REGISTER_DEBUG", "Register failed - Response code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown";
                        Log.e("REGISTER_DEBUG", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("REGISTER_DEBUG", "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(RegisterActivity.this, "Erreur lors de l'inscription", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.RegisterResponse> call, Throwable t) {
                Log.e("REGISTER_DEBUG", "Register error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(RegisterActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
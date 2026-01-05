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

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminEmail, etAdminPassword;
    private Button btnAdminLogin;
    private TextView tvBackToLogin;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        etAdminEmail = findViewById(R.id.etAdminEmail);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnAdminLogin.setOnClickListener(v -> loginAdmin());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void loginAdmin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ADMIN_LOGIN_DEBUG", "Attempting admin login for: " + email);

        ApiService.LoginRequest request = new ApiService.LoginRequest(email, password);
        Call<ApiService.LoginResponse> call = apiService.login(request);

        call.enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call, Response<ApiService.LoginResponse> response) {
                Log.d("ADMIN_LOGIN_DEBUG", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.LoginResponse loginResponse = response.body();


                    User user = loginResponse.getUser();
                    String token = loginResponse.getToken();


                    Log.d("ADMIN_LOGIN_DEBUG", "=== ADMIN LOGIN RESPONSE ===");
                    Log.d("ADMIN_LOGIN_DEBUG", "Success: " + loginResponse.success);
                    Log.d("ADMIN_LOGIN_DEBUG", "User: " + (user != null ? user.getName() : "null"));
                    Log.d("ADMIN_LOGIN_DEBUG", "Role: " + (user != null ? user.getRole() : "null"));
                    Log.d("ADMIN_LOGIN_DEBUG", "Is Admin: " + (user != null ? user.isAdmin() : "false"));

                    if (user != null && token != null && user.isAdmin()) {
                        sessionManager.createLoginSession(user, token);


                        Log.d("ADMIN_LOGIN_DEBUG", "Session saved - isLoggedIn: " + sessionManager.isLoggedIn());
                        Log.d("ADMIN_LOGIN_DEBUG", "Session isAdmin: " + sessionManager.isAdmin());

                        Toast.makeText(AdminLoginActivity.this, "Connexion admin réussie!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminLoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else if (user != null && !user.isAdmin()) {
                        Log.w("ADMIN_LOGIN_DEBUG", "User is not an admin");
                        Toast.makeText(AdminLoginActivity.this, "Accès refusé: Vous n'êtes pas administrateur", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("ADMIN_LOGIN_DEBUG", "User or token is null");
                        Toast.makeText(AdminLoginActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ADMIN_LOGIN_DEBUG", "Login failed - Response code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown";
                        Log.e("ADMIN_LOGIN_DEBUG", "Error body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("ADMIN_LOGIN_DEBUG", "Error reading error body: " + e.getMessage());
                    }
                    Toast.makeText(AdminLoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                Log.e("ADMIN_LOGIN_DEBUG", "Login error: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(AdminLoginActivity.this, "Erreur: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
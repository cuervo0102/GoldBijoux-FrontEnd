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
import com.example.jewelryapp.network.ApiService;
import com.example.jewelryapp.network.RetrofitClient;
import com.example.jewelryapp.utils.SessionManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN_ACTIVITY";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "LoginActivity onCreate started");

        try {
            initViews();
            sessionManager = new SessionManager(this);
            apiService = RetrofitClient.getApiService();

            // Check if views were found
            if (btnLogin == null) {
                Log.e(TAG, "ERROR: btnLogin is null! Check activity_login.xml has android:id=\"@+id/btnLogin\"");
                Toast.makeText(this, "Erreur de layout - vérifiez btnLogin", Toast.LENGTH_LONG).show();
                return;
            }

            if (etEmail == null) {
                Log.e(TAG, "ERROR: etEmail is null! Check activity_login.xml has android:id=\"@+id/etEmail\"");
                Toast.makeText(this, "Erreur de layout - vérifiez etEmail", Toast.LENGTH_LONG).show();
                return;
            }

            if (etPassword == null) {
                Log.e(TAG, "ERROR: etPassword is null! Check activity_login.xml has android:id=\"@+id/etPassword\"");
                Toast.makeText(this, "Erreur de layout - vérifiez etPassword", Toast.LENGTH_LONG).show();
                return;
            }

            btnLogin.setOnClickListener(v -> performLogin());

            if (tvRegister != null) {
                tvRegister.setOnClickListener(v -> {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                });
            } else {
                Log.w(TAG, "tvRegister not found in layout (optional)");
            }

            Log.d(TAG, "LoginActivity initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        // Try to find views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Log which views were found
        Log.d(TAG, "etEmail found: " + (etEmail != null));
        Log.d(TAG, "etPassword found: " + (etPassword != null));
        Log.d(TAG, "btnLogin found: " + (btnLogin != null));
        Log.d(TAG, "tvRegister found: " + (tvRegister != null));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Attempting login for: " + email);

        ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(email, password);
        Call<ApiService.LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<ApiService.LoginResponse>() {
            @Override
            public void onResponse(Call<ApiService.LoginResponse> call, Response<ApiService.LoginResponse> response) {
                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.LoginResponse loginResponse = response.body();

                    if (loginResponse.success && loginResponse.data != null) {
                        sessionManager.saveSession(
                                loginResponse.getUser(),
                                loginResponse.getToken()
                        );

                        Toast.makeText(LoginActivity.this,
                                "Bienvenue " + loginResponse.getUser().getName(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                loginResponse.message,
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        ResponseBody errorBody = response.errorBody();
                        if (errorBody != null) {
                            String errorString = errorBody.string();
                            Log.e(TAG, "Error response: " + errorString);

                            String shortError = errorString.length() > 200 ?
                                    errorString.substring(0, 200) : errorString;

                            Toast.makeText(LoginActivity.this,
                                    "Erreur: " + shortError,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Erreur " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                        Toast.makeText(LoginActivity.this,
                                "Erreur " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiService.LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login failed", t);

                String errorMessage;

                if (t instanceof java.net.ConnectException) {
                    errorMessage = "Impossible de se connecter au serveur\nVérifiez que Laravel est démarré";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Hôte introuvable\nVérifiez l'adresse IP dans RetrofitClient";
                } else if (t instanceof com.google.gson.JsonSyntaxException) {
                    errorMessage = "Laravel retourne du HTML au lieu de JSON\n\nVérifiez:\n1. Base de données connectée\n2. Table 'users' existe\n3. Utilisateur existe";
                } else {
                    errorMessage = "Erreur: " + t.getMessage();
                }

                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
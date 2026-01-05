package com.example.jewelryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.jewelryapp.R;
import com.example.jewelryapp.utils.SessionManager;

public class LandingActivity extends AppCompatActivity {

    private CardView btnLogin, btnRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SessionManager properly
        sessionManager = new SessionManager(this);

        // Debug log
        Log.d("LandingActivity", "Checking session - isLoggedIn: " + sessionManager.isLoggedIn());

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            Log.d("LandingActivity", "User already logged in, redirecting to Home");
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_landing);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (sessionManager != null && sessionManager.isLoggedIn()) {
            Log.d("LandingActivity", "onResume - User logged in, redirecting to Home");
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
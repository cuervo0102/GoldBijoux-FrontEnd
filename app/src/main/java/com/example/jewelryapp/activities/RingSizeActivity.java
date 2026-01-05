package com.example.jewelryapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jewelryapp.R;

import java.util.Locale;

public class RingSizeActivity extends AppCompatActivity {

    private com.goldbijoux.maboutique.RingCircleView ringCircleView;
    private ImageView btnBack;
    private Button btnIncrease, btnDecrease;
    private Button btnSize50, btnSize52, btnSize54, btnSize56;
    private Button btnConfirmSize;
    private TextView tvCurrentSize, tvUSSize, tvDiameter;

    // Ring size data (France size, US size, diameter in mm)
    private static final float[][] RING_SIZES = {
            {49, 4.5f, 15.6f},
            {50, 5.0f, 15.9f},
            {51, 5.5f, 16.2f},
            {52, 6.0f, 16.6f},
            {53, 6.5f, 16.9f},
            {54, 7.0f, 17.2f},
            {55, 7.5f, 17.5f},
            {56, 8.0f, 17.8f},
            {57, 8.5f, 18.1f},
            {58, 9.0f, 18.4f},
            {59, 9.5f, 18.7f},
            {60, 10.0f, 19.0f}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_size);

        ringCircleView = findViewById(R.id.ringCircleView);
        btnBack = findViewById(R.id.btnBack);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnSize50 = findViewById(R.id.btnSize50);
        btnSize52 = findViewById(R.id.btnSize52);
        btnSize54 = findViewById(R.id.btnSize54);
        btnSize56 = findViewById(R.id.btnSize56);
        btnConfirmSize = findViewById(R.id.btnConfirmSize);
        tvCurrentSize = findViewById(R.id.tvCurrentSize);
        tvUSSize = findViewById(R.id.tvUSSize);
        tvDiameter = findViewById(R.id.tvDiameter);

        updateRingSize(16.6f);

        btnBack.setOnClickListener(v -> finish());

        btnIncrease.setOnClickListener(v -> {
            ringCircleView.increaseDiameter();
            updateDisplayedSize();
        });

        btnDecrease.setOnClickListener(v -> {
            ringCircleView.decreaseDiameter();
            updateDisplayedSize();
        });

        btnSize50.setOnClickListener(v -> updateRingSize(15.9f)); // Size 50
        btnSize52.setOnClickListener(v -> updateRingSize(16.6f)); // Size 52
        btnSize54.setOnClickListener(v -> updateRingSize(17.2f)); // Size 54
        btnSize56.setOnClickListener(v -> updateRingSize(17.8f)); // Size 56

        btnConfirmSize.setOnClickListener(v -> {
            float diameter = ringCircleView.getDiameterMm();
            String sizeInfo = getRingSizeFromDiameter(diameter);
            Toast.makeText(RingSizeActivity.this,
                    "Taille confirmée: " + sizeInfo,
                    Toast.LENGTH_LONG).show();


            finish(); // Go back
        });
    }

    private void updateRingSize(float diameterMm) {
        ringCircleView.setDiameterMm(diameterMm);
        updateDisplayedSize();
    }

    private void updateDisplayedSize() {
        float diameter = ringCircleView.getDiameterMm();
        String[] sizeInfo = getRingSizeInfo(diameter);

        tvCurrentSize.setText(sizeInfo[0]); // FR size
        tvUSSize.setText("US " + sizeInfo[1]); // US size
        tvDiameter.setText(String.format(Locale.getDefault(), "Ø %.1f mm", diameter));
    }

    private String[] getRingSizeInfo(float diameter) {
        // Find closest ring size
        float minDiff = Float.MAX_VALUE;
        int closestIndex = 0;

        for (int i = 0; i < RING_SIZES.length; i++) {
            float diff = Math.abs(RING_SIZES[i][2] - diameter);
            if (diff < minDiff) {
                minDiff = diff;
                closestIndex = i;
            }
        }

        String frSize = String.valueOf((int) RING_SIZES[closestIndex][0]);
        String usSize = String.format(Locale.getDefault(), "%.1f", RING_SIZES[closestIndex][1]);

        return new String[]{frSize, usSize};
    }

    private String getRingSizeFromDiameter(float diameter) {
        String[] sizeInfo = getRingSizeInfo(diameter);
        return "FR " + sizeInfo[0] + " (US " + sizeInfo[1] + ") - " +
                String.format(Locale.getDefault(), "Ø %.1f mm", diameter);
    }
}
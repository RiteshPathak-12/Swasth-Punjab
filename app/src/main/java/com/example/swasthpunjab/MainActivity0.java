package com.example.swasthpunjab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity0 extends AppCompatActivity {

    Button hindiBtn, englishBtn, punjabiBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main0);

        // Initialize buttons
        hindiBtn = findViewById(R.id.hindi);
        englishBtn = findViewById(R.id.english);
        punjabiBtn = findViewById(R.id.punjabi);

        // Set click listeners
        hindiBtn.setOnClickListener(v -> setLanguageAndProceed("hi"));
        englishBtn.setOnClickListener(v -> setLanguageAndProceed("en"));
        punjabiBtn.setOnClickListener(v -> setLanguageAndProceed("pa"));
    }

    private void setLanguageAndProceed(String langCode) {
        // Save selected language
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("language", langCode).apply();

        Intent intent = new Intent(MainActivity0.this, main1.class);
        startActivity(intent);
        finish();
    }
}
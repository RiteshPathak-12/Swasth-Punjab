package com.example.swasthpunjab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class opening extends AppCompatActivity {
    TextView welcome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opening);
        welcome=findViewById(R.id.welcome);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is already logged in
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String email = prefs.getString("patient_email", null);

                if (email != null && !email.isEmpty()) {
                    // User is already logged in, skip to Dashboard
                    Intent intent = new Intent(opening.this, Dashboard.class);
                    startActivity(intent);
                } else {
                    // Start fresh
                    Intent intent = new Intent(opening.this, MainActivity0.class);
                    startActivity(intent);
                }
                finish();
            }
        },3000);
    }
}
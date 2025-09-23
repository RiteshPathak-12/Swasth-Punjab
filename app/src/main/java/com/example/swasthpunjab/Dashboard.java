package com.example.swasthpunjab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class Dashboard extends AppCompatActivity {

    ImageView doctor_photo;
    Toolbar toolbar;
    TextView Consultation,totalConsultation;
    Button Consult_Now,medical_shop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        doctor_photo=findViewById(R.id.doctor_photo);
        Consultation=findViewById(R.id.Consultation);
        Consult_Now=findViewById(R.id.Consult_Now);
        medical_shop=findViewById(R.id.medical_shop);
        toolbar=findViewById(R.id.toolbar);
        totalConsultation=findViewById(R.id.totalConsultation);
        Consult_Now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Dashboard.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        medical_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Dashboard.this, Medical_Shop_list.class);
                startActivity(intent);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setSubtitle("toolbar");
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String targetLang = prefs.getString("language", "en");
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang))
                .build();

        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    translator.translate(Consult_Now.getText().toString())
                            .addOnSuccessListener(translated -> Consult_Now.setText(translated));

                    translator.translate(Consultation.getText().toString())
                            .addOnSuccessListener(translated -> Consultation.setText(translated));
                    translator.translate(medical_shop.getText().toString())
                            .addOnSuccessListener(translated -> medical_shop.setText(translated));
                    translator.translate(totalConsultation.getText().toString())
                            .addOnSuccessListener(translated -> totalConsultation.setText(translated));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.bottom_nav_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            Intent intent = new Intent(Dashboard.this, PatientHistory.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
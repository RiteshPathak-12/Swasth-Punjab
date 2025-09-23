package com.example.swasthpunjab;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class Doctor_register extends AppCompatActivity {
    EditText nameInput,emailInput,passwordInput,licenseInput,specialtyInput,experienceInput;
    Button registerButton,all_doctor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_register);
        nameInput=findViewById(R.id.nameInput);
        emailInput=findViewById(R.id.emailInput);
        passwordInput=findViewById(R.id.passwordInput);
        licenseInput=findViewById(R.id.licenseInput);
        specialtyInput=findViewById(R.id.specialtyInput);
        experienceInput=findViewById(R.id.experienceInput);
        registerButton=findViewById(R.id.registerButton);
        all_doctor=findViewById(R.id.all_doctor);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String license = licenseInput.getText().toString().trim();
                String specialty = specialtyInput.getText().toString().trim();

                if (name.isEmpty() || license.isEmpty() || specialty.isEmpty()) {
                    Toast.makeText(Doctor_register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                MyDBHelper dbHelper = new MyDBHelper(Doctor_register.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(MyDBHelper.COLUMN_NAME, name);
                values.put(MyDBHelper.COLUMN_LICENSE, license);
                values.put(MyDBHelper.COLUMN_SPECIALTY, specialty);

                long rowId = db.insert(MyDBHelper.TABLE_DOCTORS, null, values);
                if (rowId != -1) {
                    Toast.makeText(Doctor_register.this, "Doctor registered successfully", Toast.LENGTH_SHORT).show();
                    nameInput.setText("");
                    licenseInput.setText("");
                    specialtyInput.setText("");
                } else {
                    Toast.makeText(Doctor_register.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }

                db.close();
            }
        });

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
                    translator.translate(nameInput.getText().toString())
                            .addOnSuccessListener(translated -> nameInput.setText(translated));

                    translator.translate(emailInput.getText().toString())
                            .addOnSuccessListener(translated -> emailInput.setText(translated));
                    translator.translate(passwordInput.getText().toString())
                            .addOnSuccessListener(translated -> passwordInput.setText(translated));
                    translator.translate(licenseInput.getText().toString())
                            .addOnSuccessListener(translated -> licenseInput.setText(translated));
                    translator.translate(specialtyInput.getText().toString())
                            .addOnSuccessListener(translated -> specialtyInput.setText(translated));
                    translator.translate(experienceInput.getText().toString())
                            .addOnSuccessListener(translated -> experienceInput.setText(translated));
                    translator.translate(registerButton.getText().toString())
                            .addOnSuccessListener(translated -> registerButton.setText(translated));
                    translator.translate(all_doctor.getText().toString())
                            .addOnSuccessListener(translated -> all_doctor.setText(translated));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show();
                });

        all_doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Doctor_register.this, DoctorListActivity.class);
                startActivity(intent);
            }
        });

    }
}
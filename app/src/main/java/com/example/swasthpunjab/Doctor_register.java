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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class Doctor_register extends AppCompatActivity {
    EditText nameInput,emailInput,passwordInput,licenseInput,specialtyInput,experienceInput;
    Button registerButton,all_doctor;

    // Firebase Reference
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_register);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("doctors");

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
                String email = emailInput.getText().toString().trim();
                String experience = experienceInput.getText().toString().trim();

                if (name.isEmpty() || license.isEmpty() || specialty.isEmpty()) {
                    Toast.makeText(Doctor_register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String password = passwordInput.getText().toString().trim();
                if (password.isEmpty()) {
                     Toast.makeText(Doctor_register.this, "Password is required", Toast.LENGTH_SHORT).show();
                     return;
                }

                registerDoctor(name, specialty, license, experience, email, password);
            }
        });
        
        // Explicitly set text to avoid confusion
        all_doctor.setText("Already Registered? Login");
        
        all_doctor.setOnClickListener(v -> {
            // Redirect to the Login Page
            Intent intent = new Intent(Doctor_register.this, DoctorLoginActivity.class);
            startActivity(intent);
            finish(); 
        });

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String targetLang = prefs.getString("language", "en");
        String langTag = TranslateLanguage.fromLanguageTag(targetLang);
        if (langTag == null) {
            langTag = TranslateLanguage.ENGLISH;
        }

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(langTag)
                .build();

        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    if (nameInput.getHint() != null)
                        translator.translate(nameInput.getHint().toString())
                                .addOnSuccessListener(translated -> nameInput.setHint(translated));
                    
                    if (emailInput.getHint() != null)
                        translator.translate(emailInput.getHint().toString())
                                .addOnSuccessListener(translated -> emailInput.setHint(translated));

                    if (passwordInput.getHint() != null)
                        translator.translate(passwordInput.getHint().toString())
                                .addOnSuccessListener(translated -> passwordInput.setHint(translated));

                    if (licenseInput.getHint() != null)
                        translator.translate(licenseInput.getHint().toString())
                                .addOnSuccessListener(translated -> licenseInput.setHint(translated));

                    if (specialtyInput.getHint() != null)
                        translator.translate(specialtyInput.getHint().toString())
                                .addOnSuccessListener(translated -> specialtyInput.setHint(translated));

                    if (experienceInput.getHint() != null)
                        translator.translate(experienceInput.getHint().toString())
                                .addOnSuccessListener(translated -> experienceInput.setHint(translated));

                    translator.translate(registerButton.getText().toString())
                            .addOnSuccessListener(translated -> registerButton.setText(translated));
                    translator.translate(all_doctor.getText().toString())
                            .addOnSuccessListener(translated -> all_doctor.setText(translated));
                })
                .addOnFailureListener(e -> {
                    // Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show();
                });

        // Removed conflicting listener that redirected to Patient View (DoctorListActivity)
        // The correct listener for Doctor Dashboard is defined above.
    }

    private void registerDoctor(String name, String specialty, String license, String experience, String email, String password) {
        String doctorId = mDatabase.push().getKey();
        Doctor doctor = new Doctor(name, specialty, license, experience, email, password);

        if (doctorId != null) {
            mDatabase.child(doctorId).setValue(doctor)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Doctor_register.this, "Doctor Registered! Please Login.", Toast.LENGTH_LONG).show();
                            // Redirect to Login
                            Intent intent = new Intent(Doctor_register.this, DoctorLoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Doctor_register.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
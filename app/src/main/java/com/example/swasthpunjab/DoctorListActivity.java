package com.example.swasthpunjab;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;

public class DoctorListActivity extends AppCompatActivity {

    ListView doctorListView;
    ArrayList<String> doctorNames = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private Translator translator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        doctorListView = findViewById(R.id.doctorListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorNames);
        doctorListView.setAdapter(adapter);

        // -------- ML KIT TRANSLATION SETUP --------
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String targetLangTag = prefs.getString("language", "en");

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLangTag))
                .build();

        translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> loadDoctorsWithTranslation())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show()
                );

        // -------- Handle doctor selection --------
        doctorListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDoctorWithSpecialty = doctorNames.get(position);
            // Extract just the name if needed, or pass the full string
            // Assuming format "Name - Specialty"
            String selectedDoctor = selectedDoctorWithSpecialty.split(" - ")[0];

            Intent intent = new Intent(
                    DoctorListActivity.this,
                    Activity_appointment_booking.class
            );
            intent.putExtra("DOCTOR_NAME", selectedDoctor);
            
            // Pass symptom summary if available
            if (getIntent().hasExtra("SYMPTOM_SUMMARY")) {
                intent.putExtra("SYMPTOM_SUMMARY", getIntent().getStringExtra("SYMPTOM_SUMMARY"));
            }
            
            startActivity(intent);
        });
    }

    // -------- Fetch doctors from FIREBASE & translate specialty --------
    private void loadDoctorsWithTranslation() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("doctors");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorNames.clear(); // Clear old list

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Doctor doctor = postSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        String name = doctor.getName();
                        String specialty = doctor.getSpecialty();

                        // Translate ONLY specialty
                        translator.translate(specialty)
                                .addOnSuccessListener(translatedSpecialty -> {
                                    doctorNames.add(name + " - " + translatedSpecialty);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    doctorNames.add(name + " - " + specialty);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DoctorListActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (translator != null) {
            translator.close();
            translator = null;
        }
    }
}

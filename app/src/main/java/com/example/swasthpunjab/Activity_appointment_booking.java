package com.example.swasthpunjab;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Added import for Dialog

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;

public class Activity_appointment_booking extends AppCompatActivity {

    private String doctorName;
    private String selectedDate = "";
    private String selectedTime = "";

    // ML Kit Translator
    private Translator translator;

    // Views
    private TextView tvDoctor, tvDate, tvTime;
    private Button btnDate, btnTime, btnConfirm;
    private EditText etPatientName, etPurpose;
    private String doctorEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        // ---- Bind Views ----
        tvDoctor = findViewById(R.id.tvSelectedDoctor);
        tvDate = findViewById(R.id.tvSelectedDate);
        tvTime = findViewById(R.id.tvSelectedTime);
        btnDate = findViewById(R.id.btnPickDate);
        btnTime = findViewById(R.id.btnPickTime);
        btnConfirm = findViewById(R.id.btnConfirmBooking);
        etPatientName = findViewById(R.id.etPatientName);
        etPurpose = findViewById(R.id.etPurpose);

        // ---- Get Doctor Name ----
        doctorName = getIntent().getStringExtra("DOCTOR_NAME");
        tvDoctor.setText("Consulting with: " + doctorName);
        
        // ---- Pre-fill Symptoms from Chatbot ----
        if (getIntent().hasExtra("SYMPTOM_SUMMARY")) {
            String summary = getIntent().getStringExtra("SYMPTOM_SUMMARY");
            etPurpose.setText(summary);
        }
        
        fetchDoctorEmail();

        // ---- Date Picker ----
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                tvDate.setText(selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ---- Time Picker ----
        btnTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedTime = hourOfDay + ":" + String.format("%02d", minute);
                tvTime.setText(selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // ---- Confirm Booking ----
        btnConfirm.setOnClickListener(v -> initiateBooking());
        
        setupTranslation();
    }

    private void initiateBooking() {
        String patientName = etPatientName.getText().toString().trim();
        String purpose = etPurpose.getText().toString().trim();
        
        if (patientName.isEmpty()) {
            etPatientName.setError("Enter Patient Name");
            return;
        }
        if (purpose.isEmpty()) {
            etPurpose.setError("Enter Purpose/Symptoms");
            return;
        }
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select Date and Time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Email from SharedPrefs
        SharedPreferences prefsInfo = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String patientEmail = prefsInfo.getString("patient_email", "");

        if (patientEmail.isEmpty()) {
            Toast.makeText(this, "Error: No registered email found. Please Login.", Toast.LENGTH_LONG).show();
            return;
        }

        checkConflictAndBook(patientName, patientEmail);
    }

    private void checkConflictAndBook(String pName, String pEmail) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
            Date date = sdf.parse(selectedDate + " " + selectedTime);
            if (date == null) return;
            long requestedTime = date.getTime();
            long fifteenMinutes = 15 * 60 * 1000;
            String purpose = etPurpose.getText().toString().trim();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");

            // Query appointments for this doctor
            ref.orderByChild("doctorName").equalTo(doctorName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean conflict = false;
                            long conflictingEndTime = 0;

                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Appointment apt = ds.getValue(Appointment.class);
                                if (apt != null) {
                                    long existingTime = apt.getTimestamp();
                                    // Check for overlap: |Requested - Existing| < 15 mins
                                    if (Math.abs(requestedTime - existingTime) < fifteenMinutes) {
                                        conflict = true;
                                        conflictingEndTime = existingTime + fifteenMinutes;
                                        break;
                                    }
                                }
                            }

                            if (conflict) {
                                String nextSlot = sdf.format(new Date(conflictingEndTime));
                                
                                new AlertDialog.Builder(Activity_appointment_booking.this)
                                    .setTitle("Slot Unavailable")
                                    .setMessage("Doctor is currently in consultation with another patient.\n\nPlease try again after 15 minutes.\n\nNext available slot: " + nextSlot)
                                    .setPositiveButton("OK", null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();

                            } else {
                                // No conflict - Proceed to Book
                                String key = ref.push().getKey();
                                if (key != null) {
                                    Appointment newApt = new Appointment(key, pName, doctorName,
                                            selectedDate, selectedTime,
                                            pEmail, purpose, "Booked", requestedTime);
                                    ref.child(key).setValue(newApt);

                                    // Keep local DB for now as backup/legacy support
                                    MyDBHelper dbHelper = new MyDBHelper(Activity_appointment_booking.this);
                                    dbHelper.insertAppointment(pName, doctorName, selectedDate, selectedTime, pEmail, "Booked");

                                    sendAppointmentEmails(key, pName, pEmail, doctorEmail, selectedDate, selectedTime, purpose);

                                    Toast.makeText(Activity_appointment_booking.this, "Booking Successful!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(Activity_appointment_booking.this, Dashboard.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Activity_appointment_booking.this, "Error checking availability: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing date", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendAppointmentEmails(String appointmentId, String pName, String pEmail, String dEmail, String date, String time, String purpose) {
        // Generate Video Call Link using 8x8.vc (Jitsi-based but often more stable for open links)
        // This ensures the doctor (on web) and patient (in-app WebView) are in the same room.
        String videoLink = "https://8x8.vc/SwasthPunjab_" + doctorName.replaceAll("\\s+", "");

        // 1. Email to Patient
        String subjectPatient = "Appointment Confirmed - Swasth Punjab";
        String bodyPatient = "Dear " + pName + ",\n\n" +
                "Your appointment with Dr. " + doctorName + " has been successfully booked.\n\n" +
                "📅 Date: " + date + "\n" +
                "⏰ Time: " + time + "\n" +
                "Purpose: " + purpose + "\n\n" +
                "Join Video Call via this link:\n" + videoLink + "\n\n" +
                "NOTE: If the meeting has not started, please wait for the doctor to join.\n\n" +
                "Thank you for choosing Swasth Punjab.\n" +
                "Wishing you good health!\n\n" +
                "Team Swasth Punjab";

        new Thread(() -> {
            try {
                EmailSender.sendEmail(pEmail, subjectPatient, bodyPatient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 2. Email to Doctor
        if (dEmail != null && !dEmail.isEmpty()) {
            String subjectDoctor = "New Appointment: " + pName;
            String bodyDoctor = "Dear Dr. " + doctorName + ",\n\n" +
                    "A patient has booked an appointment with you.\n\n" +
                    "Patient Name: " + pName + "\n" +
                    "Date: " + date + "\n" +
                    "Time: " + time + "\n" +
                    "Purpose/Reason: " + purpose + "\n\n" +
                    "Please join the consultation at the scheduled time using the link below:\n" +
                    videoLink + "\n\n" +
                    "Thank you for supporting Swasth Punjab.";

            new Thread(() -> {
                try {
                    EmailSender.sendEmail(dEmail, subjectDoctor, bodyDoctor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void fetchDoctorEmail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("doctors");
        ref.orderByChild("name").equalTo(doctorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Doctor d = ds.getValue(Doctor.class);
                    if (d != null) {
                        doctorEmail = d.getEmail();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupTranslation() {
        // ================= ML KIT TRANSLATION =================

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
                .addOnSuccessListener(unused -> translateUI())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show()
                );
    }

    // ---- Translate UI Text ----
    private void translateUI() {

        translator.translate(tvDoctor.getText().toString())
                .addOnSuccessListener(tvDoctor::setText);

        translator.translate(btnDate.getText().toString())
                .addOnSuccessListener(btnDate::setText);

        translator.translate(btnTime.getText().toString())
                .addOnSuccessListener(btnTime::setText);

        translator.translate(btnConfirm.getText().toString())
                .addOnSuccessListener(btnConfirm::setText);
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

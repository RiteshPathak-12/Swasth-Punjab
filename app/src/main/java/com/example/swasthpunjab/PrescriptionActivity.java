package com.example.swasthpunjab;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PrescriptionActivity extends AppCompatActivity {

    private String appointmentId, patientName, patientEmail;
    private EditText etDiagnosis, etMedicines;
    private Button btnFollowUp, btnSave;
    private String followUpDate = "Not Required";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        appointmentId = getIntent().getStringExtra("APPOINTMENT_ID");
        patientName = getIntent().getStringExtra("PATIENT_NAME");
        patientEmail = getIntent().getStringExtra("PATIENT_EMAIL"); // Get Email

        TextView tvTitle = findViewById(R.id.tvPrescriptionPatient);
        tvTitle.setText("Patient: " + (patientName != null ? patientName : "N/A"));

        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedicines = findViewById(R.id.etMedicines);
        btnFollowUp = findViewById(R.id.btnFollowUp);
        btnSave = findViewById(R.id.btnSavePrescription);

        btnFollowUp.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                followUpDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                btnFollowUp.setText("Follow-up: " + followUpDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> savePrescription());
    }

    private void savePrescription() {
        String diagnosis = etDiagnosis.getText().toString().trim();
        String medicines = etMedicines.getText().toString().trim();

        if (diagnosis.isEmpty() || medicines.isEmpty()) {
            Toast.makeText(this, "Please fill Diagnosis and Medicines", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (appointmentId == null) {
            Toast.makeText(this, "Error: Appointment ID Missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("prescriptions");
        
        Map<String, Object> prescription = new HashMap<>();
        prescription.put("appointmentId", appointmentId);
        prescription.put("patientName", patientName);
        prescription.put("diagnosis", diagnosis);
        prescription.put("medicines", medicines);
        prescription.put("followUpDate", followUpDate);
        prescription.put("timestamp", ServerValue.TIMESTAMP); // Use ServerValue if imports allow, else System.currentTimeMillis()
        prescription.put("timestamp_local", System.currentTimeMillis());

        ref.child(appointmentId).setValue(prescription)
            .addOnSuccessListener(aVoid -> {
                sendPrescriptionEmail(diagnosis, medicines, followUpDate);
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendPrescriptionEmail(String diagnosis, String medicines, String followUp) {
        if (patientEmail == null || patientEmail.isEmpty()) {
            Toast.makeText(this, "Prescription Saved (No Email Found)", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toast.makeText(this, "Saving & Sending Email...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String subject = "Prescription - SwasthPunjab";
                String body = "Hello " + patientName + ",\n\n" +
                        "Here is your prescription details from your recent consultation:\n\n" +
                        "Diagnosis:\n" + diagnosis + "\n\n" +
                        "Medicines:\n" + medicines + "\n\n" +
                        "Follow-up Date: " + followUp + "\n\n" +
                        "Stay Healthy,\nSwasthPunjab Team";

                EmailSender.sendEmail(patientEmail, subject, body);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Prescription Sent Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Saved, but failed to send email.", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }
}

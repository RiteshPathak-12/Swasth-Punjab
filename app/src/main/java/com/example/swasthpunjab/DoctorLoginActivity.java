package com.example.swasthpunjab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DoctorLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_doctor_login);

        // Views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        mRef = FirebaseDatabase.getInstance().getReference("doctors");

        // Login Action
        btnLogin.setOnClickListener(v -> performLogin());

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorLoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Navigation to Register
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorLoginActivity.this, Doctor_register.class);
            startActivity(intent);
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query Firebase: Find doctor with matching email
        mRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        boolean loginSuccess = false;
                        String activeDoctorName = "";

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Doctor doctor = ds.getValue(Doctor.class);
                            if (doctor != null && password.equals(doctor.getPassword())) {
                                loginSuccess = true;
                                activeDoctorName = doctor.getName();
                                break;
                            }
                        }

                        if (loginSuccess) {
                            Toast.makeText(DoctorLoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            
                            // Go to Dashboard
                            Intent intent = new Intent(DoctorLoginActivity.this, DoctorDashboardActivity.class);
                            intent.putExtra("DOCTOR_NAME", activeDoctorName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(DoctorLoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DoctorLoginActivity.this, "Doctor Email not found. Please Register.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(DoctorLoginActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}

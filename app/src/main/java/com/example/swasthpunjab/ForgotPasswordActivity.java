package com.example.swasthpunjab;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etOTP, etNewPassword;
    private Button btnSendOTP, btnConfirm;
    private LinearLayout layoutEmail, layoutConfirm;
    
    private DatabaseReference mRef;
    private String generatedOTP;
    private String targetDoctorId; // To update password later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etResetEmail);
        etOTP = findViewById(R.id.etResetOTP);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSendOTP = findViewById(R.id.btnSendResetOTP);
        btnConfirm = findViewById(R.id.btnConfirmReset);
        layoutEmail = findViewById(R.id.layoutEmailInput);
        layoutConfirm = findViewById(R.id.layoutResetConfirm);

        mRef = FirebaseDatabase.getInstance().getReference("doctors");

        btnSendOTP.setOnClickListener(v -> verifyEmailAndSendOTP());

        btnConfirm.setOnClickListener(v -> {
            String enteredOTP = etOTP.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (enteredOTP.equals(generatedOTP)) {
                if (!newPass.isEmpty()) {
                    updatePassword(newPass);
                } else {
                    Toast.makeText(this, "Enter a new password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyEmailAndSendOTP() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSendOTP.setEnabled(false);
        Toast.makeText(this, "Verifying... Please wait.", Toast.LENGTH_SHORT).show();

        mRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            targetDoctorId = ds.getKey(); // Store ID for update
                            sendOTP(email);
                            break; 
                        }
                    } else {
                        btnSendOTP.setEnabled(true);
                        Toast.makeText(ForgotPasswordActivity.this, "Email not registered", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    btnSendOTP.setEnabled(true);
                }
            });
    }

    private void sendOTP(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        generatedOTP = String.valueOf(otp);

        new Thread(() -> {
            try {
                EmailSender.sendEmail(email, "SwasthPunjab - Password Reset OTP", 
                    "Hello Doctor,\n\nYour OTP for password reset is: " + generatedOTP + "\n\nThis OTP is valid for 10 minutes.\n\nRegards,\nSwasthPunjab Team");
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "OTP Sent to " + email, Toast.LENGTH_LONG).show();
                    layoutEmail.setVisibility(View.GONE);
                    layoutConfirm.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnSendOTP.setEnabled(true);
                    Toast.makeText(this, "Failed to send email. check connection.", Toast.LENGTH_SHORT).show();
                    Log.e("MailError", e.getMessage());
                });
            }
        }).start();
    }

    private void updatePassword(String newPass) {
        if (targetDoctorId != null) {
            mRef.child(targetDoctorId).child("password").setValue(newPass)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_LONG).show();
                    finish(); // Go back to login
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
        }
    }
}

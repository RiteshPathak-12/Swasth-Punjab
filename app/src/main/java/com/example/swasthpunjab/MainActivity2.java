package com.example.swasthpunjab;

import android.content.Intent;
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

public class MainActivity2 extends AppCompatActivity {

    EditText otp;
    Button Login;
    String receivedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        otp=findViewById(R.id.otp);
        Login=findViewById(R.id.Login);
        String receivedOTP = getIntent().getStringExtra("otp");

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = otp.getText().toString().trim();

                if (enteredOTP.equals(receivedOTP)) {
                    Toast.makeText(MainActivity2.this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show();
                    // Proceed to dashboard or next activity
                } else {
                    Toast.makeText(MainActivity2.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                }

                Intent intent=new Intent(MainActivity2.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
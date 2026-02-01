//package com.example.swasthpunjab;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.google.mlkit.common.model.DownloadConditions;
//import com.google.mlkit.nl.translate.TranslateLanguage;
//import com.google.mlkit.nl.translate.Translation;
//import com.google.mlkit.nl.translate.Translator;
//import com.google.mlkit.nl.translate.TranslatorOptions;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLEncoder;
//
//public class MainActivity extends AppCompatActivity {
//
//    EditText phone_number;
//    Button SendOTP;
//    TextView LoginTitle;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        phone_number = findViewById(R.id.phone_number);
//        SendOTP = findViewById(R.id.SendOTP);
//        LoginTitle=findViewById(R.id.LoginTitle);
//        SendOTP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String phoneOrEmail = phone_number.getText().toString().trim();
//
//                /*if (phoneOrEmail.isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Please enter your phone or email", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // Generate OTP
//                String otp = OTPGenerator.generateOTP();
//
//                // For now, just show it in a Toast (later you’ll send it via SMS or email)
//                Toast.makeText(MainActivity.this, "OTP Sent: " + otp, Toast.LENGTH_LONG).show();
//
//                // Pass OTP to next activity (optional)
//                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
//                intent.putExtra("otp", otp);
//                startActivity(intent);
//            }*/
//
//                String phoneOrEmail1 = phone_number.getText().toString().trim();
//
//                String otp = OTPGenerator.generateOTP();
//                String message = "Your OTP is: " + otp;
//
//                new Thread(() -> {
//                    try {
//                        String apiKey = "apikey=" + "YOUR_API_KEY"; // Replace with your actual key
//                        String sender = "&sender=" + "TXTLCL"; // Replace with your approved sender name
//                        String numbers = "&numbers=" + phoneOrEmail1; // Must be in international format (e.g., 91XXXXXXXXXX)
//                        String msg = "&message=" + URLEncoder.encode(message, "UTF-8");
//
//                        String data = apiKey + numbers + msg + sender;
//                        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
//                        conn.setDoOutput(true);
//                        conn.setRequestMethod("POST");
//                        conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
//                        conn.getOutputStream().write(data.getBytes("UTF-8"));
//
//                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        String line;
//                        StringBuilder response = new StringBuilder();
//                        while ((line = rd.readLine()) != null) {
//                            response.append(line);
//                        }
//                        rd.close();
//
//                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "OTP Sent Successfully!", Toast.LENGTH_LONG).show());
//
//                    } catch (Exception e) {
//                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
//                    }
//                }).start();
//
//
//                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
//                intent.putExtra("otp", otp);
//                startActivity(intent);
//                finish();
//            }
//        });
//
//        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
//        String targetLang = prefs.getString("language", "en");
//        TranslatorOptions options = new TranslatorOptions.Builder()
//                .setSourceLanguage(TranslateLanguage.ENGLISH)
//                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang))
//                .build();
//
//        Translator translator = Translation.getClient(options);
//        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
//
//        translator.downloadModelIfNeeded(conditions)
//                .addOnSuccessListener(unused -> {
//                    translator.translate(phone_number.getText().toString())
//                            .addOnSuccessListener(translated -> phone_number.setText(translated));
//
//                    translator.translate(LoginTitle.getText().toString())
//                            .addOnSuccessListener(translated -> LoginTitle.setText(translated));
//                    translator.translate(SendOTP.getText().toString())
//                            .addOnSuccessListener(translated -> SendOTP.setText(translated));
//                })
//                .addOnFailureListener(e -> {
//                    //Toast.makeText(this, "Translation failed", Toast.LENGTH_SHORT).show();
//                });
//
//    }
//}
package com.example.swasthpunjab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class MainActivity extends AppCompatActivity {

    EditText phone_number; // actually email field
    Button SendOTP;
    TextView LoginTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        phone_number = findViewById(R.id.phone_number);
        SendOTP = findViewById(R.id.SendOTP);
        LoginTitle = findViewById(R.id.LoginTitle);

        SendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = phone_number.getText().toString().trim();

                if (!isValidEmail(email)) {
                    phone_number.setError("Enter valid email");
                    return;
                }

                // Generate OTP
                final String otp = OTPGenerator.generateOTP();
                final String subject = "Your SwasthPunjab OTP";
                final String message = "Your OTP is: " + otp + "\n\nDo not share this code with anyone.";

                // Send email in background thread
                new Thread(() -> {
                    try {
                        EmailSender.sendEmail(email, subject, message);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "OTP sent to email", Toast.LENGTH_LONG).show();

                            // Move to OTP screen
                            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                            intent.putExtra("otp", otp);
                            intent.putExtra("email", email);
                            startActivity(intent);
                            finish();
                        });

                    } catch (final Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(MainActivity.this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                }).start();
            }
        });

        // ---------- Your translation code (unchanged) ----------
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
                    // You probably don't want to translate user input, but I'll keep your original logic
                    translator.translate(LoginTitle.getText().toString())
                            .addOnSuccessListener(translated -> LoginTitle.setText(translated));
                    translator.translate(SendOTP.getText().toString())
                            .addOnSuccessListener(translated -> SendOTP.setText(translated));
                })
                .addOnFailureListener(e -> {
                    // ignore
                });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}

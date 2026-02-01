package com.example.swasthpunjab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MoreFragment extends Fragment {

    private TextView tvEmail;
    private Button btnLogout, btnLanguage;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmail = view.findViewById(R.id.tvUserEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLanguage = view.findViewById(R.id.btnLanguage);

        // 1. Fetch User Data
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("patient_email", "Guest User");
        tvEmail.setText(email);

        // 2. Logout Logic
        btnLogout.setOnClickListener(v -> {
            // Clear Login Session
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Removing email basically logs them out based on our Check in opening.java
            editor.apply();

            Toast.makeText(requireContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();

            // Restart App Flow
            Intent intent = new Intent(requireActivity(), MainActivity0.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
        });

        // 3. Change Language Logic
        btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), MainActivity0.class);
            startActivity(intent);
        });
    }
}
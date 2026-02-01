package com.example.swasthpunjab;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class DashboardFragment extends Fragment {

    private ImageView doctor_photo;
    private Toolbar toolbar;
    private TextView Consultation, totalConsultation;
    private Button Consult_Now, medical_shop;

    // Keep a reference so we can close it to avoid leaks
    private Translator translator;

    // Args (unused here, but kept if you need them)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public DashboardFragment() { }

    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Do NOT touch views here.
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- Bind views from the inflated layout ----
        doctor_photo       = view.findViewById(R.id.doctor_photo);
        Consultation       = view.findViewById(R.id.Consultation);
        totalConsultation  = view.findViewById(R.id.totalConsultation);
        Consult_Now        = view.findViewById(R.id.Consult_Now);
        medical_shop       = view.findViewById(R.id.medical_shop);
        toolbar            = view.findViewById(R.id.toolbar);

        // ---- Toolbar setup (Fragment -> Activity’s ActionBar) ----
        if (toolbar != null) {
            AppCompatActivity activity = (AppCompatActivity) requireActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setTitle(""); // optional
            }
            toolbar.setSubtitle("toolbar");
            setHasOptionsMenu(true); // if you plan to inflate menu
        }

        // ---- Click listeners ----
        Consult_Now.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChatbotActivity.class))
        );

        medical_shop.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), Medical_Shop_list.class))
        );

        // ---- ML Kit Translation setup ----
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", MODE_PRIVATE);
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
                .addOnSuccessListener(unused -> {
                    // Translate current button/text labels
                    translator.translate(Consult_Now.getText().toString())
                            .addOnSuccessListener(translated -> Consult_Now.setText(translated));

                    translator.translate(Consultation.getText().toString())
                            .addOnSuccessListener(translated -> Consultation.setText(translated));

                    translator.translate(medical_shop.getText().toString())
                            .addOnSuccessListener(translated -> medical_shop.setText(translated));

                    translator.translate(totalConsultation.getText().toString())
                            .addOnSuccessListener(translated -> totalConsultation.setText(translated));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Translation failed", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCount();
    }

    private void updateCount() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String email = prefs.getString("patient_email", "");

        if (!email.isEmpty()) {
            MyDBHelper dbHelper = new MyDBHelper(requireContext());
            int count = dbHelper.getAppointmentCount(email);
            // 'Consultation' is the TextView that holds the number "0" (confirmed via XML id)
            // 'totalConsultation' is the header/label "Total Consultations"
            Consultation.setText(String.valueOf(count));
        } else {
            Consultation.setText("0");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Avoid memory leaks from ML Kit translator
        if (translator != null) {
            translator.close();
            translator = null;
        }
        // Null out view refs (optional good practice)
        doctor_photo = null;
        toolbar = null;
        Consultation = null;
        totalConsultation = null;
        Consult_Now = null;
        medical_shop = null;
    }
}
